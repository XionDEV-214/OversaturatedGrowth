package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RegistryRemap {

    private static final String OLD_NAMESPACE = "extragluttony";

    private RegistryRemap() {}

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        remap(event, ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS);
        remap(event, ForgeRegistries.Keys.BLOCKS, ForgeRegistries.BLOCKS);
        remap(event, ForgeRegistries.Keys.FLUIDS, ForgeRegistries.FLUIDS);
        remap(event, ForgeRegistries.Keys.ENTITY_TYPES, ForgeRegistries.ENTITY_TYPES);
        remap(event, ForgeRegistries.Keys.MOB_EFFECTS, ForgeRegistries.MOB_EFFECTS);
        remap(event, ForgeRegistries.Keys.POTIONS, ForgeRegistries.POTIONS);
        remap(event, ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, ForgeRegistries.BLOCK_ENTITY_TYPES);
    }

    private static <T> void remap(MissingMappingsEvent event, ResourceKey<? extends Registry<T>> key, IForgeRegistry<T> registry) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getMappings(key, OLD_NAMESPACE)) {
            ResourceLocation target = ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, newPath(mapping.getKey().getPath()));
            if (registry.containsKey(target)) {
                mapping.remap(registry.getValue(target));
            }
        }
    }

    static String newPath(String p) {
        return p.replace("lipo_vomiter", "lard_retcher")
                .replace("lipo_essence_extractor", "lard_extractor")
                .replace("lipo_essence", "refined_lard")
                .replace("lipo_sludge_ball", "lard_sludge_ball")
                .replace("lipo_slime", "lard_slime")
                .replace("lipo_glob", "lard_glob")
                .replace("lipo_weight", "weight_gaining");
    }
}
