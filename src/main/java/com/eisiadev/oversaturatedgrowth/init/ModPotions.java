package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModPotions {

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, OversaturatedGrowth.MODID);

    private static final int DUR_1MIN  = 1200;
    private static final int DUR_3MIN  = 3600;
    private static final int DUR_8MIN  = 9600;
    private static final int DUR_24MIN = 28800;

    private static RegistryObject<Potion> pot(String name, Supplier<MobEffect> effect, int duration, int amp) {
        return POTIONS.register(name, () -> new Potion(name, new MobEffectInstance(effect.get(), duration, amp)));
    }

    public static final RegistryObject<Potion> WEIGHT_GAINING_1 = pot("weight_gaining_1", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 0);
    public static final RegistryObject<Potion> WEIGHT_GAINING_2 = pot("weight_gaining_2", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 1);
    public static final RegistryObject<Potion> WEIGHT_GAINING_3 = pot("weight_gaining_3", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 2);
    public static final RegistryObject<Potion> WEIGHT_GAINING_4 = pot("weight_gaining_4", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 3);
    public static final RegistryObject<Potion> WEIGHT_GAINING_5 = pot("weight_gaining_5", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 4);
    public static final RegistryObject<Potion> WEIGHT_GAINING_6 = pot("weight_gaining_6", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 5);
    public static final RegistryObject<Potion> WEIGHT_GAINING_7 = pot("weight_gaining_7", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 6);
    public static final RegistryObject<Potion> WEIGHT_GAINING_8 = pot("weight_gaining_8", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 7);
    public static final RegistryObject<Potion> WEIGHT_GAINING_9 = pot("weight_gaining_9", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 8);
    public static final RegistryObject<Potion> WEIGHT_GAINING_10 = pot("weight_gaining_10", ModEffects.WEIGHT_GAINING::get, DUR_1MIN, 9);

    public static final RegistryObject<Potion> WEIGHT_LOSING_1 = pot("weight_losing_1", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 0);
    public static final RegistryObject<Potion> WEIGHT_LOSING_2 = pot("weight_losing_2", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 1);
    public static final RegistryObject<Potion> WEIGHT_LOSING_3 = pot("weight_losing_3", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 2);
    public static final RegistryObject<Potion> WEIGHT_LOSING_4 = pot("weight_losing_4", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 3);
    public static final RegistryObject<Potion> WEIGHT_LOSING_5 = pot("weight_losing_5", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 4);
    public static final RegistryObject<Potion> WEIGHT_LOSING_6 = pot("weight_losing_6", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 5);
    public static final RegistryObject<Potion> WEIGHT_LOSING_7 = pot("weight_losing_7", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 6);
    public static final RegistryObject<Potion> WEIGHT_LOSING_8 = pot("weight_losing_8", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 7);
    public static final RegistryObject<Potion> WEIGHT_LOSING_9 = pot("weight_losing_9", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 8);
    public static final RegistryObject<Potion> WEIGHT_LOSING_10 = pot("weight_losing_10", ModEffects.WEIGHT_LOSING::get, DUR_1MIN, 9);

    public static final RegistryObject<Potion> INCREASED_CAPACITY_1 = pot("increased_capacity_1", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 0);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_2 = pot("increased_capacity_2", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 1);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_3 = pot("increased_capacity_3", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 2);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_4 = pot("increased_capacity_4", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 3);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_5 = pot("increased_capacity_5", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 4);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_6 = pot("increased_capacity_6", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 5);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_7 = pot("increased_capacity_7", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 6);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_8 = pot("increased_capacity_8", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 7);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_9 = pot("increased_capacity_9", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 8);
    public static final RegistryObject<Potion> INCREASED_CAPACITY_10 = pot("increased_capacity_10", ModEffects.INCREASED_CAPACITY::get, DUR_3MIN, 9);

    public static final RegistryObject<Potion> WEIGHTLESS        = pot("weightless",        ModEffects.WEIGHTLESS::get, DUR_3MIN,  0);
    public static final RegistryObject<Potion> WEIGHTLESS_LONG   = pot("weightless_long",   ModEffects.WEIGHTLESS::get, DUR_8MIN,  0);
    public static final RegistryObject<Potion> WEIGHTLESS_LONGER = pot("weightless_longer", ModEffects.WEIGHTLESS::get, DUR_24MIN, 0);
}
