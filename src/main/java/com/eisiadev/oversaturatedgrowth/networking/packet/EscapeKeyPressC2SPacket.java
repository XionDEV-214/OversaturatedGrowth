package com.eisiadev.oversaturatedgrowth.networking.packet;

import com.eisiadev.oversaturatedgrowth.event.PlayerConsumeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EscapeKeyPressC2SPacket {

    private final int totalPresses;

    public EscapeKeyPressC2SPacket(int totalPresses) {
        this.totalPresses = totalPresses;
    }

    public EscapeKeyPressC2SPacket(FriendlyByteBuf buf) {
        this.totalPresses = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(totalPresses);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                PlayerConsumeHandler.onEscapeKeyPress(player, totalPresses);
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
