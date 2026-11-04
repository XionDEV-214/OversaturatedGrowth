package com.eisiadev.oversaturatedgrowth.effect;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class WeightLosingEffect extends MobEffect {
    public WeightLosingEffect() { super(MobEffectCategory.BENEFICIAL, 0x7FD4C8); }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) GgBridge.loseWeight(player, amplifier + 1);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 8 == 0; }
}
