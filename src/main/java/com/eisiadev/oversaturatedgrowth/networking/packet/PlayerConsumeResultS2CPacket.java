package com.eisiadev.oversaturatedgrowth.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerConsumeResultS2CPacket {

    private final boolean escaped;

    public PlayerConsumeResultS2CPacket(boolean escaped) {
        this.escaped = escaped;
    }

    public PlayerConsumeResultS2CPacket(FriendlyByteBuf buf) {
        this.escaped = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(escaped);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            com.eisiadev.oversaturatedgrowth.client.PlayerConsumedOverlay.end(escaped);
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
