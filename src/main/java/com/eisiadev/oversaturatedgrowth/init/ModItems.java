package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraftforge.common.ForgeSpawnEggItem;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryBoxItem;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryItem;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OversaturatedGrowth.MODID);

    public static final RegistryObject<Item> REFINED_LARD = ITEMS.register("refined_lard",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(17)
                            .saturationMod(2f)
                            .effect(() -> new MobEffectInstance(ModEffects.WEIGHT_GAINING.get(), 200, 1), 1.0f)
                            .build())));

    public static final RegistryObject<Item> LARD_SLUDGE_BALL = ITEMS.register("lard_sludge_ball",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(6)
                            .saturationMod(0.6f)
                            .effect(() -> new MobEffectInstance(ModEffects.WEIGHT_GAINING.get(), 200, 0), 1.0f)
                            .build())));

    public static final RegistryObject<Item> LARD_RETCHER_SPAWN_EGG =
            ITEMS.register("lard_retcher_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARD_RETCHER,
                            0xD4B44A, 0x5C3A1A, new Item.Properties()));

    public static final RegistryObject<Item> LARD_BLOAT_SPAWN_EGG =
            ITEMS.register("lard_bloat_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARD_BLOAT,
                            0xF2C27A, 0xB8741A, new Item.Properties()));

    public static final RegistryObject<Item> LARD_DRIPPER_SPAWN_EGG =
            ITEMS.register("lard_dripper_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARD_DRIPPER,
                            0xE0A040, 0x6B3E10, new Item.Properties()));

    public static final RegistryObject<Item> FATTADIUM_BEETLE_SPAWN_EGG =
            ITEMS.register("fattadium_beetle_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.FATTADIUM_BEETLE,
                            0x6B4A2A, 0xEBBE3C, new Item.Properties()));

    public static final RegistryObject<Item> SHROOMLING_SPAWN_EGG =
            ITEMS.register("shroomling_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.SHROOMLING,
                            0xF59A22, 0xFAEBC8, new Item.Properties()));

    public static final RegistryObject<Item> OVERLARD_SPAWN_EGG =
            ITEMS.register("overlard_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.OVERLARD,
                            0xD9B66A, 0x6E1F2E, new Item.Properties()));

    public static final RegistryObject<Item> BUTLER_SPAWN_EGG =
            ITEMS.register("butler_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.BUTLER,
                            0x1E1E1E, 0xF59A22, new Item.Properties()));

    public static final RegistryObject<Item> CHEF_SPAWN_EGG =
            ITEMS.register("chef_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.CHEF,
                            0xF5F0E0, 0xF59A22, new Item.Properties()));

    public static final RegistryObject<Item> GOURMAND_KNIGHT_SPAWN_EGG =
            ITEMS.register("gourmand_knight_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.GOURMAND_KNIGHT,
                            0x8C8C8C, 0xF59A22, new Item.Properties()));

    public static final RegistryObject<Item> LARDSTONE_ARCHER_SPAWN_EGG =
            ITEMS.register("lardstone_archer_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARDSTONE_ARCHER,
                            0xC9B79C, 0xF59A22, new Item.Properties()));

    public static final RegistryObject<Item> LARD_CRAWLER_SPAWN_EGG =
            ITEMS.register("lard_crawler_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARD_CRAWLER,
                            0x6B4A2A, 0xF59A22, new Item.Properties()));

    public static final RegistryObject<Item> MAW_CHEST_SPAWN_EGG =
            ITEMS.register("maw_chest_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.MAW_CHEST,
                            0x8B5A2B, 0xE03A3A, new Item.Properties()));

    public static final RegistryObject<Item> LARDY_PIG_SPAWN_EGG =
            ITEMS.register("lardy_pig_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARDY_PIG,
                            0xE8B070, 0xF59A22, new Item.Properties()));

    public static final RegistryObject<Item> LARDY_COW_SPAWN_EGG =
            ITEMS.register("lardy_cow_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARDY_COW,
                            0x6B4A2A, 0xF0D080, new Item.Properties()));

    public static final RegistryObject<Item> LARDY_SHEEP_SPAWN_EGG =
            ITEMS.register("lardy_sheep_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARDY_SHEEP,
                            0xF0B050, 0x8B5A2B, new Item.Properties()));

    public static final RegistryObject<Item> LARDY_CHICKEN_SPAWN_EGG =
            ITEMS.register("lardy_chicken_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARDY_CHICKEN,
                            0xF0E0A0, 0xE03A3A, new Item.Properties()));

    public static final RegistryObject<Item> LARDFIN_SPAWN_EGG =
            ITEMS.register("lardfin_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARDFIN,
                            0xC8902A, 0xFFE090, new Item.Properties()));

    public static final RegistryObject<Item> GLUTTON_TOAD_SPAWN_EGG =
            ITEMS.register("glutton_toad_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.GLUTTON_TOAD,
                            0x6E7A2A, 0xE8A030, new Item.Properties()));

    public static final RegistryObject<Item> MOTHER_RETCHER_SPAWN_EGG =
            ITEMS.register("mother_retcher_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.MOTHER_RETCHER,
                            0xC8A030, 0x3A1E08, new Item.Properties()));

    public static final RegistryObject<Item> LARD_SLIME_SPAWN_EGG =
            ITEMS.register("lard_slime_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.LARD_SLIME,
                            0xE8D44D, 0xA89020, new Item.Properties()));

    public static final List<RegistryObject<Item>> CONTENT_ITEMS = new ArrayList<>();

    private static RegistryObject<Item> content(String name, java.util.function.Supplier<Item> s) {
        RegistryObject<Item> o = ITEMS.register(name, s);
        CONTENT_ITEMS.add(o);
        return o;
    }

    public static final RegistryObject<Item> RAW_LARDSOAKED_MEAT = content("raw_lardsoaked_meat",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(0.4f).meat().build())));
    public static final RegistryObject<Item> COOKED_LARDSOAKED_MEAT = content("cooked_lardsoaked_meat",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(10).saturationMod(0.9f).meat().build())));

    public static final RegistryObject<Item> INERT_FATTABERRY = content("inert_fattaberry", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FATTABERRY = content("fattaberry",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).alwaysEat()
                    .build())));
    public static final RegistryObject<Item> CRYSTALLIZED_FATTABERRY = content("crystallized_fattaberry",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).alwaysEat().fast()
                    .build())));

    public static final RegistryObject<Item> UNREFINED_FATTADIUM = content("unrefined_fattadium", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FATTADIUM = content("fattadium", () -> new Item(new Item.Properties()));

    // ---- Accessories (dynamic stats; see accessory/ package) ----
    public static final RegistryObject<Item> ACCESSORY_AMULET = ITEMS.register(AccessoryType.AMULET.itemName(),
            () -> new AccessoryItem(AccessoryType.AMULET));
    public static final RegistryObject<Item> ACCESSORY_BELT = ITEMS.register(AccessoryType.BELT.itemName(),
            () -> new AccessoryItem(AccessoryType.BELT));
    public static final RegistryObject<Item> ACCESSORY_RING = ITEMS.register(AccessoryType.RING.itemName(),
            () -> new AccessoryItem(AccessoryType.RING));
    public static final RegistryObject<Item> ACCESSORY_BOX = ITEMS.register("accessory_box",
            () -> new AccessoryBoxItem());

    public static Item accessory(AccessoryType type) {
        return switch (type) {
            case AMULET -> ACCESSORY_AMULET.get();
            case BELT -> ACCESSORY_BELT.get();
            case RING -> ACCESSORY_RING.get();
        };
    }
}
