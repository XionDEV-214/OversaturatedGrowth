package com.eisiadev.oversaturatedgrowth.compat.gregtech.gt;

import com.eisiadev.oversaturatedgrowth.blockentity.LardExtractorBlockEntity;
import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.init.ModFluids;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.List;

public class LardExtractorMultiMachine extends WorkableElectricMultiblockMachine {

    public static final int CYCLE_TICKS = 600;
    public static final long EU_PER_TICK = 64;

    private static final int MAX_PER_EXTRACT = LardExtractorBlockEntity.EXTRACT_AMOUNT * 32;

    private int tickCounter = 0;

    public LardExtractorMultiMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new Logic(this);
    }

    @Override
    public boolean keepSubscribing() {
        return true;
    }

    private static GTRecipe fluidOutRecipe(int mb) {
        return GTRecipeBuilder.ofRaw()
                .outputFluids(new FluidStack(ModFluids.REFINED_LARD_STILL.get(), mb))
                .buildRawRecipe();
    }

    boolean hasRoom(int mb) {
        return RecipeHelper.matchContents(this, fluidOutRecipe(mb)).isSuccess();
    }

    private boolean pushFluid(int mb) {
        GTRecipe r = fluidOutRecipe(mb);
        if (!RecipeHelper.matchContents(this, r).isSuccess()) return false;
        return RecipeHelper.handleRecipeIO(this, r, IO.OUT, new HashMap<>()).isSuccess();
    }

    private List<ServerPlayer> nearbyPlayers(ServerLevel level) {
        Vec3 c = Vec3.atCenterOf(getPos());
        double r2 = LardExtractorBlockEntity.EXTRACT_RADIUS * LardExtractorBlockEntity.EXTRACT_RADIUS;
        return level.getPlayers(p -> p.distanceToSqr(c) <= r2);
    }

    boolean hasPlayerNearby() {
        return getLevel() instanceof ServerLevel sl && !nearbyPlayers(sl).isEmpty();
    }

    @Override
    public boolean onWorking() {
        if (!super.onWorking()) return false;
        if (getLevel() instanceof ServerLevel sl && ++tickCounter >= LardExtractorBlockEntity.EXTRACT_INTERVAL) {
            tickCounter = 0;
            return doExtract(sl);
        }
        return true;
    }

    private boolean doExtract(ServerLevel level) {
        for (ServerPlayer player : nearbyPlayers(level)) {
            if (!hasRoom(MAX_PER_EXTRACT)) return false;
            float ratio = GgBridge.drainWeight(player, LardExtractorBlockEntity.WEIGHT_PER_EXTRACT);
            if (ratio < 0f) {
                player.displayClientMessage(
                        Component.translatable("message.oversaturatedgrowth.lard_extractor_not_enough_weight"), true);
                continue;
            }
            float multiplier = 1.0f + ratio * 31.0f;
            pushFluid(Math.round(LardExtractorBlockEntity.EXTRACT_AMOUNT * multiplier));
        }
        return true;
    }

    public static class Logic extends RecipeLogic {
        public Logic(LardExtractorMultiMachine machine) {
            super(machine);
        }

        @Override
        public LardExtractorMultiMachine getMachine() {
            return (LardExtractorMultiMachine) super.getMachine();
        }

        @Override
        public void findAndHandleRecipe() {
            lastRecipe = null;
            LardExtractorMultiMachine m = getMachine();
            if (!m.hasPlayerNearby() || !m.hasRoom(MAX_PER_EXTRACT)) return;
            GTRecipe recipe = GTRecipeBuilder.ofRaw().duration(CYCLE_TICKS).EUt(EU_PER_TICK).buildRawRecipe();
            if (RecipeHelper.matchContents(m, recipe).isSuccess()) {
                setupRecipe(recipe);
            }
        }
    }
}
