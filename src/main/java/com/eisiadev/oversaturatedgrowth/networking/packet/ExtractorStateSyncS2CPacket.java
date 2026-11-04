package com.eisiadev.oversaturatedgrowth.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import com.eisiadev.oversaturatedgrowth.blockentity.LardExtractorBlockEntity;

import java.util.function.Supplier;

public class ExtractorStateSyncS2CPacket {

    private final BlockPos pos;
    private final int fluidAmount;
    private final int capacity;
    private final boolean extracting;
    private final boolean hasTank;

    public ExtractorStateSyncS2CPacket(BlockPos pos, int fluidAmount, int capacity, boolean extracting, boolean hasTank) {
        this.pos = pos;
        this.fluidAmount = fluidAmount;
        this.capacity = capacity;
        this.extracting = extracting;
        this.hasTank = hasTank;
    }

    public ExtractorStateSyncS2CPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.fluidAmount = buf.readInt();
        this.capacity = buf.readInt();
        this.extracting = buf.readBoolean();
        this.hasTank = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(fluidAmount);
        buf.writeInt(capacity);
        buf.writeBoolean(extracting);
        buf.writeBoolean(hasTank);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            BlockEntity be = mc.level.getBlockEntity(pos);
            if (be instanceof LardExtractorBlockEntity extractor) {
                extractor.applyClientSync(fluidAmount, capacity, extracting, hasTank);
            }
        });
        return true;
    }
}
