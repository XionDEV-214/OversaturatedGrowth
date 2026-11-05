package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.effect.InflationHelper;
import com.eisiadev.oversaturatedgrowth.init.ModEffects;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InflationEvents {
    private InflationEvents() {}

    private static final int DURATION = 1800;
    private static final int CRYSTAL_DURATION = 6000;
    /** Ticks to grow by one inflation level (the HUD bar fills once per level). */
    private static final float RISE_TICKS = 400f;

    @SubscribeEvent
    public static void onFinishEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getItem().is(ModItems.FATTABERRY.get())) {
            MobEffectInstance cur = event.getEntity().getEffect(ModEffects.FATTABERRY_ATE.get());
            int amp = cur == null ? 0 : Math.min(InflationHelper.MAX_LEVEL - 1, cur.getAmplifier() + 1);
            event.getEntity().addEffect(new MobEffectInstance(ModEffects.FATTABERRY_ATE.get(), DURATION, amp));
        } else if (event.getItem().is(ModItems.CRYSTALLIZED_FATTABERRY.get())) {
            event.getEntity().addEffect(new MobEffectInstance(ModEffects.FATTABERRY_STOMACH.get(), CRYSTAL_DURATION, 0));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player p = event.player;
        CompoundTag d = p.getPersistentData();
        if (d.contains(InflationHelper.KEY_RAMP)) {
            d.remove(InflationHelper.KEY_RAMP);
            d.remove(InflationHelper.KEY_LVL);
        }
        // The inflation amount eases toward the target level: +1 level every RISE_TICKS while growing, and
        // deflates quickly (but still smoothly) when the effects run out.
        int target = InflationHelper.effectiveLevel(p);
        float previous = d.getFloat(InflationHelper.KEY_AMOUNT);
        float amount = previous;
        if (amount < target) {
            amount = Math.min(target, amount + 1f / RISE_TICKS);
        } else if (amount > target) {
            amount = Math.max(target, amount - Math.max(0.05f, amount * 0.05f));
        }
        d.putFloat(InflationHelper.KEY_AMOUNT, amount);

        int q = Math.round(InflationHelper.scale(p) * 50f);
        int oldQ = d.getInt(InflationHelper.KEY_Q);
        if (q != oldQ) {
            float oldW = p.getBbWidth();
            float oldH = p.getBbHeight();
            d.putInt(InflationHelper.KEY_Q, q);
            p.refreshDimensions();
            if (!growInto(p, oldW, oldH)) {
                // No room to grow here (tight tunnel, low ceiling): stay at the old size until there is.
                amount = previous;
                d.putFloat(InflationHelper.KEY_AMOUNT, previous);
                d.putInt(InflationHelper.KEY_Q, oldQ);
                p.refreshDimensions();
            }
        }

        if (amount > 0.05f && !p.onGround() && !p.getAbilities().flying && !p.isFallFlying() && !p.isInWater() && p.getDeltaMovement().y < 0) {
            double f = Math.max(0.55, 1.0 - 0.06 * amount);
            Vec3 m = p.getDeltaMovement();
            p.setDeltaMovement(m.x, m.y * f, m.z);
        }
    }

    /**
     * Vanilla only nudges non-player entities out of blocks when they grow, so a growing player ends up overlapping
     * the wall it was touching and gets stuck. Do the same nudge for players; returns false if there is no free spot.
     */
    private static boolean growInto(Player p, float oldW, float oldH) {
        float newW = p.getBbWidth();
        float newH = p.getBbHeight();
        if (newW <= oldW && newH <= oldH) return true;
        if (p.noPhysics || p.level().noCollision(p)) return true;
        Vec3 center = p.position().add(0.0, oldH / 2.0, 0.0);
        double dw = Math.max(0.0F, newW - oldW) + 1.0E-6;
        double dh = Math.max(0.0F, newH - oldH) + 1.0E-6;
        VoxelShape grown = Shapes.create(AABB.ofSize(center, dw, dh, dw));
        Optional<Vec3> free = p.level().findFreePosition(p, grown, center, newW, newH, newW);
        if (free.isEmpty()) return false;
        Vec3 pos = free.get().add(0.0, -newH / 2.0, 0.0);
        p.setPos(pos.x, pos.y, pos.z);
        return p.level().noCollision(p);
    }

    @SubscribeEvent
    public static void onSize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player p)) return;
        float s = InflationHelper.scale(p);
        if (s <= 1.001f) return;
        event.setNewSize(event.getNewSize().scale(s));
        event.setNewEyeHeight(event.getNewEyeHeight() * s);
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        float k = (float) Math.max(0.3, 1.0 - 0.12 * InflationHelper.amount(p));
        event.setDamageMultiplier(event.getDamageMultiplier() * k);
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        float amount = InflationHelper.amount(p);
        if (amount < 0.66f) return;
        Vec3 m = p.getDeltaMovement();
        p.setDeltaMovement(m.x, m.y * (1.0 - 0.04 * amount), m.z);
    }
}
