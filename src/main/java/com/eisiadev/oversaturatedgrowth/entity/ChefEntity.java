package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.entity.projectile.LardPotEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ChefEntity extends Monster {
    private int combo = 0;
    private int comboDelay = 0;
    private int potCooldown = 40;

    public ChefEntity(EntityType<? extends ChefEntity> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public boolean isFrenzied() { return getHealth() <= getMaxHealth() * 0.3f; }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance diff) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PotGoal());
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && combo == 0 && comboDelay == 0) { combo = isFrenzied() ? 2 : 1; comboDelay = 6; }
        return hit;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (potCooldown > 0) potCooldown--;
        if (combo > 0 && --comboDelay <= 0) {
            LivingEntity t = getTarget();
            if (t != null && distanceToSqr(t) < 3.2 * 3.2) {
                swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                super.doHurtTarget(t);
                comboDelay = 6;
                combo--;
            } else { combo = 0; comboDelay = 0; }
        }
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.VILLAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }

    private class PotGoal extends Goal {
        PotGoal() { setFlags(EnumSet.of(Flag.LOOK)); }

        @Override public boolean canUse() {
            LivingEntity t = getTarget();
            if (t == null || !t.isAlive() || potCooldown > 0) return false;
            double d = distanceToSqr(t);
            return d >= 6 * 6 && d <= 14 * 14 && hasLineOfSight(t);
        }

        @Override public boolean canContinueToUse() { return false; }

        @Override public void start() {
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 40f, 40f);
            swing(net.minecraft.world.InteractionHand.OFF_HAND);
            LardPotEntity pot = new LardPotEntity(level(), ChefEntity.this);
            Vec3 from = getEyePosition();
            Vec3 to = t.position();
            double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            pot.shoot(dx, dy + dist * 0.22, dz, 0.95f, 1.0f);
            level().addFreshEntity(pot);
            playSound(SoundEvents.SNOWBALL_THROW, 1.0f, 0.5f);
            potCooldown = isFrenzied() ? 30 : 60;
        }
    }
}
