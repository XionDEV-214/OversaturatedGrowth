package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.accessory.RandomAccessoryFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootFunctions {

    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, OversaturatedGrowth.MODID);

    public static final RegistryObject<LootItemFunctionType> RANDOM_ACCESSORY = LOOT_FUNCTIONS.register("random_accessory",
            () -> new LootItemFunctionType(new RandomAccessoryFunction.Serializer()));

    private ModLootFunctions() {}
}
