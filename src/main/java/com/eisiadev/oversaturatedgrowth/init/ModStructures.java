package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.worldgen.LardVaultPiece;
import com.eisiadev.oversaturatedgrowth.worldgen.LardVaultStructure;
import com.eisiadev.oversaturatedgrowth.worldgen.castle.CastlePiece;
import com.eisiadev.oversaturatedgrowth.worldgen.castle.RadicalLardicalCastleStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, OversaturatedGrowth.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, OversaturatedGrowth.MODID);

    public static final RegistryObject<StructureType<LardVaultStructure>> LARD_VAULT =
            TYPES.register("lard_vault", () -> () -> LardVaultStructure.CODEC);
    public static final RegistryObject<StructurePieceType> LARD_VAULT_PIECE =
            PIECES.register("lard_vault", () -> (StructurePieceType) LardVaultPiece::new);

    public static final RegistryObject<StructureType<RadicalLardicalCastleStructure>> RADICAL_LARDICAL_CASTLE =
            TYPES.register("radical_lardical_castle", () -> () -> RadicalLardicalCastleStructure.CODEC);
    public static final RegistryObject<StructurePieceType> CASTLE_PIECE =
            PIECES.register("castle", () -> (StructurePieceType) CastlePiece::new);
}
