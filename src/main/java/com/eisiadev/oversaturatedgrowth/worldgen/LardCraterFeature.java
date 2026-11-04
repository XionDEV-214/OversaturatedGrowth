package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.entity.GluttonToadEntity;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LardCraterFeature extends Feature<NoneFeatureConfiguration> {

    private static final float SPRINKLE = 0.22f;
    private static final int SPRINKLE_RANGE = 5;

    public LardCraterFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rnd = ctx.random();
        BlockPos center = ctx.origin().below();
        double r = 3.0 + rnd.nextDouble();
        int ri = (int) Math.ceil(r) + 1;
        BlockState lard = ModBlocks.LARD_BLOCK.get().defaultBlockState();
        int minY = level.getMinBuildHeight() + 6;
        if (center.getY() - ri < minY) return false;
        if (level.getBlockState(center).isAir()) return false;

        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos q = new BlockPos.MutableBlockPos();

        for (int dx = -ri; dx <= ri; dx++) for (int dy = -ri; dy <= ri; dy++) for (int dz = -ri; dz <= ri; dz++) {
            if (dx * dx + dy * dy + dz * dz > r * r) continue;
            p.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
            BlockState st = level.getBlockState(p);
            if (st.isAir() || !st.getFluidState().isEmpty() || st.is(Blocks.BEDROCK)) continue;
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
        }

        double r2 = (r + 1.5) * (r + 1.5);
        int rr = ri + 1;
        for (int dx = -rr; dx <= rr; dx++) for (int dy = -rr; dy <= rr; dy++) for (int dz = -rr; dz <= rr; dz++) {
            if (dx * dx + dy * dy + dz * dz > r2) continue;
            p.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
            BlockState st = level.getBlockState(p);
            if (st.isAir() || !st.getFluidState().isEmpty() || st.is(Blocks.BEDROCK)) continue;

            boolean touches = false;
            for (Direction d : Direction.values()) {
                q.set(p).move(d);
                int ex = q.getX() - center.getX(), ey = q.getY() - center.getY(), ez = q.getZ() - center.getZ();
                if (ex * ex + ey * ey + ez * ez <= r * r && level.getBlockState(q).isAir()) { touches = true; break; }
            }
            if (touches) level.setBlock(p, lard, 2);
        }

        int range = (int) Math.ceil(r) + SPRINKLE_RANGE;
        for (int dx = -range; dx <= range; dx++) for (int dz = -range; dz <= range; dz++) {
            double d2 = dx * dx + dz * dz;
            if (d2 <= (r + 0.5) * (r + 0.5) || d2 > range * range) continue;
            if (rnd.nextFloat() >= SPRINKLE) continue;
            for (int y = center.getY() + 4; y >= center.getY() - 4; y--) {
                p.set(center.getX() + dx, y, center.getZ() + dz);
                BlockState st = level.getBlockState(p);
                if (st.isAir() || !st.getFluidState().isEmpty()) continue;
                q.set(p).move(Direction.UP);
                if (level.getBlockState(q).isAir() && !st.is(Blocks.BEDROCK)) level.setBlock(p, lard, 2);
                break;
            }
        }

        if (rnd.nextFloat() < 0.5f) {
            for (int y = center.getY() + 1; y >= center.getY() - ri; y--) {
                p.set(center.getX(), y, center.getZ());
                if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).is(lard.getBlock())) {
                    GluttonToadEntity toad = ModEntities.GLUTTON_TOAD.get().create(level.getLevel());
                    if (toad != null) {
                        toad.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rnd.nextFloat() * 360f, 0f);
                        toad.finalizeSpawn(level, level.getCurrentDifficultyAt(p), MobSpawnType.CHUNK_GENERATION, null, null);
                        toad.setPersistenceRequired();
                        level.addFreshEntity(toad);
                    }
                    break;
                }
            }
        }
        return true;
    }
}
