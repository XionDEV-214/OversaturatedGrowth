package com.eisiadev.oversaturatedgrowth.effect;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class WeightlessEffect extends MobEffect {
    public WeightlessEffect() { super(MobEffectCategory.BENEFICIAL, 0xBFE8FF); }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) GgBridge.suppressWeightEffects(player);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 10 == 0; }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
        super.addAttributeModifiers(entity, map, amplifier);
        if (entity instanceof ServerPlayer player) GgBridge.suppressWeightEffects(player);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
        if (entity instanceof ServerPlayer player) GgBridge.restoreWeightEffects(player);
    }
}
