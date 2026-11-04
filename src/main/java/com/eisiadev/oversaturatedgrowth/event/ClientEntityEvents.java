package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.client.renderer.LardGlobRenderer;
import com.eisiadev.oversaturatedgrowth.client.renderer.LardSlimeRenderer;
import com.eisiadev.oversaturatedgrowth.client.renderer.LardRetcherRenderer;
import com.eisiadev.oversaturatedgrowth.client.renderer.MotherRetcherRenderer;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.client.model.*;
import com.eisiadev.oversaturatedgrowth.client.renderer.OGMobRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEntityEvents {

    @SubscribeEvent
    public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    com.eisiadev.oversaturatedgrowth.init.ModFluids.REFINED_LARD_STILL.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    com.eisiadev.oversaturatedgrowth.init.ModFluids.REFINED_LARD_FLOWING.get(), net.minecraft.client.renderer.RenderType.translucent());
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LARD_RETCHER.get(),        LardRetcherRenderer::new);
        event.registerEntityRenderer(ModEntities.MOTHER_RETCHER.get(), MotherRetcherRenderer::new);
        event.registerEntityRenderer(ModEntities.LARD_GLOB.get(),           LardGlobRenderer::new);
        event.registerEntityRenderer(ModEntities.LARD_SLIME.get(),          LardSlimeRenderer::new);
        event.registerEntityRenderer(ModEntities.LARD_BLOAT.get(),          OGMobRenderers.Bloat::new);
        event.registerEntityRenderer(ModEntities.LARD_DRIPPER.get(),        OGMobRenderers.Dripper::new);
        event.registerEntityRenderer(ModEntities.LARD_DRIP.get(),           ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.LARD_PUDDLE.get(),         net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.FATTADIUM_BEETLE.get(),    OGMobRenderers.Beetle::new);
        event.registerEntityRenderer(ModEntities.SHROOMLING.get(),          OGMobRenderers.Shroomling::new);
        event.registerEntityRenderer(ModEntities.LARDSTONE_ARCHER.get(),    OGMobRenderers.Archer::new);
        event.registerEntityRenderer(ModEntities.LARDSTONE_SHARD.get(),     ctx -> new ThrownItemRenderer<>(ctx, 1.6f, false));
        event.registerEntityRenderer(ModEntities.LARD_CRAWLER.get(),        OGMobRenderers.Crawler::new);
        event.registerEntityRenderer(ModEntities.MAW_CHEST.get(),           OGMobRenderers.MawChest::new);
        event.registerEntityRenderer(ModEntities.BUTLER.get(),              OGMobRenderers.Butler::new);
        event.registerEntityRenderer(ModEntities.CHEF.get(),                OGMobRenderers.Chef::new);
        event.registerEntityRenderer(ModEntities.GOURMAND_KNIGHT.get(),     OGMobRenderers.Knight::new);
        event.registerEntityRenderer(ModEntities.OVERLARD.get(),            OGMobRenderers.Overlard::new);
        event.registerEntityRenderer(ModEntities.LARD_BOMB.get(),           ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.LARD_POT.get(),            ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.LARDY_PIG.get(),           com.eisiadev.oversaturatedgrowth.client.renderer.GrottoRenderers.Pig::new);
        event.registerEntityRenderer(ModEntities.LARDY_COW.get(),           com.eisiadev.oversaturatedgrowth.client.renderer.GrottoRenderers.Cow::new);
        event.registerEntityRenderer(ModEntities.LARDY_SHEEP.get(),         com.eisiadev.oversaturatedgrowth.client.renderer.GrottoRenderers.Sheep::new);
        event.registerEntityRenderer(ModEntities.LARDY_CHICKEN.get(),       com.eisiadev.oversaturatedgrowth.client.renderer.GrottoRenderers.Chicken::new);
        event.registerEntityRenderer(ModEntities.LARDFIN.get(),             com.eisiadev.oversaturatedgrowth.client.renderer.GrottoRenderers.Lardfin::new);
        event.registerEntityRenderer(ModEntities.GLUTTON_TOAD.get(),        com.eisiadev.oversaturatedgrowth.client.renderer.GrottoRenderers.Toad::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OGMobModels.BLOAT, LardBloatModel::createLayer);
        event.registerLayerDefinition(OGMobModels.DRIPPER, LardDripperModel::createLayer);
        event.registerLayerDefinition(OGMobModels.BEETLE, FattadiumBeetleModel::createLayer);
        event.registerLayerDefinition(OGMobModels.SHROOMLING, ShroomlingModel::createLayer);
        event.registerLayerDefinition(OGMobModels.CRAWLER, LardCrawlerModel::createLayer);
        event.registerLayerDefinition(OGMobModels.MAW_CHEST, MawChestModel::createLayer);
        event.registerLayerDefinition(OGMobModels.KNIGHT, KnightModel::createLayer);
        event.registerLayerDefinition(OGMobModels.CHEF, ChefModel::createLayer);
        event.registerLayerDefinition(OGMobModels.OVERLARD, OverlardModel::createLayer);
    }
}
