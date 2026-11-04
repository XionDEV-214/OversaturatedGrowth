package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;

public class GrottoFaunaFeature extends Feature<NoneFeatureConfiguration> {

    public GrottoFaunaFeature() { super(NoneFeatureConfiguration.CODEC); }

    private static List<EntityType<? extends Mob>> types() {
        return List.of(ModEntities.LARDY_PIG.get(), ModEntities.LARDY_COW.get(),
                ModEntities.LARDY_SHEEP.get(), ModEntities.LARDY_CHICKEN.get());
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rnd = ctx.random();
        BlockPos o = ctx.origin();
        BlockPos floor = null;
        for (int i = 0; i < 60 && floor == null; i++) {
            BlockPos p = new BlockPos(o.getX() + rnd.nextInt(15) - 7, -40 + rnd.nextInt(50), o.getZ() + rnd.nextInt(15) - 7);
            if (isFloor(level, p)) floor = p;
        }
        if (floor == null) return false;

        List<EntityType<? extends Mob>> all = types();
        EntityType<? extends Mob> type = all.get(rnd.nextInt(all.size()));
        int n = 2 + rnd.nextInt(3);
        int spawned = 0;
        for (int i = 0; i < n * 4 && spawned < n; i++) {
            BlockPos p = floor.offset(rnd.nextInt(7) - 3, rnd.nextInt(3) - 1, rnd.nextInt(7) - 3);
            if (Math.abs(p.getX() - o.getX()) > 12 || Math.abs(p.getZ() - o.getZ()) > 12) continue;
            if (!isFloor(level, p)) continue;
            Mob mob = type.create(level.getLevel());
            if (mob == null) continue;
            mob.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rnd.nextFloat() * 360F, 0F);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(p), MobSpawnType.CHUNK_GENERATION, null, null);
            if (rnd.nextInt(6) == 0) mob.setBaby(true);
            level.addFreshEntityWithPassengers(mob);
            spawned++;
        }
        return spawned > 0;
    }

    private static boolean isFloor(WorldGenLevel level, BlockPos p) {
        return level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
                && level.getBlockState(p.above(2)).isAir()
                && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)
                && level.getFluidState(p).isEmpty();
    }
}
