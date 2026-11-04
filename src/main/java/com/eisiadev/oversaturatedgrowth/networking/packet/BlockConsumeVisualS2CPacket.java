package com.eisiadev.oversaturatedgrowth.networking.packet;

import com.eisiadev.oversaturatedgrowth.client.BlockConsumeClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BlockConsumeVisualS2CPacket {
    private final BlockPos pos;
    private final int stateId;
    private final int playerId;
    private final int pieces;

    public BlockConsumeVisualS2CPacket(BlockPos pos, int stateId, int playerId, int pieces) {
        this.pos = pos;
        this.stateId = stateId;
        this.playerId = playerId;
        this.pieces = pieces;
    }

    public BlockConsumeVisualS2CPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.stateId = buf.readVarInt();
        this.playerId = buf.readVarInt();
        this.pieces = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(stateId);
        buf.writeVarInt(playerId);
        buf.writeVarInt(pieces);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> BlockConsumeClient.addVisual(pos, stateId, playerId, pieces)));
        ctx.setPacketHandled(true);
        return true;
    }
}
