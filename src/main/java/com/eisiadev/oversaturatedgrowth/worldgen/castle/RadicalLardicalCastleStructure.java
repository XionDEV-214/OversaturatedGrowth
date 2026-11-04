package com.eisiadev.oversaturatedgrowth.worldgen.castle;

import com.eisiadev.oversaturatedgrowth.init.ModStructures;
import com.eisiadev.oversaturatedgrowth.worldgen.GrottoRegion;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class RadicalLardicalCastleStructure extends Structure {
    public static final Codec<RadicalLardicalCastleStructure> CODEC = simpleCodec(RadicalLardicalCastleStructure::new);

    public RadicalLardicalCastleStructure(Structure.StructureSettings settings) { super(settings); }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        ChunkPos cp = ctx.chunkPos();
        int x = cp.getMiddleBlockX(), z = cp.getMiddleBlockZ();

        // Lardswept Grotto 바이옴이 발밑 지하에 있는 곳에서만 생성 (중심 + 사방 32칸 중 3곳 이상)
        if (!grottoBelow(ctx, x, z)) return Optional.empty();
        int around = 0;
        for (int[] o : new int[][]{{32, 0}, {-32, 0}, {0, 32}, {0, -32}}) if (grottoBelow(ctx, x + o[0], z + o[1])) around++;
        if (around < 3) return Optional.empty();

        int y = ctx.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
        BlockPos pos = new BlockPos(x, y, z);
        long seed = ctx.random().nextLong();
        CastleLayout.HeightFn hf = (hx, hz) -> ctx.chunkGenerator().getFirstOccupiedHeight(hx, hz, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
        return Optional.of(new GenerationStub(pos, b -> CastleLayout.generate(x, y, z, seed, hf, ctx.heightAccessor().getMinBuildHeight(), ctx.heightAccessor().getMaxBuildHeight()).forEach(b::addPiece)));
    }

    private static boolean grottoBelow(GenerationContext ctx, int x, int z) {
        for (int y = -56; y <= 40; y += 8) {
            if (ctx.biomeSource().getNoiseBiome(x >> 2, y >> 2, z >> 2, ctx.randomState().sampler()).is(GrottoRegion.LARDSLUDGE_GROTTO)) return true;
        }
        return false;
    }

    @Override
    public StructureType<?> type() { return ModStructures.RADICAL_LARDICAL_CASTLE.get(); }
}
