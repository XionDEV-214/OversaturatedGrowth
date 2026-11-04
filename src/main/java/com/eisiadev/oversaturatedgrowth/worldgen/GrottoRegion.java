package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

import static terrablender.api.ParameterUtils.*;

public class GrottoRegion extends Region {

    public static final ResourceKey<Biome> LARDSLUDGE_GROTTO = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "lardsludge_grotto"));

    public GrottoRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();
        new ParameterPointListBuilder()
                .temperature(Temperature.FULL_RANGE)
                .humidity(Humidity.WET, Humidity.HUMID)
                .continentalness(Continentalness.MID_INLAND, Continentalness.FAR_INLAND)
                .erosion(Erosion.FULL_RANGE)
                .depth(Climate.Parameter.span(0.4F, 1.0F))
                .weirdness(Weirdness.FULL_RANGE)
                .build().forEach(point -> builder.add(point, LARDSLUDGE_GROTTO));
        builder.build().forEach(mapper);
    }
}
