package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.entity.FattadiumBeetleEntity;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID)
public class MobEvents {

    @SubscribeEvent
    public static void onOreBroken(BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide() || !event.getState().is(ModBlocks.FATTADIUM_ORE.get())) return;
        var pos = event.getPos();
        var box = new net.minecraft.world.phys.AABB(pos).inflate(12);
        for (FattadiumBeetleEntity b : level.getEntitiesOfClass(FattadiumBeetleEntity.class, box)) {
            if (pos.equals(b.eatingPos)) {
                b.setTarget(event.getPlayer());
                b.eatingPos = null;
            }
        }
    }
}
