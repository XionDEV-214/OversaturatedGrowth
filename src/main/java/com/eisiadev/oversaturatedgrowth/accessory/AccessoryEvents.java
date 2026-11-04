package com.eisiadev.oversaturatedgrowth.accessory;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class AccessoryEvents {

    private AccessoryEvents() {}

    @Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(AccessoryBag.class);
        }
    }

    @Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {

        @SubscribeEvent
        public static void attach(AttachCapabilitiesEvent<Entity> event) {
            if (!(event.getObject() instanceof Player)) return;
            AccessoryBagProvider provider = new AccessoryBagProvider();
            event.addCapability(AccessoryBagProvider.ID, provider);
            event.addListener(provider::invalidate);
        }

        /** The bag always carries over, death or not (like Skyblock's Accessory Bag). */
        @SubscribeEvent
        public static void clone(PlayerEvent.Clone event) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(AccessoryBagProvider.CAP).ifPresent(old ->
                    event.getEntity().getCapability(AccessoryBagProvider.CAP).ifPresent(bag ->
                            bag.deserializeNBT(old.serializeNBT())));
            event.getOriginal().invalidateCaps();
        }

        @SubscribeEvent
        public static void login(PlayerEvent.PlayerLoggedInEvent event) {
            markDirty(event.getEntity());
        }

        @SubscribeEvent
        public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
            markDirty(event.getEntity());
        }

        @SubscribeEvent
        public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            markDirty(event.getEntity());
        }

        @SubscribeEvent
        public static void tick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp)) return;
            // The weight bar changes slowly, so a once-a-second refresh keeps the weight-scaled stats in step.
            boolean periodic = sp.tickCount % 20 == 0;
            sp.getCapability(AccessoryBagProvider.CAP).ifPresent(bag -> {
                if (bag.consumeDirty() || periodic) AccessoryEffects.apply(sp, bag);
            });
        }

        private static void markDirty(Player player) {
            player.getCapability(AccessoryBagProvider.CAP).ifPresent(AccessoryBag::markDirty);
        }
    }
}
