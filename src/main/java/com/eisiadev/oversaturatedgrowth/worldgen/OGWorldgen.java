package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

public final class OGWorldgen {
    private OGWorldgen() {}

    public static final int REGION_WEIGHT = 4;

    public static void register() {
        Regions.register(new CavernRegion(
                ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "overworld_cavern"), REGION_WEIGHT));

        Regions.register(new GrottoRegion(
                ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "overworld_grotto"), REGION_WEIGHT));

        SurfaceRules.RuleSource lardstone = SurfaceRules.state(ModBlocks.LARDSTONE.get().defaultBlockState());
        SurfaceRules.RuleSource rules = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(CavernRegion.LARDSTONE_CAVERN, GrottoRegion.LARDSLUDGE_GROTTO),
                SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.aboveBottom(6), 0), lardstone));
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, OversaturatedGrowth.MODID, rules);
    }
}
