package com.eisiadev.oversaturatedgrowth.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FattaberryStomachEffect extends MobEffect {
    public FattaberryStomachEffect() { super(MobEffectCategory.HARMFUL, 0xC77DE0); }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) { OvereatEffect.retch(entity, false); }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 400 == 0; }
}
