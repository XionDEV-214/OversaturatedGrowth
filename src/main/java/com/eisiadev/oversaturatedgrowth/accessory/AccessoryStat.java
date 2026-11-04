package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * The five stat families. A stat value is stored in "display units" inside the item NBT:
 * <ul>
 *   <li>MAX_CALORIES - flat calories added to Gluttonous Growth's max calories (on top of abs_cal_cap)</li>
 *   <li>DIGESTION    - extra % of max calories turned into weight per digestion burst (base 5%)</li>
 *   <li>MOVEMENT   - % of Gluttonous Growth's weight speed penalty that is cancelled</li>
 *   <li>HEALTH     - extra max HP at full weight (scales with the weight bar)</li>
 *   <li>ABSORPTION - % more weight gained per calorie converted</li>
 * </ul>
 * The tooltip colour of a stat line is derived from where its value sits inside the tier's roll range
 * (weak - base - strong). The item itself is tinted with random pastel colours, see {@link AccessoryData}.
 */
public enum AccessoryStat {
    MAX_CALORIES("max_calories", 5.0, 0xFFDCDC, 0xFFAEC5, 0xFF4382),
    DIGESTION("digestion", 0.06, 0xFFF700, 0xD4FF00, 0x84FF00),
    MOVEMENT("movement", 2.0, 0x88FFFF, 0x00B3FF, 0xEDBDFF),
    HEALTH("health", 0.5, 0xFF9494, 0xFF2B2B, 0xFF0000),
    ABSORPTION("absorption", 2.0, 0xFF7F27, 0xFFA41F, 0xFFCA18);

    public static final AccessoryStat[] VALUES = values();

    public final String id;
    /** Lower bound of the Common tier roll range. Higher tiers multiply this by {@link AccessoryTier#multiplier}. */
    public final double unit;
    private final int weak;
    private final int mid;
    private final int strong;

    AccessoryStat(String id, double unit, int weak, int mid, int strong) {
        this.id = id;
        this.unit = unit;
        this.weak = weak;
        this.mid = mid;
        this.strong = strong;
    }

    public double min(AccessoryTier tier) {
        return unit * tier.multiplier;
    }

    public double max(AccessoryTier tier) {
        return min(tier) * AccessoryTier.SPREAD;
    }

    /** Value at position {@code f} (0 = weakest, 1 = strongest) of the tier's roll range. */
    public double valueAt(AccessoryTier tier, double f) {
        double lo = min(tier);
        double hi = max(tier);
        double v = lo + (hi - lo) * Math.max(0.0, Math.min(1.0, f));
        return Math.round(v * 1000.0) / 1000.0;
    }

    /** Where a value sits inside the tier's roll range, clamped to 0..1. */
    public float fractionOf(AccessoryTier tier, double value) {
        double lo = min(tier);
        double hi = max(tier);
        return (float) Math.max(0.0, Math.min(1.0, (value - lo) / (hi - lo)));
    }

    /** 3-point gradient: weak at 0, base colour at 0.5, strong at 1. */
    public int color(float f) {
        f = Math.max(0f, Math.min(1f, f));
        return f < 0.5f ? lerp(weak, mid, f * 2f) : lerp(mid, strong, (f - 0.5f) * 2f);
    }

    public int baseColor() {
        return mid;
    }

    public int color(AccessoryTier tier, double value) {
        return color(fractionOf(tier, value));
    }

    /** Roll quality inside the tier's range, 0..100 (shown as "[ 72% ]"). */
    public int percentOf(AccessoryTier tier, double value) {
        return Math.round(fractionOf(tier, value) * 100f);
    }

    public Component displayName() {
        return Component.translatable("accessory.oversaturatedgrowth.stat." + id);
    }

    public String format(double v) {
        return switch (this) {
            case MAX_CALORIES -> String.format(Locale.ROOT, "+%d", Math.round(v));
            case MOVEMENT, ABSORPTION -> String.format(Locale.ROOT, "+%.1f%%", v);
            case DIGESTION -> String.format(Locale.ROOT, "+%.2f%%", v);
            case HEALTH -> String.format(Locale.ROOT, "+%.1f HP", v);
        };
    }

    public static AccessoryStat byId(String id) {
        for (AccessoryStat s : VALUES) if (s.id.equals(id)) return s;
        if ("capacity".equals(id)) return MAX_CALORIES; // 2.1.0 dev builds
        return null;
    }

    private static int lerp(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
