package com.eisiadev.oversaturatedgrowth.fluid;

import com.eisiadev.oversaturatedgrowth.init.ModFluids;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class RefinedLardFluid extends ForgeFlowingFluid {

    protected RefinedLardFluid() {
        super(ModFluids.fluidProperties());
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return ModFluids.REFINED_LARD_FLOWING.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return ModFluids.REFINED_LARD_STILL.get();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == ModFluids.REFINED_LARD_STILL.get()
                || fluid == ModFluids.REFINED_LARD_FLOWING.get();
    }

    public static class Still extends RefinedLardFluid {
        @Override
        public boolean isSource(@NotNull FluidState state) { return true; }
        @Override
        public int getAmount(@NotNull FluidState state) { return 8; }
    }

    public static class Flowing extends RefinedLardFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        @Override
        public boolean isSource(@NotNull FluidState state) { return false; }
        @Override
        public int getAmount(FluidState state) { return state.getValue(LEVEL); }
    }

    public static class RefinedLardFluidType extends FluidType {

        private static final ResourceLocation STILL_TEXTURE =
                ResourceLocation.fromNamespaceAndPath("oversaturatedgrowth", "block/refined_lard_still");
        private static final ResourceLocation FLOW_TEXTURE =
                ResourceLocation.fromNamespaceAndPath("oversaturatedgrowth", "block/refined_lard_flow");

        private static final int TINT_COLOR = 0xFFFFFFFF;

        public RefinedLardFluidType(Properties properties) {
            super(properties);
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() { return STILL_TEXTURE; }
                @Override
                public ResourceLocation getFlowingTexture() { return FLOW_TEXTURE; }
                @Override
                public int getTintColor() { return TINT_COLOR; }
                public RenderType getRenderLayer() { return RenderType.translucent(); }
            });
        }
    }
}
