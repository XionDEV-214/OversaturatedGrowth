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

public class WeatheringFeature extends Feature<NoneFeatureConfiguration> {

    private static final float[] CHANCE = {0f, 0.90f, 0.70f, 0.45f, 0.20f};
    private static final int MAX_DEPTH = 4;
    private static final int MAX_Y = 48;

    public WeatheringFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rnd = ctx.random();
        BlockPos o = ctx.origin();
        int x0 = o.getX() & ~15, z0 = o.getZ() & ~15;
        int minY = level.getMinBuildHeight();
        int maxY = Math.min(MAX_Y, level.getMaxBuildHeight() - 1);
        BlockState lard = ModBlocks.LARDSTONE.get().defaultBlockState();
        BlockState weathered = ModBlocks.WEATHERED_LARDSTONE.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        boolean any = false;

        for (int x = x0; x < x0 + 16; x++) {
            for (int z = z0; z < z0 + 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    if (!level.getBlockState(pos).is(lard.getBlock())) continue;
                    int d = airDistance(level, pos, probe);
                    if (d < 1 || d > MAX_DEPTH) continue;
                    if (rnd.nextFloat() < CHANCE[d]) {
                        level.setBlock(pos, weathered, 2);
                        any = true;
                    }
                }
            }
        }
        return any;
    }

    private static int airDistance(WorldGenLevel level, BlockPos origin, BlockPos.MutableBlockPos probe) {
        int best = MAX_DEPTH + 1;
        for (Direction dir : Direction.values()) {
            for (int k = 1; k < best; k++) {
                probe.set(origin).move(dir, k);
                BlockState st = level.getBlockState(probe);
                if (st.isAir()) { best = k; break; }
                if (!st.is(ModBlocks.LARDSTONE.get()) && !st.is(ModBlocks.WEATHERED_LARDSTONE.get())
                        && !st.isSolidRender(level, probe)) { best = k; break; }
            }
        }
        return best;
    }
}
