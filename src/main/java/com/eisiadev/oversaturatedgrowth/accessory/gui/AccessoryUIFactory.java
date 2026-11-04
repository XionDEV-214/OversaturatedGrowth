package com.eisiadev.oversaturatedgrowth.accessory.gui;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.AbstractUIFactory;
import brachy.modularui.factory.GuiManager;
import brachy.modularui.factory.PosGuiData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Opens the Accessory Bag screen. It is a player-bound UI, but it rides on {@link PosGuiData} (the player's own block
 * position is sent along and ignored) so it only needs the ModularUI API that the Vending Machine already proves out.
 */
public final class AccessoryUIFactory extends AbstractUIFactory<PosGuiData> {

    public static final AccessoryUIFactory INSTANCE = new AccessoryUIFactory();

    private AccessoryUIFactory() {
        super(ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "accessory_bag"));
    }

    /** Call from FMLCommonSetupEvent#enqueueWork. */
    public static void register() {
        if (!GuiManager.hasFactory(INSTANCE.getFactoryName())) {
            GuiManager.registerFactory(INSTANCE);
        }
    }

    public void open(ServerPlayer player) {
        GuiManager.open(this, new PosGuiData(player, player.blockPosition()), player);
    }

    @Override
    public @NotNull IUIHolder<PosGuiData> getGuiHolder(PosGuiData data) {
        return AccessoryUIHolder.INSTANCE;
    }

    @Override
    public boolean canInteractWith(Player player, PosGuiData data) {
        return player == data.getPlayer() && player.isAlive();
    }

    @Override
    public void writeGuiData(PosGuiData data, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(data.getBlockPos());
    }

    @Override
    public @NotNull PosGuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new PosGuiData(player, buffer.readBlockPos());
    }
}
