package com.eisiadev.oversaturatedgrowth.accessory;

import com.eisiadev.oversaturatedgrowth.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The per-stack payload of an accessory:
 * <pre>
 * { OGAccessory: { tier: 4, colors: [I; band, gem], stats: [ {id:"max_calories", value:17.3d}, ... ] } }
 * </pre>
 * The two item layers are tinted with random pastel colours chosen when the accessory is created ({@code colors});
 * stats only colour the tooltip lines. Stat order is kept as rolled, so rerolling with locks never shuffles lines.
 */
public final class AccessoryData {

    public static final String ROOT = "OGAccessory";

    public record Entry(AccessoryStat stat, double value) {}

    private final AccessoryTier tier;
    private final List<Entry> entries;
    /** [0] = band / string / leather / metal (tint 0), [1] = gem / core (tint 1). RGB. */
    private final int bandColor;
    private final int gemColor;

    private AccessoryData(AccessoryTier tier, List<Entry> entries, int bandColor, int gemColor) {
        this.tier = tier;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        this.bandColor = bandColor;
        this.gemColor = gemColor;
    }

    public AccessoryTier tier() {
        return tier;
    }

    public List<Entry> entries() {
        return entries;
    }

    public int bandColor() {
        return bandColor;
    }

    public int gemColor() {
        return gemColor;
    }

    // ---------------------------------------------------------------- NBT

    @Nullable
    public static AccessoryData read(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag root = stack.getTagElement(ROOT);
        if (root == null) return null;
        AccessoryTier tier = AccessoryTier.byIndex(root.getInt("tier"));
        List<Entry> list = new ArrayList<>();
        ListTag stats = root.getList("stats", Tag.TAG_COMPOUND);
        for (int i = 0; i < stats.size(); i++) {
            CompoundTag t = stats.getCompound(i);
            AccessoryStat stat = AccessoryStat.byId(t.getString("id"));
            if (stat != null) list.add(new Entry(stat, t.getDouble("value")));
        }
        int[] colors = root.getIntArray("colors");
        int band;
        int gem;
        if (colors.length >= 2) {
            band = colors[0];
            gem = colors[1];
        } else {
            // Accessories made before colours were stored: stable colours derived from the stats.
            RandomSource r = RandomSource.create(list.hashCode() * 31L + tier.ordinal());
            gem = pastel(r);
            band = bandFor(gem, r);
        }
        return new AccessoryData(tier, list, band, gem);
    }

    public void write(ItemStack stack) {
        CompoundTag root = new CompoundTag();
        root.putInt("tier", tier.ordinal());
        root.putIntArray("colors", new int[] {bandColor, gemColor});
        ListTag stats = new ListTag();
        for (Entry e : entries) {
            CompoundTag t = new CompoundTag();
            t.putString("id", e.stat().id);
            t.putDouble("value", e.value());
            stats.add(t);
        }
        root.put("stats", stats);
        stack.getOrCreateTag().put(ROOT, root);
    }

    public static ItemStack newStack(AccessoryType type, AccessoryData data) {
        ItemStack stack = new ItemStack(ModItems.accessory(type));
        data.write(stack);
        return stack;
    }

    // ---------------------------------------------------------------- colours

    /** Item colour handler: tint 1 = gem / core, tint 0 = string / leather / metal. */
    public static int tint(ItemStack stack, int tintIndex) {
        AccessoryData d = read(stack);
        if (d == null) return 0xFFFFFF;
        return tintIndex == 1 ? d.gemColor : d.bandColor;
    }

    /** A soft pastel: any hue, low saturation, high brightness. */
    public static int pastel(RandomSource r) {
        float hue = r.nextFloat();
        float sat = 0.28f + r.nextFloat() * 0.20f;
        float val = 0.93f + r.nextFloat() * 0.07f;
        return Mth.hsvToRgb(hue, sat, val) & 0xFFFFFF;
    }

    /** Band colour that goes with a gem colour: shifted hue, a bit softer, or a near-white / pale gold metal. */
    private static int bandFor(int gem, RandomSource r) {
        if (r.nextFloat() < 0.35f) {
            // pale "metal" tones
            float hue = r.nextBoolean() ? 0.11f + r.nextFloat() * 0.04f : 0.55f + r.nextFloat() * 0.1f;
            return Mth.hsvToRgb(hue, 0.08f + r.nextFloat() * 0.12f, 0.95f) & 0xFFFFFF;
        }
        float[] hsv = rgbToHsv(gem);
        float hue = (hsv[0] + 0.25f + r.nextFloat() * 0.5f) % 1f;
        return Mth.hsvToRgb(hue, 0.20f + r.nextFloat() * 0.18f, 0.94f + r.nextFloat() * 0.06f) & 0xFFFFFF;
    }

    private static float[] rgbToHsv(int rgb) {
        float r = (rgb >> 16 & 0xFF) / 255f;
        float g = (rgb >> 8 & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h;
        if (d == 0) h = 0;
        else if (max == r) h = ((g - b) / d) / 6f;
        else if (max == g) h = ((b - r) / d + 2f) / 6f;
        else h = ((r - g) / d + 4f) / 6f;
        if (h < 0) h += 1f;
        return new float[] {h, max == 0 ? 0 : d / max, max};
    }

    // ---------------------------------------------------------------- rolling

    /** Weighted pick of a tier in {@code [min, max]}: every step up is ~45% less likely. */
    public static AccessoryTier rollTier(RandomSource r, AccessoryTier min, AccessoryTier max) {
        if (max.ordinal() < min.ordinal()) max = min;
        double total = 0;
        for (int i = min.ordinal(); i <= max.ordinal(); i++) total += Math.pow(0.55, i);
        double roll = r.nextDouble() * total;
        for (int i = min.ordinal(); i <= max.ordinal(); i++) {
            roll -= Math.pow(0.55, i);
            if (roll <= 0) return AccessoryTier.VALUES[i];
        }
        return max;
    }

    /** Common..Epic: 2 lines, Legendary 3 ... Helix 8. The same stat may appear on several lines; every line counts. */
    private static int rollCount(AccessoryTier tier, RandomSource r) {
        return tier.maxStats();
    }

    /** A brand-new accessory: fresh colours and a full set of lines for its tier. */
    public static AccessoryData generate(AccessoryTier tier, RandomSource r) {
        int gem = pastel(r);
        return new AccessoryData(tier, rollEntries(tier, rollCount(tier, r), r), bandFor(gem, r), gem);
    }

    private static List<Entry> rollEntries(AccessoryTier tier, int count, RandomSource r) {
        List<Entry> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(randomEntry(tier, r));
        return list;
    }

    public static Entry randomEntry(AccessoryTier tier, RandomSource r) {
        AccessoryStat stat = AccessoryStat.VALUES[r.nextInt(AccessoryStat.VALUES.length)];
        return new Entry(stat, stat.valueAt(tier, r.nextDouble()));
    }

    /** Deterministic sample for the creative tab. */
    public static AccessoryData sample(AccessoryType type, AccessoryTier tier) {
        return generate(tier, RandomSource.create(7919L * (type.ordinal() + 1) + 104729L * (tier.ordinal() + 1)));
    }

    // ---------------------------------------------------------------- reroll

    /**
     * Option reroll. Locked lines (bit {@code i} of {@code lockMask} = line {@code i}) keep stat and value; every other
     * line gets a new random stat and value of the same tier. Accessories from before the "always 2 lines" rule are
     * topped up to their tier's line count on a reroll. Colours are kept.
     */
    public AccessoryData reroll(int lockMask, RandomSource r) {
        if (lockMask == 0) {
            return new AccessoryData(tier, rollEntries(tier, rollCount(tier, r), r), bandColor, gemColor);
        }
        List<Entry> list = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            list.add((lockMask & (1 << i)) != 0 ? entries.get(i) : randomEntry(tier, r));
        }
        while (list.size() < tier.maxStats()) list.add(randomEntry(tier, r));
        return new AccessoryData(tier, list, bandColor, gemColor);
    }

    // ---------------------------------------------------------------- awakening

    /**
     * Result of awakening three accessories of the same type and tier into {@code target}.
     * <ul>
     *   <li>target up to Epic: stats are re-rolled from scratch inside the new (slightly higher) range.</li>
     *   <li>target Legendary or above: {@code maxStats - 1} stats are drawn at random from the pool of all stats on the
     *       three inputs and keep their relative strength inside the new range, then one fresh random stat is added.</li>
     * </ul>
     * The result gets new random colours.
     */
    public static AccessoryData awaken(AccessoryTier target, List<AccessoryData> inputs, RandomSource r) {
        if (!target.isMultiStat()) return generate(target, r);

        List<Entry> pool = new ArrayList<>();
        List<AccessoryTier> poolTier = new ArrayList<>();
        for (AccessoryData d : inputs) {
            for (Entry e : d.entries) {
                pool.add(e);
                poolTier.add(d.tier);
            }
        }
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < pool.size(); i++) order.add(i);
        Collections.shuffle(order, new java.util.Random(r.nextLong()));

        int inherit = Math.min(target.maxStats() - 1, pool.size());
        List<Entry> result = new ArrayList<>();
        for (int i = 0; i < inherit; i++) {
            int idx = order.get(i);
            Entry e = pool.get(idx);
            float f = e.stat().fractionOf(poolTier.get(idx), e.value());
            result.add(new Entry(e.stat(), e.stat().valueAt(target, f)));
        }
        while (result.size() < target.maxStats()) result.add(randomEntry(target, r));
        int gem = pastel(r);
        return new AccessoryData(target, result, bandFor(gem, r), gem);
    }
}
