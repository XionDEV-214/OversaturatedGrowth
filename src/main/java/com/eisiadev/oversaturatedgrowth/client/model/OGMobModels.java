package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class OGMobModels {
    public static final ModelLayerLocation BLOAT = layer("lard_bloat");
    public static final ModelLayerLocation DRIPPER = layer("lard_dripper");
    public static final ModelLayerLocation BEETLE = layer("fattadium_beetle");
    public static final ModelLayerLocation SHROOMLING = layer("shroomling");
    public static final ModelLayerLocation CRAWLER = layer("lard_crawler");
    public static final ModelLayerLocation MAW_CHEST = layer("maw_chest");
    public static final ModelLayerLocation KNIGHT = layer("gourmand_knight");
    public static final ModelLayerLocation CHEF = layer("chef");
    public static final ModelLayerLocation OVERLARD = layer("overlard");

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, name), "main");
    }
}
