package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, OversaturatedGrowth.MODID);

    public static final RegistryObject<SoundEvent> BELCH = reg("belch");

    public static final RegistryObject<SoundEvent> RUMBLE = reg("rumble");

    public static final RegistryObject<SoundEvent> SLOSH = reg("slosh");

    public static final RegistryObject<SoundEvent> SWALLOW = reg("swallow");

    public static final RegistryObject<SoundEvent> DIGESTING = reg("digesting");

    public static final RegistryObject<SoundEvent> LARDSTONE_BREAK = reg("lardstone_break");
    public static final RegistryObject<SoundEvent> LARDSTONE_STEP = reg("lardstone_step");
    public static final RegistryObject<SoundEvent> LARDSTONE_PLACE = reg("lardstone_place");
    public static final RegistryObject<SoundEvent> LARDSTONE_HIT = reg("lardstone_hit");
    public static final RegistryObject<SoundEvent> LARDSTONE_FALL = reg("lardstone_fall");

    public static final net.minecraftforge.common.util.ForgeSoundType LARDSTONE_TYPE =
            new net.minecraftforge.common.util.ForgeSoundType(1.0f, 1.0f,
                    LARDSTONE_BREAK, LARDSTONE_STEP, LARDSTONE_PLACE, LARDSTONE_HIT, LARDSTONE_FALL);

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, name)));
    }
}
