package com.eisiadev.oversaturatedgrowth.bridge;

import net.minecraft.server.level.ServerPlayer;
import net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeterProvider;
import net.willsbr.gluttonousgrowth.WeightSystem.PlayerWeightBarProvider;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;
import net.willsbr.gluttonousgrowth.networking.ModMessages;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.CalorieMeterDelaySyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.OverfullFoodDataSyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.WeightPackets.WeightBarDataSyncPacketS2C;

public final class GgBridge {

    private GgBridge() {}

    public static int absCalCap() {
        return GluttonousWorldConfig.absCalCap.get();
    }

    public static boolean isCalorieFull(ServerPlayer player) {
        final boolean[] full = {false};
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm ->
                full[0] = cm.getCurrentCalories() >= cm.getMaxCalories());
        return full[0];
    }

    public static boolean addCalories(ServerPlayer player, int amount) {
        final boolean[] added = {false};
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm -> {
            if (cm.getCurrentCalories() >= cm.getMaxCalories()) return;
            cm.addCalories(amount);
            if (cm.getFoodEatenTick() == -1) cm.setFoodEatenTick(player.tickCount);
            cm.setCalClearDelay(GluttonousWorldConfig.maxCalClearDelay.get());
            ModMessages.sendToPlayer(new OverfullFoodDataSyncPacketS2C(
                    cm.getCurrentCalories(), cm.getMaxCalories(),
                    cm.getModMetabolismThres(), cm.getSlowMetabolismThres()), player);
            ModMessages.sendToPlayer(new CalorieMeterDelaySyncPacketS2C(
                    cm.getCalClearDelay(), cm.getRemainingTicks(player.tickCount)), player);
            added[0] = true;
        });
        return added[0];
    }

    public static void addWeight(ServerPlayer player, int amount) {
        player.getCapability(PlayerWeightBarProvider.PLAYER_WEIGHT_BAR).ifPresent(wb -> {
            int next = Math.min(wb.getCurrentWeight() + amount, wb.getCurMaxWeight());
            wb.setCurrentWeight(next);
            ModMessages.sendToPlayer(new WeightBarDataSyncPacketS2C(wb.getCurrentWeight()), player);
        });
    }

    public static float drainWeight(ServerPlayer player, int amount) {
        final float[] result = {-1f};
        player.getCapability(PlayerWeightBarProvider.PLAYER_WEIGHT_BAR).ifPresent(wb -> {
            int current = wb.getCurrentWeight();
            int min = wb.getMinWeight();
            int max = wb.getCurMaxWeight();
            if (current - amount < min) return;
            float ratio = max > min ? Math.max(0f, Math.min(1f, (float) (current - min) / (max - min))) : 0f;
            wb.setCurrentWeight(current - amount);
            ModMessages.sendToPlayer(new WeightBarDataSyncPacketS2C(wb.getCurrentWeight()), player);
            result[0] = ratio;
        });
        return result[0];
    }

    public static int currentCalories(ServerPlayer player) {
        final int[] r = {0};
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm -> r[0] = cm.getCurrentCalories());
        return r[0];
    }

    public static int maxCalories(ServerPlayer player) {
        final int[] r = {0};
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm -> r[0] = cm.getMaxCalories());
        return r[0];
    }

    public static float calorieRatio(ServerPlayer player) {
        int max = maxCalories(player);
        return max <= 0 ? 0f : Math.max(0f, Math.min(1f, (float) currentCalories(player) / max));
    }

    public static float weightRatio(ServerPlayer player) {
        final float[] r = {0f};
        player.getCapability(PlayerWeightBarProvider.PLAYER_WEIGHT_BAR).ifPresent(wb -> {
            int min = wb.getMinWeight(), max = wb.getCurMaxWeight();
            if (max > min) r[0] = Math.max(0f, Math.min(1f, (float) (wb.getCurrentWeight() - min) / (max - min)));
        });
        return r[0];
    }

    private static void syncCalories(ServerPlayer player, net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeter cm) {
        ModMessages.sendToPlayer(new OverfullFoodDataSyncPacketS2C(
                cm.getCurrentCalories(), cm.getMaxCalories(),
                cm.getModMetabolismThres(), cm.getSlowMetabolismThres()), player);
    }

    public static void drainCalories(ServerPlayer player, int amount) {
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm -> {
            cm.setCurrentCalories(Math.max(0, cm.getCurrentCalories() - amount));
            syncCalories(player, cm);
        });
    }

    public static void stallCalorieClock(ServerPlayer player, int ticks) {
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm -> {
            if (cm.getFoodEatenTick() != -1) cm.setFoodEatenTick(cm.getFoodEatenTick() + ticks);
        });
    }

    public static void syncCalorieDelay(ServerPlayer player) {
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm ->
                ModMessages.sendToPlayer(new CalorieMeterDelaySyncPacketS2C(cm.getCalClearDelay(), cm.getRemainingTicks(player.tickCount)), player));
    }

    public static void adjustMaxCalories(ServerPlayer player, int delta) {
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm -> {
            int next = Math.max(1, cm.getMaxCalories() + delta);
            cm.setMaxCalories(next);
            if (cm.getCurrentCalories() > next) cm.setCurrentCalories(next);
            syncCalories(player, cm);
        });
    }

    public static void setMaxCalories(ServerPlayer player, int value) {
        adjustMaxCalories(player, value - maxCalories(player));
    }

    public static void loseWeight(ServerPlayer player, int amount) {
        player.getCapability(PlayerWeightBarProvider.PLAYER_WEIGHT_BAR).ifPresent(wb -> {
            int next = Math.max(wb.getMinWeight(), wb.getCurrentWeight() - amount);
            if (next == wb.getCurrentWeight()) return;
            wb.setCurrentWeight(next);
            ModMessages.sendToPlayer(new WeightBarDataSyncPacketS2C(wb.getCurrentWeight()), player);
        });
    }

    public static boolean digest(ServerPlayer player, double percent) {
        return digest(player, percent, 1.0);
    }

    /** @param absorptionMult Absorption accessories: &gt;1 converts more weight per calorie (random rounding keeps small gains). */
    public static boolean digest(ServerPlayer player, double percent, double absorptionMult) {
        final boolean[] done = {false};
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm ->
                player.getCapability(PlayerWeightBarProvider.PLAYER_WEIGHT_BAR).ifPresent(wb -> {
                    int cur = cm.getCurrentCalories();
                    if (cur < 1) return;
                    int amount = Math.min(cur, Math.max(1, (int) Math.round(cm.getMaxCalories() * percent)));
                    int rate = Math.max(1, GluttonousWorldConfig.calToWeightRate.get());
                    int weight;
                    if (absorptionMult <= 1.0) {
                        weight = amount / rate;
                    } else {
                        double exact = amount * absorptionMult / rate;
                        weight = (int) exact;
                        if (player.getRandom().nextDouble() < exact - weight) weight++;
                    }
                    cm.setCurrentCalories(cur - amount);
                    if (weight > 0) {
                        wb.addWeightChanges(weight);
                        net.willsbr.gluttonousgrowth.Event.ModEvent.syncQueuedWeight(player, wb);
                    }
                    syncCalories(player, cm);
                    done[0] = true;
                }));
        return done[0];
    }

    /** Magnitude (positive) of the movement speed penalty Gluttonous Growth currently applies for weight. */
    public static double weightSpeedPenalty(ServerPlayer player) {
        final double[] r = {0.0};
        player.getCapability(PlayerWeightBarProvider.PLAYER_WEIGHT_BAR).ifPresent(wb ->
                r[0] = Math.max(0.0, -wb.getWeightSpeed()));
        return r[0];
    }

    /** Accessory capacity bonus (Mixin field on the calorie meter); clamps current calories to the new total. */
    public static void setCapacityBonus(ServerPlayer player, int bonus) {
        player.getCapability(PlayerCalorieMeterProvider.PLAYER_CALORIE_METER).ifPresent(cm -> {
            if (!(cm instanceof com.eisiadev.oversaturatedgrowth.accessory.IOGCapacityBonus duck)) return;
            int bonusNow = Math.max(0, bonus);
            if (duck.og$getCapacityBonus() == bonusNow) return;
            duck.og$setCapacityBonus(bonusNow);
            if (cm.getCurrentCalories() > cm.getMaxCalories()) cm.setCurrentCalories(cm.getMaxCalories());
            syncCalories(player, cm);
        });
    }

    public static void suppressWeightEffects(ServerPlayer player) {
        player.getCapability(PlayerWeightBarProvider.PLAYER_WEIGHT_BAR).ifPresent(wb -> {
            net.willsbr.gluttonousgrowth.WeightSystem.PlayerWeightBar.clearModifiers(player, wb);
            net.willsbr.gluttonousgrowth.WeightSystem.PlayerWeightBar.clearScaling(player, wb);
        });
    }

    public static void restoreWeightEffects(ServerPlayer player) {
        player.getCapability(PlayerWeightBarProvider.PLAYER_WEIGHT_BAR).ifPresent(wb -> {
            wb.setLastAppliedWeight(-1);
            wb.setEffectsReady(true);
        });
    }
}
