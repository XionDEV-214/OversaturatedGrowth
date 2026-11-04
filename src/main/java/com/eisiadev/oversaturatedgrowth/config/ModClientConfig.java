package com.eisiadev.oversaturatedgrowth.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue hudEnabled;
    public static final ForgeConfigSpec.BooleanValue hideGluttonousHud;
    /** HUD offset from the bottom-right corner (GUI pixels). */
    public static final ForgeConfigSpec.ConfigValue<Integer> weightGaugeX;
    public static final ForgeConfigSpec.ConfigValue<Integer> weightGaugeY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("OversaturatedGrowth client-side settings").push("hud");

        hudEnabled = builder
                .comment("Show the Oversaturated Growth HUD (weight, calories, stomach growth, Fattaberry inflation).")
                .define("enabled", true);

        hideGluttonousHud = builder
                .comment("Hide Gluttonous Growth's own stomach / belly icons while the HUD above is shown.",
                        "Gluttonous Growth's debug view still appears while its debug option is on.")
                .define("hide_gluttonous_hud", true);

        weightGaugeX = builder
                .comment("HUD X offset from the bottom-right corner (GUI pixels, positive = further left)")
                .define("offset_x", 0);

        weightGaugeY = builder
                .comment("HUD Y offset from the bottom-right corner (GUI pixels, positive = further up)")
                .define("offset_y", 0);

        builder.pop();
        SPEC = builder.build();
    }

    public static void save() {
        hudEnabled.save();
        hideGluttonousHud.save();
        weightGaugeX.save();
        weightGaugeY.save();
    }
}
