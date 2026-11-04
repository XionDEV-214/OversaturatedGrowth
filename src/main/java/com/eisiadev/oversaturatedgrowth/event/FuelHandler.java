package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.init.ModItems;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FuelHandler {

    private static final int CALORIUM_BURN_TIME = 6400;

    @SubscribeEvent
    public static void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().getItem() == ModItems.REFINED_LARD.get()) {
            event.setBurnTime(CALORIUM_BURN_TIME);
        }
    }
}
