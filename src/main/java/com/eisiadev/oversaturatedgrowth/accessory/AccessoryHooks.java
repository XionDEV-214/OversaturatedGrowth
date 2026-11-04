package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.world.entity.player.Player;

/**
 * Glue for the Mixins into Gluttonous Growth. While GG's {@code ModEvent.stuffedSystem} runs for a player
 * (the calorie -> weight conversion tick), that player is remembered here so the other Mixins know whose
 * accessories apply. Everything happens synchronously on the server thread.
 */
public final class AccessoryHooks {

    private static final ThreadLocal<Player> STUFFED = new ThreadLocal<>();

    private AccessoryHooks() {}

    public static void enterStuffed(Player player) {
        STUFFED.set(player);
    }

    public static void exitStuffed() {
        STUFFED.remove();
    }

    public static boolean inStuffedSystem() {
        return STUFFED.get() != null;
    }

    /** Applies Absorption to a weight amount GG is about to queue; fractions are rounded randomly so small gains still scale. */
    public static int absorb(int weight) {
        Player player = STUFFED.get();
        if (player == null || weight <= 0) return weight;
        double multiplier = AccessoryEffects.absorptionMultiplier(player);
        if (multiplier <= 1.0) return weight;
        double exact = weight * multiplier;
        int whole = (int) exact;
        return whole + (player.getRandom().nextDouble() < exact - whole ? 1 : 0);
    }
}
