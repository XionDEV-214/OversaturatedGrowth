package com.eisiadev.oversaturatedgrowth.effect;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class IncreasedCapacityEffect extends MobEffect {
    private static final String KEY = "og_cap_base";

    public IncreasedCapacityEffect() { super(MobEffectCategory.BENEFICIAL, 0xFFB347); }

    private static int bonus(int amplifier) {
        return Math.max(1, Math.round(GgBridge.absCalCap() * 0.25f * (amplifier + 1)));
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
        super.addAttributeModifiers(entity, map, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY)) data.putInt(KEY, GgBridge.maxCalories(player));
        GgBridge.adjustMaxCalories(player, bonus(amplifier));
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        int base = data.contains(KEY) ? data.getInt(KEY) : 1;
        data.remove(KEY);
        int next = Math.max(base, GgBridge.maxCalories(player) - bonus(amplifier));
        GgBridge.setMaxCalories(player, next);
    }
}
