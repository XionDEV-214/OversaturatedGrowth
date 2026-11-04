package com.eisiadev.oversaturatedgrowth;

import com.eisiadev.oversaturatedgrowth.config.ModClientConfig;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.event.EntityConsumeHandler;
import com.eisiadev.oversaturatedgrowth.event.PlayerConsumeHandler;
import com.eisiadev.oversaturatedgrowth.init.*;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import com.eisiadev.oversaturatedgrowth.compat.gregtech.GregTechCompat;

@Mod(OversaturatedGrowth.MODID)
public class OversaturatedGrowth {

    public static final String MODID = "oversaturatedgrowth";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OversaturatedGrowth(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModFluids.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);
        ModTreeDecorators.TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        com.eisiadev.oversaturatedgrowth.init.ModFeatures.FEATURES.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        com.eisiadev.oversaturatedgrowth.init.ModCarvers.CARVERS.register(modEventBus);
        com.eisiadev.oversaturatedgrowth.init.ModStructures.TYPES.register(modEventBus);
        com.eisiadev.oversaturatedgrowth.init.ModStructures.PIECES.register(modEventBus);

        com.eisiadev.oversaturatedgrowth.init.ModLootFunctions.LOOT_FUNCTIONS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        context.registerConfig(
                ModConfig.Type.CLIENT,
                ModClientConfig.SPEC,
                "oversaturatedgrowth-client.toml");

        context.registerConfig(
                ModConfig.Type.SERVER,
                ModServerConfig.SPEC,
                "oversaturatedgrowth-server.toml");

        context.registerConfig(
                ModConfig.Type.SERVER,
                com.eisiadev.oversaturatedgrowth.accessory.AccessoryConfig.SPEC,
                "oversaturatedgrowth-accessories.toml");

        if (GregTechCompat.isLoaded()) GregTechCompat.preInit();

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Oversaturated Growth loaded.");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
            com.eisiadev.oversaturatedgrowth.accessory.gui.AccessoryUIFactory.register();
            LOGGER.info("Optional deps - GregTechCEu: {}", GregTechCompat.isLoaded());
            if (GregTechCompat.isLoaded()) GregTechCompat.init();
            registerBrewingRecipes();
            com.eisiadev.oversaturatedgrowth.worldgen.OGWorldgen.register();
        });
    }

    private static void registerBrewingRecipes() {
        var sludgeBall = ModItems.LARD_SLUDGE_BALL.get();
        var essence    = ModItems.REFINED_LARD.get();
        var glowstone  = Items.GLOWSTONE_DUST;

        addPotionRecipe(Potions.AWKWARD,                  sludgeBall, ModPotions.WEIGHT_GAINING_1.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_1.get(),   glowstone,  ModPotions.WEIGHT_GAINING_2.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_2.get(),   sludgeBall, ModPotions.WEIGHT_GAINING_3.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_3.get(),   glowstone,  ModPotions.WEIGHT_GAINING_4.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_4.get(),   sludgeBall, ModPotions.WEIGHT_GAINING_5.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_5.get(),   glowstone,  ModPotions.WEIGHT_GAINING_6.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_6.get(),   essence,    ModPotions.WEIGHT_GAINING_7.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_7.get(),   glowstone,  ModPotions.WEIGHT_GAINING_8.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_8.get(),   essence,    ModPotions.WEIGHT_GAINING_9.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_9.get(),   glowstone,  ModPotions.WEIGHT_GAINING_10.get());

        var spiderEye = Items.FERMENTED_SPIDER_EYE;
        addPotionRecipe(ModPotions.WEIGHT_GAINING_1.get(), spiderEye, ModPotions.WEIGHT_LOSING_1.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_2.get(), spiderEye, ModPotions.WEIGHT_LOSING_2.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_3.get(), spiderEye, ModPotions.WEIGHT_LOSING_3.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_4.get(), spiderEye, ModPotions.WEIGHT_LOSING_4.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_5.get(), spiderEye, ModPotions.WEIGHT_LOSING_5.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_6.get(), spiderEye, ModPotions.WEIGHT_LOSING_6.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_7.get(), spiderEye, ModPotions.WEIGHT_LOSING_7.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_8.get(), spiderEye, ModPotions.WEIGHT_LOSING_8.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_9.get(), spiderEye, ModPotions.WEIGHT_LOSING_9.get());
        addPotionRecipe(ModPotions.WEIGHT_GAINING_10.get(), spiderEye, ModPotions.WEIGHT_LOSING_10.get());

        var crystal = ModItems.CRYSTALLIZED_FATTABERRY.get();
        var glowBlock = Items.GLOWSTONE;
        addPotionRecipe(Potions.AWKWARD, crystal, ModPotions.INCREASED_CAPACITY_1.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_1.get(), crystal, ModPotions.INCREASED_CAPACITY_2.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_2.get(), crystal, ModPotions.INCREASED_CAPACITY_3.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_3.get(), glowstone, ModPotions.INCREASED_CAPACITY_4.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_4.get(), glowstone, ModPotions.INCREASED_CAPACITY_5.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_5.get(), glowstone, ModPotions.INCREASED_CAPACITY_6.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_6.get(), glowBlock, ModPotions.INCREASED_CAPACITY_7.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_7.get(), glowBlock, ModPotions.INCREASED_CAPACITY_8.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_8.get(), glowBlock, ModPotions.INCREASED_CAPACITY_9.get());
        addPotionRecipe(ModPotions.INCREASED_CAPACITY_9.get(), glowBlock, ModPotions.INCREASED_CAPACITY_10.get());

        addPotionRecipe(Potions.AWKWARD, Items.FEATHER, ModPotions.WEIGHTLESS.get());
        addPotionRecipe(ModPotions.WEIGHTLESS.get(),      Items.REDSTONE,       ModPotions.WEIGHTLESS_LONG.get());
        addPotionRecipe(ModPotions.WEIGHTLESS_LONG.get(), Items.REDSTONE_BLOCK, ModPotions.WEIGHTLESS_LONGER.get());
    }

    private static void addPotionRecipe(Potion input, net.minecraft.world.item.Item ingredient, Potion output) {
        ItemStack outputStack = PotionUtils.setPotion(new ItemStack(Items.POTION), output);
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), input)),
                Ingredient.of(ingredient),
                outputStack
        );
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        long tick = server.getTickCount();
        Iterable<net.minecraft.server.level.ServerLevel> levels = server.getAllLevels();
        EntityConsumeHandler.tickServer(tick, levels);
        PlayerConsumeHandler.tickServer(tick, levels);
        com.eisiadev.oversaturatedgrowth.consume.block.BlockConsumeHandler.tickServer(tick, levels);
    }
}
