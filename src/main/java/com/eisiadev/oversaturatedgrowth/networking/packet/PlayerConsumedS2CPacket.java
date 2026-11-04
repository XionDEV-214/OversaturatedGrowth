package com.eisiadev.oversaturatedgrowth.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerConsumedS2CPacket {

    private final int predatorEntityId;

    private final int timeLimitTicks;

    private final int requiredPresses;

    public PlayerConsumedS2CPacket(int predatorEntityId, int timeLimitTicks, int requiredPresses) {
        this.predatorEntityId = predatorEntityId;
        this.timeLimitTicks = timeLimitTicks;
        this.requiredPresses = requiredPresses;
    }

    public PlayerConsumedS2CPacket(FriendlyByteBuf buf) {
        this.predatorEntityId = buf.readInt();
        this.timeLimitTicks = buf.readInt();
        this.requiredPresses = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(predatorEntityId);
        buf.writeInt(timeLimitTicks);
        buf.writeInt(requiredPresses);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {

            com.eisiadev.oversaturatedgrowth.client.PlayerConsumedOverlay.startConsumed(
                    predatorEntityId, timeLimitTicks, requiredPresses);
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
