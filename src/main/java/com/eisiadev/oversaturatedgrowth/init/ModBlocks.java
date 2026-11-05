package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.block.LardExtractorBlock;
import com.eisiadev.oversaturatedgrowth.block.LardExtractorBlockItem;
import com.eisiadev.oversaturatedgrowth.block.FlammableBlocks;
import com.eisiadev.oversaturatedgrowth.block.LardlogLeavesVeinBlock;
import com.eisiadev.oversaturatedgrowth.block.LardlogSaplingBlock;
import com.eisiadev.oversaturatedgrowth.block.LardshroomBlock;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, OversaturatedGrowth.MODID);

    public static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();

    private static RegistryObject<Block> reg(String name, Supplier<Block> supplier) {
        RegistryObject<Block> block = BLOCKS.register(name, supplier);
        BLOCK_ITEMS.add(ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties())));
        return block;
    }

    private static RegistryObject<Block> stairs(String name, RegistryObject<Block> base) {
        return reg(name, () -> new StairBlock(() -> base.get().defaultBlockState(), BlockBehaviour.Properties.copy(base.get())));
    }
    private static RegistryObject<Block> slab(String name, RegistryObject<Block> base) {
        return reg(name, () -> new SlabBlock(BlockBehaviour.Properties.copy(base.get())));
    }
    private static RegistryObject<Block> wall(String name, RegistryObject<Block> base) {
        return reg(name, () -> new WallBlock(BlockBehaviour.Properties.copy(base.get())));
    }
    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                .requiresCorrectToolForDrops().strength(2.0f, 6.0f).sound(net.minecraft.world.level.block.SoundType.NETHERRACK);
    }

    public static final RegistryObject<Block> LARD_EXTRACTOR = BLOCKS.register("lard_extractor",
            () -> new LardExtractorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .noOcclusion().isViewBlocking((st, g, po) -> false)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f)
                    .destroyTime(3.5f)));

    public static final RegistryObject<Item> LARD_EXTRACTOR_ITEM =
            ModItems.ITEMS.register("lard_extractor",
                    () -> new LardExtractorBlockItem(
                            LARD_EXTRACTOR.get(), new Item.Properties()));

    public static final RegistryObject<Block> LARDSTONE = reg("lardstone", () -> new Block(stone()));
    public static final RegistryObject<Block> WEATHERED_LARDSTONE = reg("weathered_lardstone", () -> new Block(stone()));
    public static final RegistryObject<Block> GREASY_LARDSTONE = reg("greasy_lardstone", () -> new Block(stone()));
    public static final RegistryObject<Block> WEATHERED_GREASY_LARDSTONE = reg("weathered_greasy_lardstone", () -> new Block(stone()));
    public static final RegistryObject<Block> LARDY_WOOL = reg("lardy_wool", () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL)));

    private static BlockBehaviour.Properties castleWood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(60.0f, 1200.0f)
                .sound(SoundType.WOOD).requiresCorrectToolForDrops();
    }
    public static final RegistryObject<Block> CASTLE_WOOD = reg("castle_wood", () -> new Block(castleWood()));
    public static final RegistryObject<Block> CRACKED_CASTLE_WOOD = reg("cracked_castle_wood", () -> new Block(castleWood()));
    public static final RegistryObject<Block> ENCASED_CASTLE_WOOD = reg("encased_castle_wood", () -> new Block(castleWood()));
    public static final RegistryObject<Block> REAPPEARING_CASTLE_DOOR = reg("reappearing_castle_door",
            () -> new com.eisiadev.oversaturatedgrowth.block.ReappearingBlock(BlockBehaviour.Properties.of().lightLevel(st -> 4)
                    .mapColor(MapColor.WOOD).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)
                    .requiresCorrectToolForDrops().sound(SoundType.WOOD).strength(20.0F, 35.0F)));
    public static final RegistryObject<Block> INFESTED_CASTLE_WOOD = reg("infested_castle_wood", () -> new Block(castleWood()));
    public static final RegistryObject<Block> POLISHED_LARDSTONE = reg("polished_lardstone", () -> new Block(stone()));
    public static final RegistryObject<Block> CARVED_LARDSTONE = reg("carved_lardstone", () -> new Block(stone()));
    public static final RegistryObject<Block> LARDSTONE_BRICKS = reg("lardstone_bricks", () -> new Block(stone()));
    public static final RegistryObject<Block> LARDSTONE_SLATES = reg("lardstone_slates", () -> new Block(stone()));

    public static final RegistryObject<Block> LARDSTONE_STAIRS = stairs("lardstone_stairs", LARDSTONE);
    public static final RegistryObject<Block> LARDSTONE_SLAB = slab("lardstone_slab", LARDSTONE);
    public static final RegistryObject<Block> LARDSTONE_WALL = wall("lardstone_wall", LARDSTONE);
    public static final RegistryObject<Block> POLISHED_LARDSTONE_STAIRS = stairs("polished_lardstone_stairs", POLISHED_LARDSTONE);
    public static final RegistryObject<Block> POLISHED_LARDSTONE_SLAB = slab("polished_lardstone_slab", POLISHED_LARDSTONE);
    public static final RegistryObject<Block> POLISHED_LARDSTONE_WALL = wall("polished_lardstone_wall", POLISHED_LARDSTONE);
    public static final RegistryObject<Block> LARDSTONE_BRICK_STAIRS = stairs("lardstone_brick_stairs", LARDSTONE_BRICKS);
    public static final RegistryObject<Block> LARDSTONE_BRICK_SLAB = slab("lardstone_brick_slab", LARDSTONE_BRICKS);
    public static final RegistryObject<Block> LARDSTONE_BRICK_WALL = wall("lardstone_brick_wall", LARDSTONE_BRICKS);

    public static final RegistryObject<Block> LARDLOG = reg("lardlog", () -> new FlammableBlocks.Pillar(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(2.0f).sound(SoundType.WOOD).ignitedByLava()));
    public static final RegistryObject<Block> LARDLOG_PLANKS = reg("lardlog_planks", () -> new FlammableBlocks.Plain(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava()));
    public static final RegistryObject<Block> LARDLOG_STAIRS = reg("lardlog_stairs", () -> new FlammableBlocks.Stairs(() -> LARDLOG_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(LARDLOG_PLANKS.get())));
    public static final RegistryObject<Block> LARDLOG_SLAB = reg("lardlog_slab", () -> new FlammableBlocks.Slab(BlockBehaviour.Properties.copy(LARDLOG_PLANKS.get())));
    public static final RegistryObject<Block> LARDLOG_LEAVES = reg("lardlog_leaves", () -> new FlammableBlocks.Leaves(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.2f).sound(SoundType.GRASS).randomTicks().noOcclusion()
                    .isValidSpawn((s, g, p, t) -> false).isSuffocating((s, g, p) -> false).isViewBlocking((s, g, p) -> false)
                    .ignitedByLava()));
    public static final RegistryObject<Block> LARDLOG_LEAVES_VEIN = reg("lardlog_leaves_vein", () -> new LardlogLeavesVeinBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollission().noOcclusion().instabreak()
                    .sound(net.minecraft.world.level.block.SoundType.GRASS).ignitedByLava()));

    public static final RegistryObject<Block> LARD_BLOCK = reg("lard_block", () -> new HoneyBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).speedFactor(0.4f).jumpFactor(0.5f)
                    .noOcclusion().sound(net.minecraft.world.level.block.SoundType.HONEY_BLOCK)));
    public static final RegistryObject<Block> LARD_SLUDGE_BLOCK = reg("lard_sludge_block", () -> new SlimeBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).friction(0.8f).noOcclusion()
                    .sound(net.minecraft.world.level.block.SoundType.SLIME_BLOCK)));

    public static final RegistryObject<Block> LARDLOG_SAPLING = reg("lardlog_sapling", () -> new LardlogSaplingBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollission().randomTicks().instabreak()
                    .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<Block> FATTADIUM_ORE = reg("fattadium_ore", () -> new DropExperienceBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().strength(3.0f, 3.0f),
            UniformInt.of(1, 3)));

    private static BlockBehaviour.Properties shroom() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).noCollission().randomTicks().instabreak()
                .sound(SoundType.GRASS).lightLevel(s -> 1).pushReaction(PushReaction.DESTROY);
    }
    public static final RegistryObject<Block> LARDSHROOM = BLOCKS.register("lardshroom", () -> new LardshroomBlock(shroom()));

    public static final RegistryObject<Item> LARDSHROOM_ITEM = ModItems.ITEMS.register("lardshroom",
            () -> new BlockItem(LARDSHROOM.get(),
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).fast().build())));
    static { BLOCK_ITEMS.add(LARDSHROOM_ITEM); }
}
