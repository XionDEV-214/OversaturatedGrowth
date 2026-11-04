package com.eisiadev.oversaturatedgrowth.entity;

public interface IPlump {
    int MAX_PLUMP = 5;

    float WIDE = 1.2f;

    int getPlumpLevel();
    void setPlumpLevel(int level);

    default boolean isPlump() { return getPlumpLevel() > 0; }

    static float scaleFor(int level) { return 1.0f + 0.08f * Math.max(0, Math.min(MAX_PLUMP, level)); }
}
