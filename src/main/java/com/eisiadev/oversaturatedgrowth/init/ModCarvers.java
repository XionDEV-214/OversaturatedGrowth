package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.worldgen.GiantCavernCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModCarvers {
    public static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, OversaturatedGrowth.MODID);

    public static final RegistryObject<WorldCarver<?>> GIANT_CAVERN =
            CARVERS.register("giant_cavern", GiantCavernCarver::new);
}
