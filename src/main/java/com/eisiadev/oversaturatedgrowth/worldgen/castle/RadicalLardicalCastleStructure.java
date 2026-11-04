package com.eisiadev.oversaturatedgrowth.worldgen.castle;

import com.eisiadev.oversaturatedgrowth.init.ModStructures;
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

        int y = ctx.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
        BlockPos pos = new BlockPos(x, y, z);
        long seed = ctx.random().nextLong();
        CastleLayout.HeightFn hf = (hx, hz) -> ctx.chunkGenerator().getFirstOccupiedHeight(hx, hz, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
        return Optional.of(new GenerationStub(pos, b -> CastleLayout.generate(x, y, z, seed, hf, ctx.heightAccessor().getMinBuildHeight(), ctx.heightAccessor().getMaxBuildHeight()).forEach(b::addPiece)));
    }

    @Override
    public StructureType<?> type() { return ModStructures.RADICAL_LARDICAL_CASTLE.get(); }
}
