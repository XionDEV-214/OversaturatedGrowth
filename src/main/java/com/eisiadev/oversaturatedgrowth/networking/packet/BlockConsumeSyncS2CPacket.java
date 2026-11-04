package com.eisiadev.oversaturatedgrowth.networking.packet;

import com.eisiadev.oversaturatedgrowth.client.BlockConsumeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class BlockConsumeSyncS2CPacket {
    private final List<ResourceLocation> blocks;
    private final List<ResourceLocation> tags;

    public BlockConsumeSyncS2CPacket(List<ResourceLocation> blocks, List<ResourceLocation> tags) {
        this.blocks = blocks;
        this.tags = tags;
    }

    public BlockConsumeSyncS2CPacket(FriendlyByteBuf buf) {
        this.blocks = buf.readList(FriendlyByteBuf::readResourceLocation);
        this.tags = buf.readList(FriendlyByteBuf::readResourceLocation);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(blocks, FriendlyByteBuf::writeResourceLocation);
        buf.writeCollection(tags, FriendlyByteBuf::writeResourceLocation);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BlockConsumeClient.setData(blocks, tags)));
        ctx.setPacketHandled(true);
        return true;
    }
}
