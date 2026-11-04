package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OversaturatedGrowth.MODID);

    public static final RegistryObject<CreativeModeTab> EXTRA_GLUTTONY_TAB = TABS.register("extra_gluttony_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.oversaturatedgrowth"))
                    .icon(() -> ModItems.REFINED_LARD.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.REFINED_LARD.get());
                        output.accept(ModItems.LARD_SLUDGE_BALL.get());
                        if (!com.eisiadev.oversaturatedgrowth.compat.gregtech.GregTechCompat.isLoaded()) {
                            output.accept(ModBlocks.LARD_EXTRACTOR_ITEM.get());
                        } else {
                            var controller = com.eisiadev.oversaturatedgrowth.compat.gregtech.GregTechCompat.controllerStack();
                            if (!controller.isEmpty()) output.accept(controller);
                        }
                        ModBlocks.BLOCK_ITEMS.forEach(i -> output.accept(i.get()));
                        ModItems.CONTENT_ITEMS.forEach(i -> output.accept(i.get()));
                        for (var type : com.eisiadev.oversaturatedgrowth.accessory.AccessoryType.values()) {
                            for (var tier : com.eisiadev.oversaturatedgrowth.accessory.AccessoryTier.values()) {
                                output.accept(com.eisiadev.oversaturatedgrowth.accessory.AccessoryData.newStack(type,
                                        com.eisiadev.oversaturatedgrowth.accessory.AccessoryData.sample(type, tier)));
                            }
                        }
                        output.accept(ModItems.LARD_RETCHER_SPAWN_EGG.get());
                        output.accept(ModItems.MOTHER_RETCHER_SPAWN_EGG.get());
                        output.accept(ModItems.LARD_SLIME_SPAWN_EGG.get());
                        output.accept(ModItems.LARD_BLOAT_SPAWN_EGG.get());
                        output.accept(ModItems.LARD_DRIPPER_SPAWN_EGG.get());
                        output.accept(ModItems.FATTADIUM_BEETLE_SPAWN_EGG.get());
                        output.accept(ModItems.SHROOMLING_SPAWN_EGG.get());
                        output.accept(ModItems.LARDSTONE_ARCHER_SPAWN_EGG.get());
                        output.accept(ModItems.OVERLARD_SPAWN_EGG.get());
                        output.accept(ModItems.BUTLER_SPAWN_EGG.get());
                        output.accept(ModItems.CHEF_SPAWN_EGG.get());
                        output.accept(ModItems.GOURMAND_KNIGHT_SPAWN_EGG.get());
                        output.accept(ModItems.LARD_CRAWLER_SPAWN_EGG.get());
                        output.accept(ModItems.MAW_CHEST_SPAWN_EGG.get());
                        output.accept(ModItems.LARDY_PIG_SPAWN_EGG.get());
                        output.accept(ModItems.LARDY_COW_SPAWN_EGG.get());
                        output.accept(ModItems.LARDY_SHEEP_SPAWN_EGG.get());
                        output.accept(ModItems.LARDY_CHICKEN_SPAWN_EGG.get());
                        output.accept(ModItems.LARDFIN_SPAWN_EGG.get());
                        output.accept(ModItems.GLUTTON_TOAD_SPAWN_EGG.get());
                    })
                    .build());
}
