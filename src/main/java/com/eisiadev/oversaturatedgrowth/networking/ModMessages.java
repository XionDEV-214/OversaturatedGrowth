package com.eisiadev.oversaturatedgrowth.networking;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.networking.packet.BlockConsumeSyncS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.BlockConsumeVisualS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.ExtractorStateSyncS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.GurgleRateC2SPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.OGSoundS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.HoldStateC2SPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.PlayerConsumedS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.PlayerConsumeResultS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.PredatorConsumeStateS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.EscapeKeyPressC2SPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.OpenAccessoryBagC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        INSTANCE = net;

        net.messageBuilder(HoldStateC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(HoldStateC2SPacket::new)
                .encoder(HoldStateC2SPacket::toBytes)
                .consumerMainThread(HoldStateC2SPacket::handle)
                .add();

        net.messageBuilder(ExtractorStateSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ExtractorStateSyncS2CPacket::new)
                .encoder(ExtractorStateSyncS2CPacket::toBytes)
                .consumerMainThread(ExtractorStateSyncS2CPacket::handle)
                .add();

        net.messageBuilder(PlayerConsumedS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PlayerConsumedS2CPacket::new)
                .encoder(PlayerConsumedS2CPacket::toBytes)
                .consumerMainThread(PlayerConsumedS2CPacket::handle)
                .add();

        net.messageBuilder(PlayerConsumeResultS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PlayerConsumeResultS2CPacket::new)
                .encoder(PlayerConsumeResultS2CPacket::toBytes)
                .consumerMainThread(PlayerConsumeResultS2CPacket::handle)
                .add();

        net.messageBuilder(EscapeKeyPressC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(EscapeKeyPressC2SPacket::new)
                .encoder(EscapeKeyPressC2SPacket::toBytes)
                .consumerMainThread(EscapeKeyPressC2SPacket::handle)
                .add();

        net.messageBuilder(PredatorConsumeStateS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PredatorConsumeStateS2CPacket::new)
                .encoder(PredatorConsumeStateS2CPacket::toBytes)
                .consumerMainThread(PredatorConsumeStateS2CPacket::handle)
                .add();

        net.messageBuilder(BlockConsumeSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BlockConsumeSyncS2CPacket::new)
                .encoder(BlockConsumeSyncS2CPacket::toBytes)
                .consumerMainThread(BlockConsumeSyncS2CPacket::handle)
                .add();
        net.messageBuilder(BlockConsumeVisualS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BlockConsumeVisualS2CPacket::new)
                .encoder(BlockConsumeVisualS2CPacket::toBytes)
                .consumerMainThread(BlockConsumeVisualS2CPacket::handle)
                .add();

        net.messageBuilder(OGSoundS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OGSoundS2CPacket::new)
                .encoder(OGSoundS2CPacket::toBytes)
                .consumerMainThread(OGSoundS2CPacket::handle)
                .add();
        net.messageBuilder(GurgleRateC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(GurgleRateC2SPacket::new)
                .encoder(GurgleRateC2SPacket::toBytes)
                .consumerMainThread(GurgleRateC2SPacket::handle)
                .add();
        net.messageBuilder(OpenAccessoryBagC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenAccessoryBagC2SPacket::new)
                .encoder(OpenAccessoryBagC2SPacket::toBytes)
                .consumerMainThread(OpenAccessoryBagC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        if (player == null) return;
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToNearby(MSG message, net.minecraft.server.level.ServerLevel level,
                                           net.minecraft.world.phys.Vec3 pos, double radius) {
        INSTANCE.send(PacketDistributor.NEAR.with(
                () -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, level.dimension())
        ), message);
    }
}
