package com.eisiadev.oversaturatedgrowth.util;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import net.minecraft.world.item.ItemStack;

public final class LardItemFluid {
    private LardItemFluid() {}

    public static final int REFINED_LARD_MB = 576;
    public static final int SLUDGE_BALL_MB  = 1152;
    public static final int SLUDGE_BLOCK_MB = 10368;

    public static int mbFor(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(ModItems.REFINED_LARD.get())) return REFINED_LARD_MB;
        if (stack.is(ModItems.LARD_SLUDGE_BALL.get())) return SLUDGE_BALL_MB;
        if (stack.is(ModBlocks.LARD_SLUDGE_BLOCK.get().asItem())) return SLUDGE_BLOCK_MB;
        return 0;
    }
}
