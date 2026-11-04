package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.entity.*;
import com.eisiadev.oversaturatedgrowth.entity.LardRetcherEntity;
import com.eisiadev.oversaturatedgrowth.entity.MotherRetcherEntity;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityEvents {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.OVERLARD.get(),            OverlardEntity.createAttributes().build());
        event.put(ModEntities.BUTLER.get(),              ButlerEntity.createAttributes().build());
        event.put(ModEntities.CHEF.get(),                ChefEntity.createAttributes().build());
        event.put(ModEntities.GOURMAND_KNIGHT.get(),     GourmandKnightEntity.createAttributes().build());
        event.put(ModEntities.LARD_RETCHER.get(),        LardRetcherEntity.createAttributes().build());
        event.put(ModEntities.MOTHER_RETCHER.get(), MotherRetcherEntity.createAttributes().build());
        event.put(ModEntities.LARD_SLIME.get(),          LardSlimeEntity.createAttributes().build());
        event.put(ModEntities.LARD_BLOAT.get(),          LardBloatEntity.createAttributes().build());
        event.put(ModEntities.LARD_DRIPPER.get(),        LardDripperEntity.createAttributes().build());
        event.put(ModEntities.FATTADIUM_BEETLE.get(),    FattadiumBeetleEntity.createAttributes().build());
        event.put(ModEntities.SHROOMLING.get(),          ShroomlingEntity.createAttributes().build());
        event.put(ModEntities.LARDSTONE_ARCHER.get(),    LardstoneArcherEntity.createAttributes().build());
        event.put(ModEntities.LARD_CRAWLER.get(),        LardCrawlerEntity.createAttributes().build());
        event.put(ModEntities.MAW_CHEST.get(),           MawChestEntity.createAttributes().build());
        event.put(ModEntities.LARDY_PIG.get(),           net.minecraft.world.entity.animal.Pig.createAttributes().build());
        event.put(ModEntities.LARDY_COW.get(),           net.minecraft.world.entity.animal.Cow.createAttributes().build());
        event.put(ModEntities.LARDY_SHEEP.get(),         net.minecraft.world.entity.animal.Sheep.createAttributes().build());
        event.put(ModEntities.LARDY_CHICKEN.get(),       net.minecraft.world.entity.animal.Chicken.createAttributes().build());
        event.put(ModEntities.LARDFIN.get(),             LardfinEntity.createAttributes().build());
        event.put(ModEntities.GLUTTON_TOAD.get(),        GluttonToadEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacement(SpawnPlacementRegisterEvent event) {

        castleMob(event, ModEntities.BUTLER.get());
        castleMob(event, ModEntities.CHEF.get());
        castleMob(event, ModEntities.GOURMAND_KNIGHT.get());
        event.register(ModEntities.LARD_BLOAT.get(), SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> lvl.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && lvl.getBlockState(pos).isAir() && rnd.nextInt(4) == 0,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.LARD_DRIPPER.get(), SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> lvl.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && lvl.getBlockState(pos).isAir()
                        && lvl.getBlockState(pos.above()).isFaceSturdy(lvl, pos.above(), net.minecraft.core.Direction.DOWN),
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.FATTADIUM_BEETLE.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> lvl.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && lvl.getBlockState(pos.below()).isFaceSturdy(lvl, pos.below(), net.minecraft.core.Direction.UP),
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.LARDSTONE_ARCHER.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> lvl.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && lvl.getBlockState(pos.below()).isFaceSturdy(lvl, pos.below(), net.minecraft.core.Direction.UP),
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.LARD_CRAWLER.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> lvl.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && lvl.getBlockState(pos.below()).isFaceSturdy(lvl, pos.below(), net.minecraft.core.Direction.UP),
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.MAW_CHEST.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> lvl.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && lvl.getBlockState(pos.below()).isFaceSturdy(lvl, pos.below(), net.minecraft.core.Direction.UP),
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.SHROOMLING.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> lvl.getBlockState(pos.below()).isFaceSturdy(lvl, pos.below(), net.minecraft.core.Direction.UP),
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        registerGrottoGround(event, ModEntities.LARDY_PIG.get());
        registerGrottoGround(event, ModEntities.LARDY_COW.get());
        registerGrottoGround(event, ModEntities.LARDY_SHEEP.get());
        registerGrottoGround(event, ModEntities.LARDY_CHICKEN.get());
        event.register(ModEntities.GLUTTON_TOAD.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> GluttonToadEntity.isInCrater(lvl, pos),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.LARDFIN.get(), SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> lvl.getFluidState(pos).getFluidType()
                        == com.eisiadev.oversaturatedgrowth.init.ModFluids.REFINED_LARD_TYPE.get()
                        && lvl.getFluidState(pos.above()).getFluidType()
                        == com.eisiadev.oversaturatedgrowth.init.ModFluids.REFINED_LARD_TYPE.get(),
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.LARD_RETCHER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.MOTHER_RETCHER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ModEntities.LARD_SLIME.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> {
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        if (serverLevel.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) return false;

                        net.minecraft.world.level.ChunkPos chunkPos =
                                new net.minecraft.world.level.ChunkPos(pos);
                        java.util.Random chunkRandom = new java.util.Random(
                                serverLevel.getSeed()
                                        + ((long) chunkPos.x * chunkPos.x * 0x4c1906)
                                        + (chunkPos.x * 0x5ac0dbL)
                                        + (long) chunkPos.z * chunkPos.z * 0x4307a7L
                                        + (chunkPos.z * 0x5f24fL) ^ 0x3ad8025f);
                        boolean isSlimeChunk = chunkRandom.nextInt(10) == 0;

                        if (isSlimeChunk && pos.getY() < 40
                                && serverLevel.getMaxLocalRawBrightness(pos) <= 8
                                && serverLevel.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL) {
                            return true;
                        }

                        net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome =
                                serverLevel.getBiome(pos);
                        return biome.is(net.minecraft.tags.BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS)
                                && pos.getY() > 50 && pos.getY() < 70
                                && random.nextFloat() < 0.5f
                                && random.nextFloat() < serverLevel.getMoonBrightness()
                                && serverLevel.getMaxLocalRawBrightness(pos) <= 8;
                    }
                    return false;
                },
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private static <T extends net.minecraft.world.entity.Mob> void registerGrottoGround(SpawnPlacementRegisterEvent event, net.minecraft.world.entity.EntityType<T> type) {
        event.register(type, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, lvl, st, pos, rnd) -> {
                    var below = lvl.getBlockState(pos.below());
                    return below.is(com.eisiadev.oversaturatedgrowth.init.ModBlocks.LARD_BLOCK.get())
                            || below.is(com.eisiadev.oversaturatedgrowth.init.ModBlocks.LARD_SLUDGE_BLOCK.get())
                            || below.isFaceSturdy(lvl, pos.below(), net.minecraft.core.Direction.UP);
                },
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private static <T extends net.minecraft.world.entity.Mob> void castleMob(SpawnPlacementRegisterEvent event, net.minecraft.world.entity.EntityType<T> type) {
        event.register(type, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (tp, lvl, st, pos, rnd) -> lvl.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
