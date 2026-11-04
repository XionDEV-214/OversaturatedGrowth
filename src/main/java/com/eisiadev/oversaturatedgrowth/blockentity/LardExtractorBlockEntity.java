package com.eisiadev.oversaturatedgrowth.blockentity;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.block.LardExtractorBlock;
import com.eisiadev.oversaturatedgrowth.init.ModBlockEntities;
import com.eisiadev.oversaturatedgrowth.init.ModFluids;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.ExtractorStateSyncS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LardExtractorBlockEntity extends BlockEntity {

    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public @NotNull FluidStack getFluidInTank(int t) { return tank.getFluid(); }
        @Override public int getTankCapacity(int t) { return TANK_CAPACITY; }
        @Override public boolean isFluidValid(int t, @NotNull FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return tank.drain(resource, action);
        }
        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return tank.drain(maxDrain, action);
        }
    });

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCap.invalidate();
    }

    public static final int TANK_CAPACITY      = 64000;
    public static final int EXTRACT_AMOUNT     = 100;
    public static final int EXTRACT_INTERVAL   = 4;
    public static final int WEIGHT_PER_EXTRACT = 10;
    public static final double EXTRACT_RADIUS  = 5.0;

    public static final int TRANSFER_AMOUNT    = 250;
    public static final int FILL_STAGES        = 5;

    private int clientFluidAmount    = 0;
    private int clientCapacity       = TANK_CAPACITY;
    private boolean clientExtracting = false;
    private boolean clientHasTank    = false;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private boolean extracting = false;
    private int extractTimer   = 0;

    private int lastSyncedAmount = -1;

    public LardExtractorBlockEntity(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(ModBlockEntities.LARD_EXTRACTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos,
                                  net.minecraft.world.level.block.state.BlockState state,
                                  LardExtractorBlockEntity be) {

        if (be.extracting) {
            if (be.tank.getFluidAmount() >= TANK_CAPACITY) {
                be.stopExtracting();
            } else {
                be.extractTimer++;
                if (be.extractTimer >= EXTRACT_INTERVAL) {
                    be.extractTimer = 0;
                    be.doExtractTick((ServerLevel) level, pos);
                }
            }
        }

        if (!be.tank.isEmpty()) {
            be.transferToAdjacentTank(level, pos);
        }

        int currentAmount = be.tank.getFluidAmount();
        if (currentAmount != be.lastSyncedAmount) {
            be.sendSyncPacket((ServerLevel) level, pos);
            be.lastSyncedAmount = currentAmount;
        }
    }

    private void doExtractTick(ServerLevel level, BlockPos pos) {

        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        List<ServerPlayer> nearby = level.getPlayers(p ->
                p.distanceToSqr(cx, cy, cz) <= EXTRACT_RADIUS * EXTRACT_RADIUS);
        if (nearby.isEmpty()) return;

        for (ServerPlayer player : nearby) {
            if (tank.getFluidAmount() >= TANK_CAPACITY) break;

            float ratio = GgBridge.drainWeight(player, WEIGHT_PER_EXTRACT);
            if (ratio < 0f) {
                player.displayClientMessage(
                        Component.translatable("message.oversaturatedgrowth.lard_extractor_not_enough_weight"),
                        true);
                continue;
            }
            float multiplier = 1.0f + ratio * 31.0f;
            int dynamicExtractAmount = Math.round(EXTRACT_AMOUNT * multiplier);
            tank.fill(new FluidStack(ModFluids.REFINED_LARD_STILL.get(), dynamicExtractAmount),
                    IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void transferToAdjacentTank(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite())
                    .ifPresent(handler -> {
                        if (tank.isEmpty()) return;

                        FluidStack toSend = new FluidStack(
                                tank.getFluid(),
                                Math.min(TRANSFER_AMOUNT, tank.getFluidAmount()));

                        int accepted = handler.fill(toSend, IFluidHandler.FluidAction.SIMULATE);
                        if (accepted <= 0) return;

                        FluidStack actualSend = new FluidStack(tank.getFluid(), accepted);
                        handler.fill(actualSend, IFluidHandler.FluidAction.EXECUTE);
                        tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);

                                });

            if (tank.isEmpty()) break;
        }
    }

    public void toggleExtracting() {
        if (extracting) stopExtracting(); else startExtracting();
    }

    public void startExtracting() {
        if (tank.getFluidAmount() >= TANK_CAPACITY) return;
        extracting   = true;
        extractTimer = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            sendSyncPacket((ServerLevel) level, worldPosition);
        }
    }

    public void stopExtracting() {
        extracting   = false;
        extractTimer = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            sendSyncPacket((ServerLevel) level, worldPosition);
        }
    }

    public boolean isExtracting() { return extracting; }

    private void sendSyncPacket(ServerLevel level, BlockPos pos) {
        boolean hasTank = LardExtractorBlock.hasAdjacentFluidHandler(level, pos);
        Vec3 center = Vec3.atCenterOf(pos);
        ModMessages.sendToNearby(
                new ExtractorStateSyncS2CPacket(pos, tank.getFluidAmount(), TANK_CAPACITY, extracting, hasTank),
                level,
                center,
                32.0
        );
    }

    private float renderFill = 0f;

    public float smoothFill(float target) {
        renderFill += (target - renderFill) * 0.12f;
        if (Math.abs(target - renderFill) < 0.001f) renderFill = target;
        return renderFill;
    }

    public void applyClientSync(int fluidAmount, int capacity, boolean extracting, boolean hasTank) {
        this.clientFluidAmount  = fluidAmount;
        this.clientCapacity     = capacity;
        this.clientExtracting   = extracting;
        this.clientHasTank      = hasTank;
    }

    public int getClientFluidAmount()   { return clientFluidAmount; }
    public int getClientCapacity()      { return clientCapacity; }
    public boolean isClientExtracting() { return clientExtracting; }
    public boolean isClientHasTank()    { return clientHasTank; }

    public FluidTank getTank()   { return tank; }
    public int getFluidAmount()  { return tank.getFluidAmount(); }
    public int getTankCapacity() { return TANK_CAPACITY; }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putBoolean("extracting", extracting);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        extracting = tag.getBoolean("extracting");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
