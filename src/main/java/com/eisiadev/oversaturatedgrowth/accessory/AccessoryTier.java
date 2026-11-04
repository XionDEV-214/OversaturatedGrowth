package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import javax.annotation.Nullable;

/**
 * The ten accessory grades. {@link #color} is the colour of the item name; stat colours come from {@link AccessoryStat}.
 * <p>
 * Balance knobs (all in one place):
 * <ul>
 *   <li>{@link #multiplier}: every stat rolls inside {@code [unit * multiplier, unit * multiplier * SPREAD]}.</li>
 *   <li>{@link #maxStats()}: Common..Epic always hold 2 stats; from Legendary on every grade holds one more
 *       (Legendary 3 ... Helix 8).</li>
 * </ul>
 */
public enum AccessoryTier {
    COMMON("common", 0xC3C3C3, 1.0),
    UNCOMMON("uncommon", 0x55FF00, 1.5),
    RARE("rare", 0x0088FF, 2.2),
    EPIC("epic", 0xC300FF, 3.3),
    LEGENDARY("legendary", 0xFFBF00, 5.0),
    MYTHIC("mythic", 0xFFA3F3, 7.5),
    DIVINE("divine", 0x00E5FF, 11.0),
    ETERNAL("eternal", 0xC50000, 16.0),
    ZENITH("zenith", 0x008D60, 24.0),
    HELIX("helix", 0x00FFD5, 36.0);

    public static final AccessoryTier[] VALUES = values();

    /** Top of a tier's roll range relative to its bottom. Must stay below the tier-to-tier ratio (1.4 < 1.5). */
    public static final double SPREAD = 1.4;

    public final String id;
    public final int color;
    public final double multiplier;

    AccessoryTier(String id, int color, double multiplier) {
        this.id = id;
        this.color = color;
        this.multiplier = multiplier;
    }

    /** Legendary and above gain an extra stat on every awakening. */
    public boolean isMultiStat() {
        return ordinal() >= LEGENDARY.ordinal();
    }

    public int maxStats() {
        return isMultiStat() ? ordinal() - 1 : 2;
    }

    public boolean isMax() {
        return this == HELIX;
    }

    @Nullable
    public AccessoryTier next() {
        return isMax() ? null : VALUES[ordinal() + 1];
    }

    public static AccessoryTier byIndex(int index) {
        return VALUES[Math.max(0, Math.min(VALUES.length - 1, index))];
    }

    public static AccessoryTier byId(String id, AccessoryTier fallback) {
        for (AccessoryTier t : VALUES) if (t.id.equals(id)) return t;
        return fallback;
    }

    public Component displayName() {
        return Component.translatable("accessory.oversaturatedgrowth.tier." + id)
                .withStyle(Style.EMPTY.withColor(color));
    }
}
