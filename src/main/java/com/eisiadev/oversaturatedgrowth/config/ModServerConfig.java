package com.eisiadev.oversaturatedgrowth.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModServerConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> retcherMeleeCalPercent;

    public static final ForgeConfigSpec.ConfigValue<Double> motherMeleeCalPercent;

    public static final ForgeConfigSpec.ConfigValue<Integer> globSpitCount;

    public static final ForgeConfigSpec.ConfigValue<Double> globCalPercent;

    public static final ForgeConfigSpec.ConfigValue<Double> globWeightOnFullCalPercent;

    public static final ForgeConfigSpec.ConfigValue<Double> overeatChanceWhenFull;

    public static final ForgeConfigSpec.ConfigValue<Integer> overeatFullDurationSeconds;

    public static final ForgeConfigSpec.ConfigValue<Double> slimeBaseCalPercent;

    public static final ForgeConfigSpec.ConfigValue<Double> slimeBaseWeightPercent;

    public static final ForgeConfigSpec.ConfigValue<Double> fluidCalPercent;

    public static final ForgeConfigSpec.ConfigValue<Double> fluidWeightPercent;

    public static final ForgeConfigSpec.ConfigValue<Double> consumeCalPerHealthPercent;

    public static final ForgeConfigSpec.ConfigValue<Integer> playerConsumeEscapeTimeSec;

    public static final ForgeConfigSpec.ConfigValue<Integer> playerConsumeEscapeKeyPresses;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment(
                "OversaturatedGrowth balance config.",
                "All calorie values are expressed as % of GluttonousGrowth's 'abs_cal_cap' (default 300).",
                "All weight values are expressed as % of the max weight (default 300).",
                "Example: abs_cal_cap=300, value=5.0 → actual amount = 300 * 0.05 = 15."
        ).push("balance");

        builder.comment("── LardRetcher ──").push("lard_retcher");
        retcherMeleeCalPercent = builder
                .comment("Calorie gain on melee hit, as % of abs_cal_cap. Default: ~5.3% (=16/300)")
                .defineInRange("melee_cal_percent", 5.3, 0.0, 100.0);
        builder.pop();

        builder.comment("── MotherRetcher ──").push("mother_retcher");
        motherMeleeCalPercent = builder
                .comment("Calorie gain on melee hit, as % of abs_cal_cap. Default: ~13.3% (=40/300)")
                .defineInRange("melee_cal_percent", 13.3, 0.0, 100.0);
        builder.pop();

        builder.comment("── LardGlob ──").push("lard_glob");
        globSpitCount = builder
                .comment("Number of LardGlobs spat at once by every Lard-spitting mob (Retcher shotgun, Mother per burst shot). Default: 3")
                .defineInRange("spit_count", 3, 1, 50);
        globCalPercent = builder
                .comment("Calorie gain per LardGlob hit, as % of abs_cal_cap. LardGlobs deal no damage. Default: 2.0%")
                .defineInRange("cal_percent", 2.0, 0.0, 100.0);
        globWeightOnFullCalPercent = builder
                .comment("Weight gained when a LardGlob hits a player with full calories, as % of max_weight. Default: ~0.67% (=2/300)")
                .defineInRange("weight_on_full_cal_percent", 0.67, 0.0, 100.0);
        builder.pop();

        builder.comment("── Overeat ──").push("overeat");
        overeatChanceWhenFull = builder
                .comment("Chance (%) to get Overeat each time you eat something while your calories are already full. Default: 30%")
                .defineInRange("chance_when_full_percent", 30.0, 0.0, 100.0);
        overeatFullDurationSeconds = builder
                .comment("Duration (seconds) of the Overeat applied that way. Default: 15")
                .defineInRange("duration_when_full_seconds", 15, 1, 600);
        builder.pop();

        builder.comment("── LardSlime ──").push("lard_slime");
        slimeBaseCalPercent = builder
                .comment("Base calorie gain (size 1) on melee hit, as % of abs_cal_cap. Doubles per size tier. Default: 1.0% (=3/300)")
                .defineInRange("base_cal_percent", 1.0, 0.0, 100.0);
        slimeBaseWeightPercent = builder
                .comment("Base weight gain (size 1) on melee hit, as % of max_weight. Doubles per size tier. Default: ~0.33% (=1/300)")
                .defineInRange("base_weight_percent", 0.33, 0.0, 100.0);
        builder.pop();

        builder.comment("── RefinedLard Fluid (per tick while submerged, interval: every 5 ticks) ──").push("refined_lard_fluid");
        fluidCalPercent = builder
                .comment("Calorie gain per interval tick while submerged in Refined Lard fluid, as % of abs_cal_cap. Default: ~0.67% (=2/300)")
                .defineInRange("cal_percent", 0.67, 0.0, 100.0);
        fluidWeightPercent = builder
                .comment("Weight gain per interval tick when calories are full, as % of max_weight. Default: ~0.33% (=1/300)")
                .defineInRange("weight_percent", 0.33, 0.0, 100.0);
        builder.pop();

        builder.comment("── Entity Consume ──").push("entity_consume");
        consumeCalPerHealthPercent = builder
                .comment("Calorie gain per 1 max-health point of the consumed entity, as % of abs_cal_cap. Default: ~1.67% (=5/300)")
                .defineInRange("cal_per_health_percent", 1.67, 0.0, 100.0);
        builder.pop();

        builder.comment("── Player Consume ──").push("player_consume");
        playerConsumeEscapeTimeSec = builder
                .comment("Time limit (seconds) for the consumed player to escape. -1 = no limit.")
                .defineInRange("escape_time_sec", 10, -1, 300);
        playerConsumeEscapeKeyPresses = builder
                .comment("Total number of WASD key presses needed to escape from a predator player.")
                .defineInRange("escape_key_presses", 50, 1, 500);
        builder.pop();

        builder.pop();
        SPEC = builder.build();
    }
}
