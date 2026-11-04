package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LardDrapeFeature extends Feature<NoneFeatureConfiguration> {

    public LardDrapeFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rnd = ctx.random();
        BlockPos o = ctx.origin();
        BlockState sludge = ModBlocks.LARD_SLUDGE_BLOCK.get().defaultBlockState();
        if (!level.getBlockState(o).isAir()) return false;

        boolean pillar = rnd.nextFloat() < 0.10f;
        int len = pillar ? 24 : 2 + rnd.nextInt(7);
        int placed = 0;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        boolean reachedFloor = false;
        for (int i = 0; i < len; i++) {
            p.set(o).move(Direction.DOWN, i);
            if (!level.getBlockState(p).isAir()) { reachedFloor = true; break; }
            level.setBlock(p, sludge, 2);
            placed++;
        }
        if (pillar && !reachedFloor) {

            for (int i = Math.min(placed, 8); i < placed; i++) {
                p.set(o).move(Direction.DOWN, i);
                level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
            }
            placed = Math.min(placed, 8);
        }

        if (placed >= 3) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (rnd.nextFloat() < 0.5f) {
                    p.set(o).move(d);
                    if (level.getBlockState(p).isAir()) level.setBlock(p, sludge, 2);
                }
            }
        }
        return placed > 0;
    }
}
