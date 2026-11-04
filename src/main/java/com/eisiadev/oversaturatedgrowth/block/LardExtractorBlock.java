package com.eisiadev.oversaturatedgrowth.block;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import com.eisiadev.oversaturatedgrowth.blockentity.LardExtractorBlockEntity;
import com.eisiadev.oversaturatedgrowth.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LardExtractorBlock extends BaseEntityBlock {

    public LardExtractorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static boolean hasAdjacentFluidHandler(Level level, BlockPos pos) {

        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be != null && be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).isPresent()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LardExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.LARD_EXTRACTOR.get(),
                LardExtractorBlockEntity::serverTick);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level,
                                           @NotNull BlockPos pos, @NotNull Player player,
                                           @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof LardExtractorBlockEntity extractor)) return InteractionResult.PASS;

        if (!level.isClientSide()) {

            if (player.isShiftKeyDown()) {
                int unit = com.eisiadev.oversaturatedgrowth.util.LardItemFluid.REFINED_LARD_MB;
                int n = extractor.getFluidAmount() / unit;
                if (n <= 0) {
                    player.displayClientMessage(Component.translatable("message.oversaturatedgrowth.extractor_not_enough_fluid", unit), true);
                    return InteractionResult.CONSUME;
                }
                extractor.getTank().drain(n * unit, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                int left = n;
                while (left > 0) {
                    int c = Math.min(left, 64);
                    ItemStack out = new ItemStack(com.eisiadev.oversaturatedgrowth.init.ModItems.REFINED_LARD.get(), c);
                    if (!player.getInventory().add(out)) player.drop(out, false);
                    left -= c;
                }
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.HONEY_BLOCK_BREAK,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1f, 0.9f);
                player.displayClientMessage(Component.translatable("message.oversaturatedgrowth.extractor_solidified", n, n * unit), true);
                return InteractionResult.CONSUME;
            }

            ItemStack held = player.getItemInHand(hand);
            int itemMb = com.eisiadev.oversaturatedgrowth.util.LardItemFluid.mbFor(held);
            if (itemMb > 0) {
                int accepted = extractor.getTank().fill(
                        new net.minecraftforge.fluids.FluidStack(com.eisiadev.oversaturatedgrowth.init.ModFluids.REFINED_LARD_STILL.get(), itemMb),
                        net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                if (accepted >= itemMb) {
                    extractor.getTank().fill(
                            new net.minecraftforge.fluids.FluidStack(com.eisiadev.oversaturatedgrowth.init.ModFluids.REFINED_LARD_STILL.get(), itemMb),
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.HONEY_BLOCK_PLACE,
                            net.minecraft.sounds.SoundSource.BLOCKS, 1f, 1f);
                } else {
                    player.displayClientMessage(Component.translatable("message.oversaturatedgrowth.extractor_no_room"), true);
                }
                return InteractionResult.CONSUME;
            }
            if (!held.isEmpty()
                    && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
                return InteractionResult.CONSUME;
            }

            extractor.toggleExtracting();

            int amount   = extractor.getFluidAmount();
            int capacity = LardExtractorBlockEntity.TANK_CAPACITY;
            int pct      = (int)((float) amount / capacity * 100);
            String statusKey = extractor.isExtracting()
                    ? "message.oversaturatedgrowth.status_extracting"
                    : "message.oversaturatedgrowth.status_idle";

            player.displayClientMessage(
                    Component.translatable("message.oversaturatedgrowth.tank_status", amount, capacity, pct)
                             .append(Component.literal("  "))
                             .append(Component.translatable(statusKey)),
                    true);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }
}
