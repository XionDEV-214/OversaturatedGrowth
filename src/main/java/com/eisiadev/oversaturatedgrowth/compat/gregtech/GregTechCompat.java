package com.eisiadev.oversaturatedgrowth.compat.gregtech;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraftforge.fml.ModList;

public final class GregTechCompat {

    public static final String MODID = "gtceu";

    private GregTechCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    public static void preInit() {
        com.eisiadev.oversaturatedgrowth.compat.gregtech.gt.OGGTAddon.registerRegistrate();
        OversaturatedGrowth.LOGGER.info("GregTechCEu Modern detected - registering GT addon content.");
    }

    public static net.minecraft.world.item.ItemStack controllerStack() {
        return com.eisiadev.oversaturatedgrowth.compat.gregtech.gt.OGGTAddon.controllerStack();
    }

    public static void init() {
        OversaturatedGrowth.LOGGER.info("GregTech compat ready.");
    }
}
