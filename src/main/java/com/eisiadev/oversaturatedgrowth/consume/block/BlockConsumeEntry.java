package com.eisiadev.oversaturatedgrowth.consume.block;

import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public record BlockConsumeEntry(double caloriesPercent, double weightPercent, int holdTicks,
                                boolean bite, List<MobEffectInstance> effects) {
}
