package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.init.ModStructures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class LardVaultStructure extends Structure {
    public static final Codec<LardVaultStructure> CODEC = simpleCodec(LardVaultStructure::new);

    public LardVaultStructure(Structure.StructureSettings settings) { super(settings); }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        ChunkPos cp = ctx.chunkPos();
        WorldgenRandom r = ctx.random();
        int x = cp.getMinBlockX() + 4 + r.nextInt(8);
        int z = cp.getMinBlockZ() + 4 + r.nextInt(8);
        int y = -26 + r.nextInt(22);
        BlockPos pos = new BlockPos(x, y, z);
        long seed = r.nextLong();
        return Optional.of(new GenerationStub(pos, b -> b.addPiece(new LardVaultPiece(pos, seed))));
    }

    @Override
    public StructureType<?> type() { return ModStructures.LARD_VAULT.get(); }
}
