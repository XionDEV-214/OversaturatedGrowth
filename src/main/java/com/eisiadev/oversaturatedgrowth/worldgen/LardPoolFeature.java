package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.entity.LardfinEntity;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.init.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LardPoolFeature extends Feature<NoneFeatureConfiguration> {

    public LardPoolFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rnd = ctx.random();
        BlockPos top = ctx.origin().below();
        int r = 1 + rnd.nextInt(2);
        int depth = 7 + rnd.nextInt(7);
        BlockState lard = ModFluids.REFINED_LARD_BLOCK.get().defaultBlockState();

        if (level.getBlockState(top).isAir() || !level.getBlockState(top).getFluidState().isEmpty()) return false;
        if (level.getBlockState(top.below(depth + 1)).isAir()) return false;

        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                double d2 = dx * dx + dz * dz;
                if (d2 > (r + 1) * (r + 1) || d2 <= r * r + 0.5) continue;
                BlockPos edge = top.offset(dx, 0, dz);
                BlockState es = level.getBlockState(edge);
                if (es.isAir() || !es.getFluidState().isEmpty()) return false;
            }
        }

        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < depth; dy++) {
            int rr = dy > depth - 3 ? Math.max(1, r - 1) : r;
            for (int dx = -rr; dx <= rr; dx++) {
                for (int dz = -rr; dz <= rr; dz++) {
                    if (dx * dx + dz * dz > rr * rr + 0.5) continue;
                    p.set(top.getX() + dx, top.getY() - dy, top.getZ() + dz);
                    level.setBlock(p, lard, 2);
                    level.scheduleTick(p, lard.getFluidState().getType(), 0);
                }
            }
        }

        int fins = 1 + rnd.nextInt(2);
        for (int i = 0; i < fins; i++) {
            LardfinEntity fin = ModEntities.LARDFIN.get().create(level.getLevel());
            if (fin == null) break;
            fin.moveTo(top.getX() + 0.5, top.getY() - 1 - rnd.nextInt(Math.max(1, depth - 3)), top.getZ() + 0.5, rnd.nextFloat() * 360f, 0f);
            fin.finalizeSpawn(level, level.getCurrentDifficultyAt(top), MobSpawnType.CHUNK_GENERATION, null, null);
            level.addFreshEntity(fin);
        }
        return true;
    }
}
