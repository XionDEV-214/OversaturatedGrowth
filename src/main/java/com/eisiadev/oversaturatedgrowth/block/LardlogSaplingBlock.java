package com.eisiadev.oversaturatedgrowth.block;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import javax.annotation.Nullable;

public class LardlogSaplingBlock extends SaplingBlock {
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "lardlog_tree"));

    public LardlogSaplingBlock(Properties props) {
        super(new AbstractTreeGrower() {
            @Nullable
            @Override
            protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
                return TREE;
            }
        }, props);
    }
}
