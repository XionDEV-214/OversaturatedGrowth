package com.eisiadev.oversaturatedgrowth.networking.packet;

import com.eisiadev.oversaturatedgrowth.consume.block.BlockConsumeHandler;
import com.eisiadev.oversaturatedgrowth.event.EntityConsumeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class HoldStateC2SPacket {
    private final boolean holding;
    private final int targetId;
    @Nullable
    private final BlockPos blockPos;

    public HoldStateC2SPacket(boolean holding, int targetId) {
        this(holding, targetId, null);
    }

    private HoldStateC2SPacket(boolean holding, int targetId, @Nullable BlockPos blockPos) {
        this.holding = holding;
        this.targetId = targetId;
        this.blockPos = blockPos;
    }

    public static HoldStateC2SPacket block(BlockPos pos) {
        return new HoldStateC2SPacket(true, -1, pos);
    }

    public HoldStateC2SPacket(FriendlyByteBuf buf) {
        this.holding = buf.readBoolean();
        this.targetId = buf.readInt();
        this.blockPos = buf.readBoolean() ? buf.readBlockPos() : null;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(holding);
        buf.writeInt(targetId);
        buf.writeBoolean(blockPos != null);
        if (blockPos != null) buf.writeBlockPos(blockPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (holding && blockPos != null) {
                EntityConsumeHandler.cancelHold(player);
                BlockConsumeHandler.onHold(player, blockPos);
            } else if (holding) {
                BlockConsumeHandler.cancel(player);
                EntityConsumeHandler.onHoldTick(player, targetId);
            } else {
                EntityConsumeHandler.cancelHold(player);
                BlockConsumeHandler.cancel(player);
            }
        });
        return true;
    }
}
