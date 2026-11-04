package com.eisiadev.oversaturatedgrowth.effect;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class OversaturatedEffect extends MobEffect {
    private static final String ACC_KEY = "og_oversat_stall";

    public OversaturatedEffect() {
        super(MobEffectCategory.HARMFUL, 0xE0A030);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "3c5e7a10-8d24-4b6f-a1e9-2f6d9b0c4a77", -0.10,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer player)) return;
        double stall = Math.min(1.0, 0.2 * (amplifier + 1));
        double acc = player.getPersistentData().getDouble(ACC_KEY) + stall;
        int ticks = (int) acc;
        player.getPersistentData().putDouble(ACC_KEY, acc - ticks);
        if (ticks > 0) GgBridge.stallCalorieClock(player, ticks);
        if (player.tickCount % 20 == 0) GgBridge.syncCalorieDelay(player);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return true; }
}
