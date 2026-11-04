package com.eisiadev.oversaturatedgrowth.accessory;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Materials consumed from the player's inventory (never from the bag).
 * <ul>
 *   <li>{@link #forTarget}: awakening INTO a tier, on top of the three accessories. The three-accessories-per-step rule
 *       already makes every tier ~3x more expensive than the last, so the material counts only grow slowly.</li>
 *   <li>{@link #forReroll}: one option reroll. Base cost grows with the tier, every locked line adds the base cost
 *       once more (0 locks = x1, 1 lock = x2, 2 locks = x3 ...).</li>
 * </ul>
 */
public final class AwakeningCost {

    public record Req(Supplier<Item> item, int count) {
        public ItemStack icon() {
            return new ItemStack(item.get());
        }
    }

    private static final Supplier<Item> LARDSHROOM = () -> ModBlocks.LARDSHROOM_ITEM.get();
    private static final Supplier<Item> LARDLOG = () -> ModBlocks.LARDLOG.get().asItem();
    private static final Supplier<Item> CRYSTALLIZED = () -> ModItems.CRYSTALLIZED_FATTABERRY.get();
    private static final Supplier<Item> FATTADIUM = () -> ModItems.FATTADIUM.get();
    private static final Supplier<Item> SLUDGE_BALL = () -> ModItems.LARD_SLUDGE_BALL.get();
    private static final Supplier<Item> REFINED_LARD = () -> ModItems.REFINED_LARD.get();

    private AwakeningCost() {}

    /** {lardshroom, lardlog, crystallized fattaberry, fattadium} per target tier (index = tier ordinal). */
    private static final int[][] AWAKEN = {
            {0, 0, 0, 0},       // Common (never a target)
            {5, 0, 0, 0},       // Uncommon
            {11, 4, 0, 0},      // Rare
            {16, 8, 4, 0},      // Epic
            {21, 16, 8, 4},     // Legendary
            {32, 24, 16, 8},    // Mythic
            {43, 32, 24, 16},   // Divine
            {64, 48, 32, 32},   // Eternal
            {85, 64, 48, 48},   // Zenith
            {128, 96, 64, 64},  // Helix
    };

    public static List<Req> forTarget(AccessoryTier target) {
        int[] c = AWAKEN[target.ordinal()];
        List<Req> list = new ArrayList<>(4);
        if (c[0] > 0) list.add(new Req(LARDSHROOM, c[0]));
        if (c[1] > 0) list.add(new Req(LARDLOG, c[1]));
        if (c[2] > 0) list.add(new Req(CRYSTALLIZED, c[2]));
        if (c[3] > 0) list.add(new Req(FATTADIUM, c[3]));
        return list;
    }

    /** Reroll: Lard Sludge Ball {@code 4 + 2*tier}, Refined Lard {@code 1 + tier/2}, both x (1 + locked lines). */
    public static List<Req> forReroll(AccessoryTier tier, int locks) {
        int mult = 1 + Math.max(0, locks);
        int t = tier.ordinal();
        return List.of(new Req(SLUDGE_BALL, (4 + 2 * t) * mult), new Req(REFINED_LARD, (1 + t / 2) * mult));
    }

    public static int count(Player player, Item item) {
        int total = 0;
        for (ItemStack s : player.getInventory().items) if (s.is(item)) total += s.getCount();
        for (ItemStack s : player.getInventory().offhand) if (s.is(item)) total += s.getCount();
        return total;
    }

    public static boolean has(Player player, List<Req> reqs) {
        for (Req r : reqs) if (count(player, r.item().get()) < r.count()) return false;
        return true;
    }

    public static void consume(Player player, List<Req> reqs) {
        for (Req r : reqs) {
            int left = r.count();
            Item item = r.item().get();
            for (ItemStack s : player.getInventory().items) {
                if (left <= 0) break;
                if (!s.is(item)) continue;
                int take = Math.min(left, s.getCount());
                s.shrink(take);
                left -= take;
            }
            for (ItemStack s : player.getInventory().offhand) {
                if (left <= 0) break;
                if (!s.is(item)) continue;
                int take = Math.min(left, s.getCount());
                s.shrink(take);
                left -= take;
            }
        }
        player.getInventory().setChanged();
    }
}
