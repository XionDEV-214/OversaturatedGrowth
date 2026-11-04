package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.effect.EffectStacking;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EffectEvents {
    private EffectEvents() {}

    private static final Set<UUID> FULL_AT_START = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity e = event.getEntity();
        if (!e.hasEffect(ModEffects.OVEREAT.get()) || !event.getItem().isEdible()) return;
        event.setCanceled(true);
        if (e instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.translatable("message.oversaturatedgrowth.overeat_blocked"), true);
        }
    }

    @SubscribeEvent
    public static void onUseStartRecord(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer sp) || !event.getItem().isEdible()) return;
        if (GgBridge.isCalorieFull(sp)) FULL_AT_START.add(sp.getUUID()); else FULL_AT_START.remove(sp.getUUID());
    }

    @SubscribeEvent
    public static void onUseStop(LivingEntityUseItemEvent.Stop event) {
        FULL_AT_START.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer sp) || !event.getItem().isEdible()) return;
        if (FULL_AT_START.remove(sp.getUUID())) EffectStacking.rollOvereatOnEatWhenFull(sp);
    }
}
