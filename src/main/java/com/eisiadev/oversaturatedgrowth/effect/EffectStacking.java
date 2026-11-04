package com.eisiadev.oversaturatedgrowth.effect;

import com.eisiadev.oversaturatedgrowth.init.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public final class EffectStacking {
    private EffectStacking() {}
    private static final int DURATION = 100;

    public static void stackFullness(ServerPlayer p) {
        MobEffectInstance sat = p.getEffect(ModEffects.OVERSATURATED.get());
        if (sat == null) { p.addEffect(new MobEffectInstance(ModEffects.OVERSATURATED.get(), DURATION, 0)); return; }
        if (sat.getAmplifier() < 4) {
            p.addEffect(new MobEffectInstance(ModEffects.OVERSATURATED.get(), DURATION, sat.getAmplifier() + 1));
            return;
        }
        p.addEffect(new MobEffectInstance(ModEffects.OVERSATURATED.get(), DURATION, 4));
        MobEffectInstance eat = p.getEffect(ModEffects.OVEREAT.get());
        int amp = eat == null ? 0 : Math.min(4, eat.getAmplifier() + 1);
        p.addEffect(new MobEffectInstance(ModEffects.OVEREAT.get(), DURATION, amp));
    }

    public static void rollOvereatOnEatWhenFull(ServerPlayer p) {
        if (p.hasEffect(ModEffects.OVEREAT.get())) return;
        double chance = com.eisiadev.oversaturatedgrowth.config.ModServerConfig.overeatChanceWhenFull.get() / 100.0;
        if (p.getRandom().nextDouble() >= chance) return;
        int ticks = com.eisiadev.oversaturatedgrowth.config.ModServerConfig.overeatFullDurationSeconds.get() * 20;
        p.addEffect(new MobEffectInstance(ModEffects.OVEREAT.get(), ticks, 0));
    }
}
