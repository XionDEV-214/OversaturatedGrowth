package com.eisiadev.oversaturatedgrowth.networking.packet;

import com.eisiadev.oversaturatedgrowth.accessory.gui.AccessoryUIFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent when the player presses the Accessory Bag key. */
public class OpenAccessoryBagC2SPacket {

    public OpenAccessoryBagC2SPacket() {}

    public OpenAccessoryBagC2SPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) AccessoryUIFactory.INSTANCE.open(player);
        });
        return true;
    }
}
