package com.eisiadev.oversaturatedgrowth.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.levelgen.Aquifer;

import java.util.function.Function;

public class GiantCavernCarver extends WorldCarver<CaveCarverConfiguration> {

    public GiantCavernCarver() { super(CaveCarverConfiguration.CODEC); }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration cfg, RandomSource random) {
        return random.nextFloat() <= cfg.probability;
    }

    @Override
    public boolean carve(CarvingContext ctx, CaveCarverConfiguration cfg, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomes, RandomSource rnd, Aquifer aquifer,
                         ChunkPos pos, CarvingMask mask) {
        double cx = pos.getBlockX(rnd.nextInt(16));
        double cz = pos.getBlockZ(rnd.nextInt(16));
        double cy = cfg.y.sample(rnd, ctx);
        double hr = cfg.horizontalRadiusMultiplier.sample(rnd);
        double vr = cfg.verticalRadiusMultiplier.sample(rnd);
        final double floorRel = -0.55;

        boolean any = false;
        int lobes = 4 + rnd.nextInt(4);
        for (int i = 0; i < lobes; i++) {
            double lx = cx, ly = cy, lz = cz, lh = hr, lv = vr;
            if (i > 0) {
                double ang = rnd.nextDouble() * Math.PI * 2;
                double off = (0.35 + rnd.nextDouble() * 0.5) * hr;
                lx += Math.cos(ang) * off;
                lz += Math.sin(ang) * off;
                ly += rnd.nextInt(5) - 2;
                lh = hr * (0.4 + rnd.nextDouble() * 0.4);
                lv = vr * (0.6 + rnd.nextDouble() * 0.35);
            }
            any |= this.carveEllipsoid(ctx, cfg, chunk, biomes, aquifer, lx, ly, lz, lh, lv, mask,
                    (c, relX, relY, relZ, y) -> relY <= floorRel);
        }
        return any;
    }
}
