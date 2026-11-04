package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.block.LardExtractorBlock;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID)
public class ExtractorEvents {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock e) {
        if (e.getHand() != InteractionHand.MAIN_HAND) return;
        if (!e.getEntity().isShiftKeyDown()) return;
        if (e.getLevel().getBlockState(e.getPos()).getBlock() instanceof LardExtractorBlock) {
            e.setUseBlock(Event.Result.ALLOW);
        }
    }
}
