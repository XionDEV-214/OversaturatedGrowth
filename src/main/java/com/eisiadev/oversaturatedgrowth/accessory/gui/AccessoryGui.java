package com.eisiadev.oversaturatedgrowth.accessory.gui;

import com.eisiadev.oversaturatedgrowth.accessory.AccessoryBag;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryBagProvider;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryData;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryItem;
import com.eisiadev.oversaturatedgrowth.accessory.Awakening;
import com.eisiadev.oversaturatedgrowth.accessory.Reroll;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.ModularSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Accessory Bag screen: the 45 bag slots in the middle, the summed stats on the left, Awakening / Reroll tabs on the right.
 * Built on both sides: the server only registers the slots / actions, the client adds the widgets.
 */
public class AccessoryGui {

    public static final String PANEL_NAME = "og_accessory_bag";
    public static final String SLOT_BAG = "og_bag";
    public static final String SLOT_IN = "og_awaken_in";
    public static final String SLOT_OUT = "og_awaken_out";
    public static final String SLOT_REROLL = "og_reroll";
    public static final String ACTION_AWAKEN = "og_awaken";
    public static final String ACTION_REROLL = "og_reroll";
    public static final String ACTION_MOVE = "og_move";

    /** Where a clicked accessory sits (sent with {@link #ACTION_MOVE}). */
    public static final int ZONE_BAG = 0, ZONE_INPUT = 1, ZONE_OUTPUT = 2, ZONE_REROLL = 3, ZONE_PLAYER = 4;
    /** Which side tab is open, i.e. where shift-click inserts. */
    public static final int TAB_AWAKEN = 0, TAB_REROLL = 1;

    /** One window: [summary | bag + inventory | awakening / reroll]. */
    public static final int SUMMARY_W = 104;
    public static final int MAIN_X = SUMMARY_W;
    public static final int MAIN_W = 178;
    public static final int SIDE_X = MAIN_X + MAIN_W;
    public static final int SIDE_W = 112;
    public static final int WIDTH = SUMMARY_W + MAIN_W + SIDE_W;
    public static final int HEIGHT = 206;
    public static final int SIDE_HEIGHT = HEIGHT;
    public static final int BAG_X = MAIN_X + 8;
    public static final int BAG_Y = 18;
    public static final int INVENTORY_X = MAIN_X + 8;
    public static final int INVENTORY_Y = 124;

    private final PosGuiData data;
    private final PanelSyncManager syncManager;
    private final boolean client;

    private final ItemStackHandler bagItems;
    private final ItemStackHandler inputs = new ItemStackHandler(Awakening.INPUTS) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof AccessoryItem && AccessoryData.read(stack) != null;
        }
    };
    private final ItemStackHandler output = new ItemStackHandler(1);
    private final ItemStackHandler reroll = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof AccessoryItem && AccessoryData.read(stack) != null;
        }
    };

    public AccessoryGui(PosGuiData data, PanelSyncManager syncManager) {
        this.data = data;
        this.syncManager = syncManager;
        this.client = syncManager.isClient();
        this.bagItems = data.getPlayer().getCapability(AccessoryBagProvider.CAP)
                .map(bag -> bag.items)
                .orElseGet(() -> new AccessoryBag().items);
    }

    public ModularPanel<?> build(UISettings settings) {
        registerSync();

        ModularPanel<?> panel = new ModularPanel<>(PANEL_NAME);
        panel.size(WIDTH, HEIGHT);

        if (client) {
            AccessoryClientUI.build(panel, this);
        } else {
            panel.child(SlotGroupWidget.playerInventory(false).pos(INVENTORY_X, INVENTORY_Y));
        }
        return panel;
    }

    private void registerSync() {
        Player player = data.getPlayer();
        syncManager.bindPlayerInventory(player);

        syncManager.registerSlotGroup(SLOT_BAG, 9, true);
        for (int i = 0; i < AccessoryBag.SIZE; i++) {
            // Never filled / emptied by vanilla clicks: every move goes through ACTION_MOVE.
            syncManager.itemSlot(SLOT_BAG, i, new ModularSlot(bagItems, i).accessibility(false, false).slotGroup(SLOT_BAG));
        }

        syncManager.registerSlotGroup(SLOT_IN, Awakening.INPUTS, false);
        for (int i = 0; i < Awakening.INPUTS; i++) {
            syncManager.itemSlot(SLOT_IN, i, new ModularSlot(inputs, i).accessibility(false, false).slotGroup(SLOT_IN));
        }

        syncManager.registerSlotGroup(SLOT_OUT, 1, false);
        syncManager.itemSlot(SLOT_OUT, 0, new ModularSlot(output, 0)
                .accessibility(false, false)
                .slotGroup(SLOT_OUT));

        syncManager.registerSlotGroup(SLOT_REROLL, 1, false);
        syncManager.itemSlot(SLOT_REROLL, 0, new ModularSlot(reroll, 0).accessibility(false, false).slotGroup(SLOT_REROLL));

        syncManager.registerServerSyncedAction(ACTION_MOVE, buf -> {
            int zone = buf.readVarInt();
            int index = buf.readVarInt();
            boolean shift = buf.readBoolean();
            int tab = buf.readVarInt();
            if (player instanceof ServerPlayer sp) move(sp, zone, index, shift, tab);
        });

        syncManager.registerServerSyncedAction(ACTION_REROLL, buf -> {
            int mask = buf.readVarInt();
            if (player instanceof ServerPlayer sp) Reroll.perform(sp, reroll, mask);
        });
        syncManager.registerServerSyncedAction(ACTION_AWAKEN, buf -> {
            if (player instanceof ServerPlayer sp) Awakening.perform(sp, inputs, output);
        });

        // Whatever is still sitting in the awakening slots when the screen closes goes back to the player.
        syncManager.addCloseListener(p -> {
            if (client) return;
            giveBack(p, inputs);
            giveBack(p, output);
            giveBack(p, reroll);
        });
    }

    // ------------------------------------------------------------------------------------------------ click moves

    /**
     * Click rules (left button, nothing on the cursor):
     * <ul>
     *   <li>inventory: click = into the bag, shift = into the open tab (awakening input / reroll slot)</li>
     *   <li>bag: click = back to the inventory, shift = into the open tab</li>
     *   <li>awakening / reroll slots: click = into the bag, shift = back to the inventory</li>
     * </ul>
     */
    private void move(ServerPlayer player, int zone, int index, boolean shift, int tab) {
        if (!player.containerMenu.getCarried().isEmpty()) return;
        ItemStackHandler source = switch (zone) {
            case ZONE_BAG -> bagItems;
            case ZONE_INPUT -> inputs;
            case ZONE_OUTPUT -> output;
            case ZONE_REROLL -> reroll;
            default -> null;
        };
        ItemStack stack;
        if (zone == ZONE_PLAYER) {
            if (index < 0 || index >= player.getInventory().items.size()) return;
            stack = player.getInventory().items.get(index);
        } else {
            if (source == null || index < 0 || index >= source.getSlots()) return;
            stack = source.getStackInSlot(index);
        }
        if (stack.isEmpty() || !(stack.getItem() instanceof AccessoryItem) || AccessoryData.read(stack) == null) return;

        boolean toInventory = (zone == ZONE_BAG && !shift) || (zone != ZONE_BAG && zone != ZONE_PLAYER && shift);
        boolean toBag = (zone == ZONE_PLAYER && !shift) || (zone != ZONE_BAG && zone != ZONE_PLAYER && !shift);

        if (toInventory) {
            ItemStack copy = stack.copy();
            if (!player.getInventory().add(copy) || !copy.isEmpty()) return;
            source.setStackInSlot(index, ItemStack.EMPTY);
            player.playNotifySound(SoundEvents.HORSE_SADDLE, SoundSource.PLAYERS, 0.8f, 2.0f);
        } else {
            ItemStackHandler target = toBag ? bagItems : (tab == TAB_REROLL ? reroll : inputs);
            int free = firstEmpty(target);
            if (free < 0 && toBag && zone != ZONE_PLAYER) {
                // bag full: a side-slot item goes back to the inventory instead
                move(player, zone, index, true, tab);
                return;
            }
            if (free < 0) return;
            target.setStackInSlot(free, stack.copy());
            if (zone == ZONE_PLAYER) player.getInventory().items.set(index, ItemStack.EMPTY);
            else source.setStackInSlot(index, ItemStack.EMPTY);
            player.playNotifySound(SoundEvents.HORSE_ARMOR, SoundSource.PLAYERS, 0.8f, 2.0f);
            player.playNotifySound(SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.6f, 1.5f);
        }
        player.getInventory().setChanged();
    }

    private static int firstEmpty(ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) if (handler.getStackInSlot(i).isEmpty()) return i;
        return -1;
    }

    private static void giveBack(Player player, ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            handler.setStackInSlot(i, ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    public Player player() {
        return data.getPlayer();
    }

    public PanelSyncManager sync() {
        return syncManager;
    }

    public ItemStackHandler bagItems() {
        return bagItems;
    }

    public ItemStackHandler inputs() {
        return inputs;
    }

    public ItemStackHandler output() {
        return output;
    }

    public ItemStackHandler reroll() {
        return reroll;
    }
}
