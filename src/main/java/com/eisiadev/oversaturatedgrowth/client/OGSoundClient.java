package com.eisiadev.oversaturatedgrowth.client;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModSounds;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.GurgleRateC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.willsbr.gluttonousgrowth.Menu.Buttons.OptionSlider;
import net.willsbr.gluttonousgrowth.Menu.ClientConfigScreen;
import net.willsbr.gluttonousgrowth.config.GluttonousClientConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class OGSoundClient {
    private OGSoundClient() {}

    private static final float SLOSH_START_RATIO = 0.25f;
    private static final float SLOSH_MAX_VOLUME = 0.5f;
    private static final float TURN_THRESHOLD_DEG = 35f;

    private static Set<ResourceLocation> belchIds;
    private static Set<ResourceLocation> gurgleIds;

    private static void initIds() {
        if (belchIds != null) return;
        belchIds = new HashSet<>();
        gurgleIds = new HashSet<>();
        for (var o : net.willsbr.gluttonousgrowth.sound.ModSounds.BURP_SOUNDS) belchIds.add(o.getId());
        for (var o : net.willsbr.gluttonousgrowth.sound.ModSounds.GURGLE_SOUNDS) gurgleIds.add(o.getId());
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance s = event.getSound();
        if (s == null) return;
        initIds();
        ResourceLocation id = s.getLocation();
        if (belchIds.contains(id)) {
            event.setSound(new SimpleSoundInstance(ModSounds.BELCH.get().getLocation(), s.getSource(),
                    s.getVolume(), s.getPitch(), RandomSource.create(), false, 0,
                    SoundInstance.Attenuation.LINEAR, s.getX(), s.getY(), s.getZ(), false));
        } else if (gurgleIds.contains(id)) {
            event.setSound(null);
        }
    }

    public static void playDigestive(int type, double x, double y, double z, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        int dv = GluttonousClientConfig.digestiveSoundsVolume.get();
        if (dv <= 0) return;
        boolean rumble = type == 0;
        if (rumble && GluttonousClientConfig.gurgleFrequency.get() <= 0) return;
        mc.level.playLocalSound(x, y, z, rumble ? ModSounds.RUMBLE.get() : ModSounds.DIGESTING.get(),
                SoundSource.PLAYERS, volume * dv / 10f, pitch, false);
    }

    private static float lastWalk, walkAcc, lastYaw;
    private static boolean tracking;
    private static int sloshCooldown, sentRate = -1, rateTimer;

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        sentRate = -1;
        tracking = false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.getConnection() == null) { tracking = false; return; }

        int rate = Mth.clamp(GluttonousClientConfig.gurgleFrequency.get(), 0, 25);
        if (rate != sentRate || ++rateTimer >= 200) {
            ModMessages.sendToServer(new GurgleRateC2SPacket(rate));
            sentRate = rate;
            rateTimer = 0;
        }

        float yaw = p.getYRot();
        float dWalk = p.walkDist - lastWalk;
        float dYaw = Math.abs(Mth.wrapDegrees(yaw - lastYaw));
        lastWalk = p.walkDist;
        lastYaw = yaw;
        if (!tracking) { tracking = true; return; }
        if (sloshCooldown > 0) sloshCooldown--;

        int max = GluttonousClientConfig.getMaxWeight();
        float ratio = max <= 0 ? 0f : (float) net.willsbr.gluttonousgrowth.client.ClientWeightBarData.getPlayerWeight() / max;
        if (ratio < SLOSH_START_RATIO || p.isSpectator()) { walkAcc = 0; return; }

        if (p.onGround() && !p.isPassenger() && dWalk > 0 && dWalk < 3f) {
            walkAcc += dWalk;
            if (walkAcc >= 1.0f) { walkAcc -= 1.0f; slosh(mc, p, ratio); }
        }
        if (dYaw > TURN_THRESHOLD_DEG && sloshCooldown == 0) {
            slosh(mc, p, ratio);
            sloshCooldown = 8;
        }
    }

    private static void slosh(Minecraft mc, LocalPlayer p, float ratio) {
        int dv = GluttonousClientConfig.digestiveSoundsVolume.get();
        if (dv <= 0) return;
        float t = Mth.clamp((ratio - SLOSH_START_RATIO) / (1f - SLOSH_START_RATIO), 0f, 1f);
        float vol = SLOSH_MAX_VOLUME * t * dv / 10f;
        if (vol < 0.01f) return;
        mc.level.playLocalSound(p.getX(), p.getY(), p.getZ(), ModSounds.SLOSH.get(), SoundSource.PLAYERS,
                vol, 0.9f + p.getRandom().nextFloat() * 0.2f, false);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof ClientConfigScreen)) return;
        try {
            Field f = ClientConfigScreen.class.getDeclaredField("gurgleFrequency");
            f.setAccessible(true);
            OptionSlider old = (OptionSlider) f.get(screen);
            if (old == null) return;
            GurgleSlider replacement = new GurgleSlider(old.getX(), old.getY(), old.getWidth(), old.getHeight(),
                    GluttonousClientConfig.gurgleFrequency.get());
            event.removeListener(old);
            Method add = ObfuscationReflectionHelper.findMethod(Screen.class, "m_142416_", GuiEventListener.class);
            add.invoke(screen, replacement);
            f.set(screen, replacement);
        } catch (Throwable t) {
            OversaturatedGrowth.LOGGER.warn("Gurgle Rate slider extension failed (config file value up to 25 still works): {}", t.toString());
        }
    }

    private static final class GurgleSlider extends OptionSlider {
        private static final double SCALE = 25.0;

        GurgleSlider(int x, int y, int w, int h, int value) {
            super(x, y, w, h, Component.translatable("menu.overstuffed.gurglefreq"), Mth.clamp(value / SCALE, 0.0, 1.0));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("menu.overstuffed.gurglefreq").append(": " + getValue()));
        }

        @Override
        protected void applyValue() {
            this.value = Math.round(this.value * SCALE) / SCALE;
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            double keep = this.value;
            super.onRelease(mouseX, mouseY);
            this.value = Math.round(keep * SCALE) / SCALE;
            updateMessage();
        }

        @Override
        public int getValue() {
            return (int) Math.round(this.value * SCALE);
        }
    }
}
