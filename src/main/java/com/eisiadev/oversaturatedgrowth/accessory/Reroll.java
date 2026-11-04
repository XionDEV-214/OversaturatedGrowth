package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Option reroll: one accessory + Lard Sludge Balls + Refined Lard -> the same accessory with new options.
 * Lines can be locked (bit mask, bit i = line i); at least one line must stay unlocked.
 * The client only calls {@link #check} to enable / explain the button, the server re-checks.
 */
public final class Reroll {

    public enum Check {
        OK,
        EMPTY,
        ALL_LOCKED,
        MATERIALS
    }

    private Reroll() {}

    /** Drops lock bits that point past the accessory's last line. */
    public static int sanitize(int lockMask, AccessoryData data) {
        int lines = data.entries().size();
        return lines >= 31 ? lockMask : lockMask & ((1 << lines) - 1);
    }

    public static Check check(Player player, ItemStackHandler slot, int lockMask) {
        AccessoryData data = AccessoryData.read(slot.getStackInSlot(0));
        if (data == null || data.entries().isEmpty()) return Check.EMPTY;
        int mask = sanitize(lockMask, data);
        int locks = Integer.bitCount(mask);
        if (locks >= data.entries().size()) return Check.ALL_LOCKED;
        if (!AwakeningCost.has(player, AwakeningCost.forReroll(data.tier(), locks))) return Check.MATERIALS;
        return Check.OK;
    }

    public static void perform(ServerPlayer player, ItemStackHandler slot, int lockMask) {
        if (check(player, slot, lockMask) != Check.OK) return;
        ItemStack stack = slot.getStackInSlot(0).copy();
        AccessoryData data = AccessoryData.read(stack);
        if (data == null) return;
        int mask = sanitize(lockMask, data);

        AwakeningCost.consume(player, AwakeningCost.forReroll(data.tier(), Integer.bitCount(mask)));
        data.reroll(mask, player.getRandom()).write(stack);
        slot.setStackInSlot(0, stack);

        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.7f, 1.1f);
    }
}
