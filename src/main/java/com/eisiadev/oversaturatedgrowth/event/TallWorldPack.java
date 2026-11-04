package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TallWorldPack {
    @SubscribeEvent
    public static void addPacks(AddPackFindersEvent e) {
        if (e.getPackType() != PackType.SERVER_DATA) return;
        Path path = ModList.get().getModFileById(OversaturatedGrowth.MODID).getFile().findResource("tall_overworld");
        Pack pack = Pack.readMetaAndCreate("oversaturatedgrowth_tall_overworld",
                Component.literal("OG: Tall Overworld (y 575)"), false,
                id -> new PathPackResources(id, path, false),
                PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
        if (pack != null) e.addRepositorySource(consumer -> consumer.accept(pack));
    }
}
