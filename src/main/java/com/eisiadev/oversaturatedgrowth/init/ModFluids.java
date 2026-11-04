package com.eisiadev.oversaturatedgrowth.init;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.fluid.RefinedLardFluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, OversaturatedGrowth.MODID);

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, OversaturatedGrowth.MODID);

    public static final RegistryObject<FluidType> REFINED_LARD_TYPE = FLUID_TYPES.register("refined_lard",
            () -> new RefinedLardFluid.RefinedLardFluidType(
                    FluidType.Properties.create()
                            .density(1200)
                            .viscosity(1800)
                            .temperature(310)
            ));

    public static final RegistryObject<FlowingFluid> REFINED_LARD_STILL = FLUIDS.register("refined_lard",
            RefinedLardFluid.Still::new);

    public static final RegistryObject<FlowingFluid> REFINED_LARD_FLOWING = FLUIDS.register("refined_lard_flowing",
            RefinedLardFluid.Flowing::new);

    public static final RegistryObject<Item> REFINED_LARD_BUCKET = ModItems.ITEMS.register("refined_lard_bucket",
            () -> new BucketItem(REFINED_LARD_STILL, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));

    public static final RegistryObject<LiquidBlock> REFINED_LARD_BLOCK = ModBlocks.BLOCKS.register("refined_lard_fluid",
            () -> new LiquidBlock(REFINED_LARD_STILL,
                    BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.WATER)
                            .noLootTable()));

    public static ForgeFlowingFluid.Properties fluidProperties() {
        return new ForgeFlowingFluid.Properties(
                REFINED_LARD_TYPE,
                REFINED_LARD_STILL,
                REFINED_LARD_FLOWING)
                .bucket(REFINED_LARD_BUCKET)
                .block(REFINED_LARD_BLOCK);
    }

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
    }
}
