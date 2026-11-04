package com.eisiadev.oversaturatedgrowth.client;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModColorHandlers {

    private static final int LARD_COLOR   = com.eisiadev.oversaturatedgrowth.util.LardColors.LARD;
    private static final int SLUDGE_COLOR = com.eisiadev.oversaturatedgrowth.util.LardColors.SLUDGE;

    private ModColorHandlers() {}

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> LARD_COLOR, ModBlocks.LARD_BLOCK.get());
        event.register((state, level, pos, tint) -> SLUDGE_COLOR, ModBlocks.LARD_SLUDGE_BLOCK.get());
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> LARD_COLOR, ModBlocks.LARD_BLOCK.get());
        event.register((stack, tint) -> SLUDGE_COLOR, ModBlocks.LARD_SLUDGE_BLOCK.get());
        event.register(com.eisiadev.oversaturatedgrowth.accessory.AccessoryData::tint,
                com.eisiadev.oversaturatedgrowth.init.ModItems.ACCESSORY_AMULET.get(),
                com.eisiadev.oversaturatedgrowth.init.ModItems.ACCESSORY_BELT.get(),
                com.eisiadev.oversaturatedgrowth.init.ModItems.ACCESSORY_RING.get());
    }
}
