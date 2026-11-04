package com.eisiadev.oversaturatedgrowth.compat.gregtech.gt;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.minecraft.resources.ResourceLocation;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public final class OGMachines {
    private OGMachines() {}

    public static MultiblockMachineDefinition LARD_EXTRACTOR;

    public static final ResourceLocation BASE_CASING = GTCEu.id("block/casings/solid/machine_casing_solid_steel");
    public static final ResourceLocation OVERLAY = GTCEu.id("block/multiblock/fluid_drilling_rig");

    public static void init() {
        if (LARD_EXTRACTOR != null) return;
        LARD_EXTRACTOR = OGGTAddon.REGISTRATE
                .multiblock("lard_extractor_multiblock", LardExtractorMultiMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .langValue("Lard Extractor")
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                .pattern(definition -> FactoryBlockPattern.start()

                        .aisle("XXX", "XXX", "XXX", "XXX")
                        .aisle("XXX", "XAX", "XAX", "XXX")
                        .aisle("XXX", "XSX", "XXX", "XXX")
                        .where('S', controller(blocks(definition.get())))
                        .where('A', air())
                        .where('X', blocks(GTBlocks.CASING_STEEL_SOLID.get()).setMinGlobalLimited(14)
                                .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                                .or(abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(1)))
                        .build())
                .workableCasingModel(BASE_CASING, OVERLAY)
                .register();
    }
}
