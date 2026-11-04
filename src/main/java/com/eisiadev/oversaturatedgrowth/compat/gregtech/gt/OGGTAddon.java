package com.eisiadev.oversaturatedgrowth.compat.gregtech.gt;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModFluids;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import com.eisiadev.oversaturatedgrowth.util.LardItemFluid;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

@GTAddon
public class OGGTAddon implements IGTAddon {

    public static final GTRegistrate REGISTRATE = GTRegistrate.create(OversaturatedGrowth.MODID);

    public static void registerRegistrate() {
        REGISTRATE.registerRegistrate();
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addGenericListener(MachineDefinition.class, OGGTAddon::onRegisterMachines);

        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            OGModels.registerListener(modBus);
        }
    }

    private static void onRegisterMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        OGMachines.init();
    }

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    @Override
    public void initializeAddon() {

    }

    public static net.minecraft.world.item.ItemStack controllerStack() {
        return OGMachines.LARD_EXTRACTOR != null ? OGMachines.LARD_EXTRACTOR.asStack()
                : net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public String addonModId() {
        return OversaturatedGrowth.MODID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {

        solidify(provider, "refined_lard_item", LardItemFluid.REFINED_LARD_MB, 1,
                new net.minecraft.world.item.ItemStack(ModItems.REFINED_LARD.get()));

        solidify(provider, "lard_sludge_ball", LardItemFluid.SLUDGE_BALL_MB, 2,
                new net.minecraft.world.item.ItemStack(ModItems.LARD_SLUDGE_BALL.get()));

        solidify(provider, "lard_sludge_block", LardItemFluid.SLUDGE_BLOCK_MB, 3,
                new net.minecraft.world.item.ItemStack(ModBlocks.LARD_SLUDGE_BLOCK.get()));

        extract(provider, "refined_lard_from_item", ModItems.REFINED_LARD.get(), LardItemFluid.REFINED_LARD_MB);
        extract(provider, "refined_lard_from_sludge_ball", ModItems.LARD_SLUDGE_BALL.get(), LardItemFluid.SLUDGE_BALL_MB);
        extract(provider, "refined_lard_from_sludge_block", ModBlocks.LARD_SLUDGE_BLOCK.get().asItem(), LardItemFluid.SLUDGE_BLOCK_MB);
    }

    private static void extract(Consumer<FinishedRecipe> provider, String id, net.minecraft.world.item.Item in, int mb) {
        GTRecipeTypes.EXTRACTOR_RECIPES
                .recipeBuilder(ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, id))
                .inputItems(in)
                .outputFluids(new FluidStack(ModFluids.REFINED_LARD_STILL.get(), mb))
                .duration(Math.max(20, mb / 8))
                .EUt(8)
                .save(provider);
    }

    private static void solidify(Consumer<FinishedRecipe> provider, String id, int mb, int circuit,
                                 net.minecraft.world.item.ItemStack out) {
        GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES
                .recipeBuilder(ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, id))
                .circuitMeta(circuit)

                .inputFluids(FluidIngredient.of(ModFluids.REFINED_LARD_STILL.get(), mb))
                .outputItems(out)
                .duration(Math.max(20, mb / 8))
                .EUt(8)
                .save(provider);
    }
}
