package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.worldgen.GreasySplatterFeature;
import com.eisiadev.oversaturatedgrowth.worldgen.GrottoFaunaFeature;
import com.eisiadev.oversaturatedgrowth.worldgen.LardCraterFeature;
import com.eisiadev.oversaturatedgrowth.worldgen.LardDrapeFeature;
import com.eisiadev.oversaturatedgrowth.worldgen.LardPoolFeature;
import com.eisiadev.oversaturatedgrowth.worldgen.WeatheringFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, OversaturatedGrowth.MODID);

    public static final RegistryObject<Feature<?>> WEATHERING =
            FEATURES.register("weathering", WeatheringFeature::new);

    public static final RegistryObject<Feature<?>> LARD_POOL =
            FEATURES.register("lard_pool", LardPoolFeature::new);

    public static final RegistryObject<Feature<?>> LARD_CRATER =
            FEATURES.register("lard_crater", LardCraterFeature::new);
    public static final RegistryObject<Feature<?>> LARD_DRAPE =
            FEATURES.register("lard_drape", LardDrapeFeature::new);
    public static final RegistryObject<Feature<?>> GREASY_SPLATTER =
            FEATURES.register("greasy_splatter", GreasySplatterFeature::new);
    public static final RegistryObject<Feature<?>> GROTTO_FAUNA =
            FEATURES.register("grotto_fauna", GrottoFaunaFeature::new);
}
