package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;

public class GreasySplatterFeature extends Feature<NoneFeatureConfiguration> {

    private static final int RAY = 8;

    public GreasySplatterFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rnd = ctx.random();
        BlockPos origin = ctx.origin();
        int minX = ((origin.getX() >> 4) << 4) - 12, minZ = ((origin.getZ() >> 4) << 4) - 12;
        int splats = 0;
        for (int attempt = 0; attempt < 60 && splats < 4; attempt++) {
            BlockPos c = new BlockPos(origin.getX() + rnd.nextInt(11) - 5, -40 + rnd.nextInt(50), origin.getZ() + rnd.nextInt(11) - 5);
            if (!level.getBlockState(c).isAir()) continue;
            if (splat(level, rnd, c, minX, minZ)) splats++;
        }
        return splats > 0;
    }

    private boolean splat(WorldGenLevel level, RandomSource rnd, BlockPos o, int minX, int minZ) {

        Vec3 dir = new Vec3(rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian()).normalize();
        BlockPos hit = null;
        for (int i = 1; i <= RAY; i++) {
            BlockPos p = BlockPos.containing(o.getX() + 0.5 + dir.x * i, o.getY() + 0.5 + dir.y * i, o.getZ() + 0.5 + dir.z * i);
            if (p.getX() < minX + 6 || p.getX() > minX + 41 || p.getZ() < minZ + 6 || p.getZ() > minZ + 41) break;
            if (!level.getBlockState(p).isAir()) { hit = p; break; }
        }
        if (hit == null) return false;

        Block lard = ModBlocks.LARDSTONE.get(), weathered = ModBlocks.WEATHERED_LARDSTONE.get();
        double radius = 3.0 + rnd.nextDouble() * 2.0;
        int ri = (int) Math.ceil(radius);
        boolean any = false;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos q = new BlockPos.MutableBlockPos();
        for (int dx = -ri; dx <= ri; dx++) for (int dy = -ri; dy <= ri; dy++) for (int dz = -ri; dz <= ri; dz++) {
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d > radius) continue;
            p.set(hit.getX() + dx, hit.getY() + dy, hit.getZ() + dz);
            if (p.getX() < minX || p.getX() > minX + 43 || p.getZ() < minZ || p.getZ() > minZ + 43) continue;
            Block b = level.getBlockState(p).getBlock();
            if (b != lard && b != weathered) continue;
            boolean exposed = false;
            for (Direction dir6 : Direction.values()) {
                q.set(p).move(dir6);
                if (level.getBlockState(q).isAir()) { exposed = true; break; }
            }
            if (!exposed) continue;
            double chance = Math.pow(1.0 - d / (radius + 0.5), 0.6) * 0.97;
            if (rnd.nextDouble() >= chance) continue;
            level.setBlock(p, (b == lard ? ModBlocks.GREASY_LARDSTONE : ModBlocks.WEATHERED_GREASY_LARDSTONE).get().defaultBlockState(), 2);
            any = true;
        }
        return any;
    }
}
