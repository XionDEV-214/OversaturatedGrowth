package com.eisiadev.oversaturatedgrowth.effect;

import com.eisiadev.oversaturatedgrowth.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Fattaberry inflation. The effects only set a target level ({@link #effectiveLevel}, 0..6); the actual inflation
 * {@link #amount} (also 0..6, fractional) eases toward it every tick in InflationEvents, so going from one level to
 * the next always grows smoothly instead of jumping.
 */
public final class InflationHelper {
    private InflationHelper() {}
    public static final String KEY_AMOUNT = "og_infl_amount", KEY_Q = "og_infl_q";
    /** Old keys (level x ramp model), only cleared on load. */
    public static final String KEY_LVL = "og_infl_lvl", KEY_RAMP = "og_infl_ramp";
    public static final int MAX_LEVEL = 5;
    /** Highest possible target: Fattaberry Ate V + Fattaberry Stomach. */
    public static final int MAX_AMOUNT = MAX_LEVEL + 1;

    public static int effectiveLevel(Player p) {
        MobEffectInstance e = p.getEffect(ModEffects.FATTABERRY_ATE.get());
        if (e == null) return 0;
        int l = Math.min(MAX_LEVEL, e.getAmplifier() + 1);
        if (p.hasEffect(ModEffects.FATTABERRY_STOMACH.get())) l++;
        return l;
    }

    /** Current inflation, 0..6 (fractional while growing / shrinking). */
    public static float amount(Player p) {
        return p.getPersistentData().getFloat(KEY_AMOUNT);
    }

    /** Compatibility for older callers: level() * ramp() == amount(). */
    @Deprecated
    public static float level(Player p) {
        float a = amount(p);
        return a <= 0f ? 0f : (float) Math.ceil(a);
    }

    /** Compatibility for older callers: level() * ramp() == amount(). */
    @Deprecated
    public static float ramp(Player p) {
        float l = level(p);
        return l <= 0f ? 0f : amount(p) / l;
    }

    public static float scale(Player p) { return 1.0f + 0.10f * amount(p); }
}
