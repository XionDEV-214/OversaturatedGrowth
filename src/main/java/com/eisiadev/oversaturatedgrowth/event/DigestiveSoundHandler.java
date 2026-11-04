package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.OGSoundS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DigestiveSoundHandler {
    private DigestiveSoundHandler() {}

    public static final int DEFAULT_GURGLE_RATE = 4;
    public static final int MAX_GURGLE_RATE = 25;

    private static final double RUMBLE_BASE = 900;

    private static final double RUMBLE_AT_FULL_WEIGHT = 0.35;
    private static final double RUMBLE_AT_FULL_CAL = 0.35;

    private static final double DIGEST_BASE = 400;
    private static final double DIGEST_PERCENT = 0.05;

    private static final Map<UUID, Integer> GURGLE_RATE = new ConcurrentHashMap<>();
    private static final Map<UUID, State> STATES = new HashMap<>();

    private static final class State {
        int rumbleCd = -1, digestCd = -1;
        double lastX, lastZ;
        boolean hasPos;
    }

    public static void setGurgleRate(UUID id, int rate) {
        GURGLE_RATE.put(id, Math.max(0, Math.min(MAX_GURGLE_RATE, rate)));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        GURGLE_RATE.remove(id);
        STATES.remove(id);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp) || sp.isSpectator()) return;
        State st = STATES.computeIfAbsent(sp.getUUID(), k -> new State());

        double dx = sp.getX() - st.lastX, dz = sp.getZ() - st.lastZ;
        boolean moving = st.hasPos && (dx * dx + dz * dz) > 0.0004;
        st.lastX = sp.getX();
        st.lastZ = sp.getZ();
        st.hasPos = true;

        tickRumble(sp, st);
        tickDigesting(sp, st, moving);
    }

    private static void tickRumble(ServerPlayer sp, State st) {
        int rate = GURGLE_RATE.getOrDefault(sp.getUUID(), DEFAULT_GURGLE_RATE);
        if (rate <= 0) { st.rumbleCd = -1; return; }
        float w = GgBridge.weightRatio(sp), c = GgBridge.calorieRatio(sp);
        if (w <= 0f && c <= 0f) { st.rumbleCd = -1; return; }

        if (st.rumbleCd < 0) { st.rumbleCd = nextRumble(sp, w, c, rate); return; }
        if (--st.rumbleCd > 0) return;

        float vol = 0.5f + 0.5f * Math.max(w, c);
        send(sp, OGSoundS2CPacket.RUMBLE, vol, 0.85f + sp.getRandom().nextFloat() * 0.3f);
        st.rumbleCd = nextRumble(sp, w, c, rate);
    }

    private static int nextRumble(ServerPlayer sp, float w, float c, int rate) {
        double d = RUMBLE_BASE
                * (1.0 - (1.0 - RUMBLE_AT_FULL_WEIGHT) * w)
                * (1.0 - (1.0 - RUMBLE_AT_FULL_CAL) * c);
        d /= (rate / (double) DEFAULT_GURGLE_RATE);
        d *= 0.7 + sp.getRandom().nextDouble() * 0.6;
        return Math.max(10, (int) d);
    }

    private static void tickDigesting(ServerPlayer sp, State st, boolean moving) {
        // Only with at least one Digestion line in the Accessory Bag: then every ~400 ticks (faster while walking /
        // sprinting) 5% of max calories + the summed Digestion % is turned into weight.
        if (com.eisiadev.oversaturatedgrowth.accessory.AccessoryEffects.totals(sp).digestion() <= 0
                || GgBridge.currentCalories(sp) < 1) { st.digestCd = -1; return; }

        if (st.digestCd < 0) { st.digestCd = nextDigest(sp, moving); return; }
        if (--st.digestCd > 0) return;

        if (GgBridge.digest(sp, com.eisiadev.oversaturatedgrowth.accessory.AccessoryEffects.digestShare(sp, DIGEST_PERCENT),
                com.eisiadev.oversaturatedgrowth.accessory.AccessoryEffects.absorptionMultiplier(sp))) {
            send(sp, OGSoundS2CPacket.DIGESTING, 0.9f, 0.9f + sp.getRandom().nextFloat() * 0.2f);
        }
        st.digestCd = nextDigest(sp, moving);
    }

    private static int nextDigest(ServerPlayer sp, boolean moving) {
        double d = DIGEST_BASE;
        if (sp.isSprinting()) d /= 3.0;
        else if (moving) d /= 1.5;
        d *= 0.7 + sp.getRandom().nextDouble() * 0.6;
        return Math.max(10, (int) d);
    }

    private static void send(ServerPlayer sp, int type, float volume, float pitch) {
        Vec3 pos = sp.position().add(0, sp.getBbHeight() * 0.5, 0);
        ModMessages.sendToNearby(new OGSoundS2CPacket(type, pos.x, pos.y, pos.z, volume, pitch),
                sp.serverLevel(), pos, 16);
    }
}
