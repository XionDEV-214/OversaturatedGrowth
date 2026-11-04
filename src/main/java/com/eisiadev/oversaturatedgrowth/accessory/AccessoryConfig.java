package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Hard caps for the summed accessory stats plus the digestion curve.
 * File: oversaturatedgrowth-accessories.toml (SERVER, synced to clients).
 * Every getter falls back to the default while the config is not loaded yet (e.g. a client in the main menu).
 */
public final class AccessoryConfig {

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.DoubleValue CAPACITY_CAP;
    private static final ForgeConfigSpec.DoubleValue DIGESTION_CAP;
    private static final ForgeConfigSpec.DoubleValue MOVEMENT_CAP;
    private static final ForgeConfigSpec.DoubleValue HEALTH_CAP;
    private static final ForgeConfigSpec.DoubleValue ABSORPTION_CAP;
    private static final ForgeConfigSpec.DoubleValue DIGEST_MAX;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Caps applied to the SUM of all accessories in the Accessory Bag.").push("caps");
        CAPACITY_CAP = b.comment("Max extra Max Calories (flat calories, added on top of Gluttonous Growth's own capacity).")
                .defineInRange("max_calories", 1500.0, 0.0, 1000000.0);
        DIGESTION_CAP = b.comment("Max extra % of max calories digested per burst (added to the base 5%).")
                .defineInRange("digestion_percent", 90.0, 0.0, 100.0);
        MOVEMENT_CAP = b.comment("Max % of the weight speed penalty that can be cancelled. 100 = no slowdown at all.")
                .defineInRange("movement_percent", 100.0, 0.0, 100.0);
        HEALTH_CAP = b.comment("Max extra HP at full weight (scaled down linearly with the weight bar).")
                .defineInRange("health_hp", 100.0, 0.0, 10000.0);
        ABSORPTION_CAP = b.comment("Max extra weight per converted calorie, in %. 300 = x4 weight.")
                .defineInRange("absorption_percent", 300.0, 0.0, 100000.0);
        b.pop();
        b.comment("How much one digestion burst digests (every ~20 s, faster while walking / sprinting).").push("digestion");
        DIGEST_MAX = b.comment("Max % of max calories digested per step.")
                .defineInRange("max_percent", 95.0, 0.0, 100.0);
        b.pop();
        SPEC = b.build();
    }

    private AccessoryConfig() {}

    private static double get(ForgeConfigSpec.DoubleValue v, double fallback) {
        try {
            return SPEC.isLoaded() ? v.get() : fallback;
        } catch (IllegalStateException e) {
            return fallback;
        }
    }

    public static double maxCaloriesCap() { return get(CAPACITY_CAP, 1500.0); }
    public static double digestionCap() { return get(DIGESTION_CAP, 90.0); }
    public static double movementCapPercent() { return get(MOVEMENT_CAP, 100.0); }
    public static double healthCap() { return get(HEALTH_CAP, 100.0); }
    public static double absorptionCapPercent() { return get(ABSORPTION_CAP, 300.0); }
    public static double digestMaxPercent() { return get(DIGEST_MAX, 95.0); }
}
