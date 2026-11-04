package com.eisiadev.oversaturatedgrowth.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PredatorConsumeStateS2CPacket {

    private final boolean active;

    public PredatorConsumeStateS2CPacket(boolean active) {
        this.active = active;
    }

    public PredatorConsumeStateS2CPacket(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            com.eisiadev.oversaturatedgrowth.client.PredatorConsumeOverlay.setActive(active);
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
