package com.eisiadev.oversaturatedgrowth.util;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;

public final class BalanceHelper {

    private static final int DEFAULT_MAX_WEIGHT = 300;

    private BalanceHelper() {}

    public static int scaleCal(double percent) {
        int cap = GgBridge.absCalCap();
        return Math.max(1, (int) Math.round(cap * percent / 100.0));
    }

    public static int scaleWeight(double percent) {
        return Math.max(1, (int) Math.round(DEFAULT_MAX_WEIGHT * percent / 100.0));
    }
}
