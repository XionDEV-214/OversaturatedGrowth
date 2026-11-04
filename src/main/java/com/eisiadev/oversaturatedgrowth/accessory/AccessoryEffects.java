package com.eisiadev.oversaturatedgrowth.accessory;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.init.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/** Turns the bag totals into attribute modifiers / Gluttonous Growth hooks. Server side only. */
public final class AccessoryEffects {

    public static final UUID DIGESTION_ID = UUID.fromString("5e3f2c1a-7b44-4e0a-9a51-0c2d1b0f7a01");
    public static final UUID HEALTH_ID = UUID.fromString("5e3f2c1a-7b44-4e0a-9a51-0c2d1b0f7a02");
    public static final UUID SPEED_ID = UUID.fromString("5e3f2c1a-7b44-4e0a-9a51-0c2d1b0f7a03");

    private AccessoryEffects() {}

    public static void apply(ServerPlayer sp, AccessoryBag bag) {
        AccessoryTotals t = bag.totals();

        // Digestion: plain attribute, DigestiveSoundHandler already reads it.
        // Digestion no longer uses the attribute (see AccessoryTotals#digestShare); drop modifiers from older builds.
        set(sp.getAttribute(ModAttributes.DIGESTION.get()), DIGESTION_ID, "", 0, AttributeModifier.Operation.ADDITION);

        // Health: GG's own weight health is "percentage * max_hearts", so ours scales with the same weight ratio.
        double hp = t.healthHp() * GgBridge.weightRatio(sp);
        set(sp.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID, "og_accessory_health", hp,
                AttributeModifier.Operation.ADDITION);
        if (sp.getHealth() > sp.getMaxHealth()) sp.setHealth(sp.getMaxHealth());

        // Movement: cancel a share of GG's (negative, MULTIPLY_BASE) weight speed modifier.
        double cancelled = GgBridge.weightSpeedPenalty(sp) * t.movementReduction();
        set(sp.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID, "og_accessory_movement", cancelled,
                AttributeModifier.Operation.MULTIPLY_BASE);

        // Capacity: handed to the Mixin on GG's calorie meter.
        GgBridge.setCapacityBonus(sp, t.bonusCalories());
    }

    /** Remove everything this class added (used when a player's bag is cleared by commands / on logout cleanup). */
    public static void clear(ServerPlayer sp) {
        set(sp.getAttribute(ModAttributes.DIGESTION.get()), DIGESTION_ID, "", 0, AttributeModifier.Operation.ADDITION);
        set(sp.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID, "", 0, AttributeModifier.Operation.ADDITION);
        set(sp.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID, "", 0, AttributeModifier.Operation.MULTIPLY_BASE);
        GgBridge.setCapacityBonus(sp, 0);
    }

    private static void set(@Nullable AttributeInstance inst, UUID id, String name, double amount,
                            AttributeModifier.Operation op) {
        if (inst == null) return;
        AttributeModifier cur = inst.getModifier(id);
        if (amount == 0.0) {
            if (cur != null) inst.removeModifier(id);
            return;
        }
        if (cur != null && Math.abs(cur.getAmount() - amount) < 1.0E-6) return;
        if (cur != null) inst.removeModifier(id);
        inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
    }

    // ------------------------------------------------------------------ queries used by the digestion code / mixins

    public static AccessoryTotals totals(Player player) {
        return player.getCapability(AccessoryBagProvider.CAP).map(AccessoryBag::totals).orElse(AccessoryTotals.EMPTY);
    }

    public static double absorptionMultiplier(Player player) {
        return totals(player).absorptionMultiplier();
    }

    /** Share of the max calories that one OG digestion step converts (base 0.05). */
    public static double digestShare(Player player, double base) {
        return totals(player).digestShare(base);
    }
}
