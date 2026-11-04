package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.worldgen.LeafVeinDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTreeDecorators {
    public static final DeferredRegister<TreeDecoratorType<?>> TYPES =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, OversaturatedGrowth.MODID);

    public static final RegistryObject<TreeDecoratorType<LeafVeinDecorator>> LEAF_VEIN =
            TYPES.register("leaf_vein", () -> new TreeDecoratorType<>(LeafVeinDecorator.CODEC));
}
