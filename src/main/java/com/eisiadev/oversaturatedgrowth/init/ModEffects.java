package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, OversaturatedGrowth.MODID);

    public static final RegistryObject<MobEffect> WEIGHT_GAINING =
            MOB_EFFECTS.register("weight_gaining", WeightGainingEffect::new);
    public static final RegistryObject<MobEffect> WEIGHT_LOSING =
            MOB_EFFECTS.register("weight_losing", WeightLosingEffect::new);
    public static final RegistryObject<MobEffect> WEIGHTLESS =
            MOB_EFFECTS.register("weightless", WeightlessEffect::new);
    public static final RegistryObject<MobEffect> INCREASED_CAPACITY =
            MOB_EFFECTS.register("increased_capacity", IncreasedCapacityEffect::new);
    public static final RegistryObject<MobEffect> OVEREAT =
            MOB_EFFECTS.register("overeat", OvereatEffect::new);
    public static final RegistryObject<MobEffect> OVERSATURATED =
            MOB_EFFECTS.register("oversaturated", OversaturatedEffect::new);
    public static final RegistryObject<MobEffect> FATTABERRY_ATE =
            MOB_EFFECTS.register("fattaberry_ate", FattaberryAteEffect::new);
    public static final RegistryObject<MobEffect> FATTABERRY_STOMACH =
            MOB_EFFECTS.register("fattaberry_stomach", FattaberryStomachEffect::new);
}
