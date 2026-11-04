package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.blockentity.LardExtractorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, OversaturatedGrowth.MODID);

    public static final RegistryObject<BlockEntityType<LardExtractorBlockEntity>> LARD_EXTRACTOR =
            BLOCK_ENTITIES.register("lard_extractor",
                    () -> BlockEntityType.Builder
                            .of(LardExtractorBlockEntity::new,
                                    ModBlocks.LARD_EXTRACTOR.get())
                            .build(null));
}
