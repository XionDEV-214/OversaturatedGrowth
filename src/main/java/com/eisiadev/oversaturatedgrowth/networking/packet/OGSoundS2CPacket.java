package com.eisiadev.oversaturatedgrowth.networking.packet;

import com.eisiadev.oversaturatedgrowth.client.OGSoundClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OGSoundS2CPacket {
    public static final int RUMBLE = 0;
    public static final int DIGESTING = 1;

    private final int type;
    private final double x, y, z;
    private final float volume, pitch;

    public OGSoundS2CPacket(int type, double x, double y, double z, float volume, float pitch) {
        this.type = type; this.x = x; this.y = y; this.z = z; this.volume = volume; this.pitch = pitch;
    }

    public OGSoundS2CPacket(FriendlyByteBuf buf) {
        this.type = buf.readVarInt();
        this.x = buf.readDouble(); this.y = buf.readDouble(); this.z = buf.readDouble();
        this.volume = buf.readFloat(); this.pitch = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(type);
        buf.writeDouble(x); buf.writeDouble(y); buf.writeDouble(z);
        buf.writeFloat(volume); buf.writeFloat(pitch);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> OGSoundClient.playDigestive(type, x, y, z, volume, pitch)));
        ctx.setPacketHandled(true);
        return true;
    }
}
