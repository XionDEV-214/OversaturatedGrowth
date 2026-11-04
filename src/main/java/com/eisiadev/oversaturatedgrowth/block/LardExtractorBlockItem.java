package com.eisiadev.oversaturatedgrowth.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class LardExtractorBlockItem extends BlockItem {

    public LardExtractorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext context, net.minecraft.world.level.block.state.@NotNull BlockState state) {
        return super.placeBlock(context, state);
    }
}
