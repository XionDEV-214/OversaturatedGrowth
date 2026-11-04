package com.eisiadev.oversaturatedgrowth.networking.packet;

import com.eisiadev.oversaturatedgrowth.event.DigestiveSoundHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GurgleRateC2SPacket {
    private final int rate;

    public GurgleRateC2SPacket(int rate) { this.rate = rate; }

    public GurgleRateC2SPacket(FriendlyByteBuf buf) { this.rate = buf.readVarInt(); }

    public void toBytes(FriendlyByteBuf buf) { buf.writeVarInt(rate); }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) DigestiveSoundHandler.setGurgleRate(sender.getUUID(), rate);
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
