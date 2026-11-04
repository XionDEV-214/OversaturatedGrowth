package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/** The summed (and capped) effect of everything in an Accessory Bag. */
public record AccessoryTotals(double maxCalories, double digestion, double movementPct,
                              double healthHp, double absorptionPct, int slotsUsed) {

    public static final AccessoryTotals EMPTY = new AccessoryTotals(0, 0, 0, 0, 0, 0);

    public static AccessoryTotals of(IItemHandler handler) {
        double[] sum = new double[AccessoryStat.VALUES.length];
        int used = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            AccessoryData data = AccessoryData.read(stack);
            if (data == null) continue;
            used++;
            for (AccessoryData.Entry e : data.entries()) sum[e.stat().ordinal()] += e.value();
        }
        return new AccessoryTotals(
                Math.min(sum[AccessoryStat.MAX_CALORIES.ordinal()], AccessoryConfig.maxCaloriesCap()),
                Math.min(sum[AccessoryStat.DIGESTION.ordinal()], AccessoryConfig.digestionCap()),
                Math.min(sum[AccessoryStat.MOVEMENT.ordinal()], AccessoryConfig.movementCapPercent()),
                Math.min(sum[AccessoryStat.HEALTH.ordinal()], AccessoryConfig.healthCap()),
                Math.min(sum[AccessoryStat.ABSORPTION.ordinal()], AccessoryConfig.absorptionCapPercent()),
                used);
    }

    /** Extra max calories (flat). */
    public int bonusCalories() {
        return (int) Math.round(maxCalories);
    }

    /** Share (0..1) of the weight speed penalty that is cancelled. */
    public double movementReduction() {
        return Math.max(0.0, Math.min(1.0, movementPct / 100.0));
    }

    /** Multiplier on the weight gained per converted calorie. */
    public double absorptionMultiplier() {
        return 1.0 + absorptionPct / 100.0;
    }

    /** Share of the max calories converted per OG digestion burst: base (0.05 = 5%) + the summed Digestion %. */
    public double digestShare(double base) {
        return Math.min(AccessoryConfig.digestMaxPercent() / 100.0, base + digestion / 100.0);
    }
}
