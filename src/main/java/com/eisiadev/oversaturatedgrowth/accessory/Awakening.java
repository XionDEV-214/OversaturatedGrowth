package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * "Accessory Awakening": three accessories of the same shape and tier + materials -> one accessory of the next tier.
 * Used by both sides: the client only calls {@link #check} to enable / explain the button, the server re-checks.
 */
public final class Awakening {

    public static final int INPUTS = 3;

    public enum Check {
        OK,
        NEED_THREE,
        MISMATCH,
        MAX_TIER,
        OUTPUT_FULL,
        MATERIALS
    }

    private Awakening() {}

    /** The tier that the current inputs would awaken into, or null if the inputs are not a valid set. */
    @Nullable
    public static AccessoryTier target(ItemStackHandler in) {
        ItemStack first = in.getStackInSlot(0);
        AccessoryData d = AccessoryData.read(first);
        return d == null ? null : d.tier().next();
    }

    public static Check check(Player player, ItemStackHandler in, ItemStackHandler out) {
        AccessoryItem item = null;
        AccessoryTier tier = null;
        for (int i = 0; i < INPUTS; i++) {
            ItemStack s = in.getStackInSlot(i);
            AccessoryData d = AccessoryData.read(s);
            if (s.isEmpty() || d == null || !(s.getItem() instanceof AccessoryItem ai)) return Check.NEED_THREE;
            if (item == null) {
                item = ai;
                tier = d.tier();
            } else if (ai != item || d.tier() != tier) {
                return Check.MISMATCH;
            }
        }
        if (tier == null || tier.isMax()) return Check.MAX_TIER;
        if (!out.getStackInSlot(0).isEmpty()) return Check.OUTPUT_FULL;
        if (!AwakeningCost.has(player, AwakeningCost.forTarget(tier.next()))) return Check.MATERIALS;
        return Check.OK;
    }

    /** Server side: re-validates, then consumes the inputs + materials and puts the result in the output slot. */
    public static void perform(ServerPlayer player, ItemStackHandler in, ItemStackHandler out) {
        if (check(player, in, out) != Check.OK) return;

        List<AccessoryData> inputs = new ArrayList<>();
        AccessoryItem item = (AccessoryItem) in.getStackInSlot(0).getItem();
        for (int i = 0; i < INPUTS; i++) inputs.add(AccessoryData.read(in.getStackInSlot(i)));
        AccessoryTier target = inputs.get(0).tier().next();

        AccessoryData result = AccessoryData.awaken(target, inputs, player.getRandom());
        AwakeningCost.consume(player, AwakeningCost.forTarget(target));
        for (int i = 0; i < INPUTS; i++) in.setStackInSlot(i, ItemStack.EMPTY);
        out.setStackInSlot(0, AccessoryData.newStack(item.type(), result));

        player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.6f, 1.4f);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.2f);
    }
}
