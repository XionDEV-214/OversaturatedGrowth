package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.entity.*;
import com.eisiadev.oversaturatedgrowth.entity.projectile.LardDripEntity;
import com.eisiadev.oversaturatedgrowth.entity.projectile.LardPotEntity;
import com.eisiadev.oversaturatedgrowth.entity.LardRetcherEntity;
import com.eisiadev.oversaturatedgrowth.entity.MotherRetcherEntity;
import com.eisiadev.oversaturatedgrowth.entity.projectile.LardGlobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OversaturatedGrowth.MODID);

    public static final RegistryObject<EntityType<LardPuddleEntity>> LARD_PUDDLE =
            ENTITY_TYPES.register("lard_puddle", () ->
                    EntityType.Builder.<LardPuddleEntity>of(LardPuddleEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f).clientTrackingRange(8).updateInterval(20)
                            .build("oversaturatedgrowth:entities/lard_puddle"));

    public static final RegistryObject<EntityType<ButlerEntity>> BUTLER =
            ENTITY_TYPES.register("butler", () ->
                    EntityType.Builder.<ButlerEntity>of(ButlerEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.99f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/butler"));

    public static final RegistryObject<EntityType<ChefEntity>> CHEF =
            ENTITY_TYPES.register("chef", () ->
                    EntityType.Builder.<ChefEntity>of(ChefEntity::new, MobCategory.MONSTER)
                            .sized(0.7f, 2.3f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/chef"));

    public static final RegistryObject<EntityType<GourmandKnightEntity>> GOURMAND_KNIGHT =
            ENTITY_TYPES.register("gourmand_knight", () ->
                    EntityType.Builder.<GourmandKnightEntity>of(GourmandKnightEntity::new, MobCategory.MONSTER)
                            .sized(1.0f, 2.5f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/gourmand_knight"));

    public static final RegistryObject<EntityType<OverlardEntity>> OVERLARD =
            ENTITY_TYPES.register("overlard", () ->
                    EntityType.Builder.<OverlardEntity>of(OverlardEntity::new, MobCategory.MONSTER)
                            .sized(1.8f, 1.9f).clientTrackingRange(12).fireImmune()
                            .build("oversaturatedgrowth:entities/overlard"));

    public static final RegistryObject<EntityType<com.eisiadev.oversaturatedgrowth.entity.projectile.LardBombEntity>> LARD_BOMB =
            ENTITY_TYPES.register("lard_bomb", () ->
                    EntityType.Builder.<com.eisiadev.oversaturatedgrowth.entity.projectile.LardBombEntity>of(
                                    com.eisiadev.oversaturatedgrowth.entity.projectile.LardBombEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f).clientTrackingRange(8).updateInterval(10)
                            .build("oversaturatedgrowth:entities/lard_bomb"));

    public static final RegistryObject<EntityType<LardPotEntity>> LARD_POT =
            ENTITY_TYPES.register("lard_pot", () ->
                    EntityType.Builder.<LardPotEntity>of(LardPotEntity::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f).clientTrackingRange(6).updateInterval(2)
                            .build("oversaturatedgrowth:entities/lard_pot"));

    public static final RegistryObject<EntityType<LardRetcherEntity>> LARD_RETCHER =
            ENTITY_TYPES.register("lard_retcher", () ->
                    EntityType.Builder.<LardRetcherEntity>of(LardRetcherEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lard_retcher"));

    public static final RegistryObject<EntityType<MotherRetcherEntity>> MOTHER_RETCHER =
            ENTITY_TYPES.register("mother_retcher", () ->
                    EntityType.Builder.<MotherRetcherEntity>of(MotherRetcherEntity::new, MobCategory.MONSTER)
                            .sized(0.6f * MotherRetcherEntity.MODEL_SCALE, 1.95f * MotherRetcherEntity.MODEL_SCALE)
                            .clientTrackingRange(12)
                            .build("oversaturatedgrowth:entities/mother_retcher"));

    public static final RegistryObject<EntityType<LardGlobEntity>> LARD_GLOB =
            ENTITY_TYPES.register("lard_glob", () ->
                    EntityType.Builder.<LardGlobEntity>of(LardGlobEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("oversaturatedgrowth:entities/lard_glob"));

    public static final RegistryObject<EntityType<LardSlimeEntity>> LARD_SLIME =
            ENTITY_TYPES.register("lard_slime", () ->
                    EntityType.Builder.<LardSlimeEntity>of(LardSlimeEntity::new, MobCategory.MONSTER)
                            .sized(4.08f, 4.08f)
                            .clientTrackingRange(10)
                            .build("oversaturatedgrowth:entities/lard_slime"));

    public static final RegistryObject<EntityType<LardBloatEntity>> LARD_BLOAT =
            ENTITY_TYPES.register("lard_bloat", () ->
                    EntityType.Builder.<LardBloatEntity>of(LardBloatEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 0.9f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lard_bloat"));

    public static final RegistryObject<EntityType<LardDripperEntity>> LARD_DRIPPER =
            ENTITY_TYPES.register("lard_dripper", () ->
                    EntityType.Builder.<LardDripperEntity>of(LardDripperEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 0.9f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lard_dripper"));

    public static final RegistryObject<EntityType<LardDripEntity>> LARD_DRIP =
            ENTITY_TYPES.register("lard_drip", () ->
                    EntityType.Builder.<LardDripEntity>of(LardDripEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f).clientTrackingRange(6).updateInterval(2)
                            .build("oversaturatedgrowth:entities/lard_drip"));

    public static final RegistryObject<EntityType<FattadiumBeetleEntity>> FATTADIUM_BEETLE =
            ENTITY_TYPES.register("fattadium_beetle", () ->
                    EntityType.Builder.<FattadiumBeetleEntity>of(FattadiumBeetleEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 0.6f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/fattadium_beetle"));

    public static final RegistryObject<EntityType<ShroomlingEntity>> SHROOMLING =
            ENTITY_TYPES.register("shroomling", () ->
                    EntityType.Builder.<ShroomlingEntity>of(ShroomlingEntity::new, MobCategory.CREATURE)
                            .sized(0.5f, 0.9f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/shroomling"));

    public static final RegistryObject<EntityType<LardstoneArcherEntity>> LARDSTONE_ARCHER =
            ENTITY_TYPES.register("lardstone_archer", () ->
                    EntityType.Builder.<LardstoneArcherEntity>of(LardstoneArcherEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.99f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lardstone_archer"));

    public static final RegistryObject<EntityType<com.eisiadev.oversaturatedgrowth.entity.projectile.LardShardEntity>> LARDSTONE_SHARD =
            ENTITY_TYPES.register("lardstone_shard", () ->
                    EntityType.Builder.<com.eisiadev.oversaturatedgrowth.entity.projectile.LardShardEntity>of(
                                    com.eisiadev.oversaturatedgrowth.entity.projectile.LardShardEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20)
                            .build("oversaturatedgrowth:entities/lardstone_shard"));

    public static final RegistryObject<EntityType<LardCrawlerEntity>> LARD_CRAWLER =
            ENTITY_TYPES.register("lard_crawler", () ->
                    EntityType.Builder.<LardCrawlerEntity>of(LardCrawlerEntity::new, MobCategory.MONSTER)
                            .sized(1.2f, 0.9f).clientTrackingRange(8).updateInterval(1)
                            .build("oversaturatedgrowth:entities/lard_crawler"));

    public static final RegistryObject<EntityType<MawChestEntity>> MAW_CHEST =
            ENTITY_TYPES.register("maw_chest", () ->
                    EntityType.Builder.<MawChestEntity>of(MawChestEntity::new, MobCategory.MONSTER)
                            .sized(0.95f, 0.95f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/maw_chest"));

    public static final RegistryObject<EntityType<LardyPigEntity>> LARDY_PIG =
            ENTITY_TYPES.register("lardy_pig", () ->
                    EntityType.Builder.<LardyPigEntity>of(LardyPigEntity::new, MobCategory.CREATURE)
                            .sized(0.9f, 0.9f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lardy_pig"));

    public static final RegistryObject<EntityType<LardyCowEntity>> LARDY_COW =
            ENTITY_TYPES.register("lardy_cow", () ->
                    EntityType.Builder.<LardyCowEntity>of(LardyCowEntity::new, MobCategory.CREATURE)
                            .sized(0.9f, 1.4f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lardy_cow"));

    public static final RegistryObject<EntityType<LardySheepEntity>> LARDY_SHEEP =
            ENTITY_TYPES.register("lardy_sheep", () ->
                    EntityType.Builder.<LardySheepEntity>of(LardySheepEntity::new, MobCategory.CREATURE)
                            .sized(0.9f, 1.3f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lardy_sheep"));

    public static final RegistryObject<EntityType<LardyChickenEntity>> LARDY_CHICKEN =
            ENTITY_TYPES.register("lardy_chicken", () ->
                    EntityType.Builder.<LardyChickenEntity>of(LardyChickenEntity::new, MobCategory.CREATURE)
                            .sized(0.4f, 0.7f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lardy_chicken"));

    public static final RegistryObject<EntityType<LardfinEntity>> LARDFIN =
            ENTITY_TYPES.register("lardfin", () ->
                    EntityType.Builder.<LardfinEntity>of(LardfinEntity::new, MobCategory.WATER_AMBIENT)
                            .sized(0.5f, 0.4f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/lardfin"));

    public static final RegistryObject<EntityType<GluttonToadEntity>> GLUTTON_TOAD =
            ENTITY_TYPES.register("glutton_toad", () ->
                    EntityType.Builder.<GluttonToadEntity>of(GluttonToadEntity::new, MobCategory.CREATURE)
                            .sized(0.7f, 0.5f).clientTrackingRange(8)
                            .build("oversaturatedgrowth:entities/glutton_toad"));
}
