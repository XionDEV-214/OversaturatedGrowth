package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.entity.projectile.LardGlobEntity;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ButlerEntity extends AbstractSkeleton {
    private static final int MIN_RANGE = 4, MAX_RANGE = 16;

    public ButlerEntity(EntityType<? extends ButlerEntity> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.monster.Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 0.5)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override public void reassessWeaponGoal() { }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance diff) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RAW_LARDSOAKED_MEAT.get()));
        ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
        ((DyeableLeatherItem) Items.LEATHER_CHESTPLATE).setColor(chest, 0x1E1E1E);
        ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
        ((DyeableLeatherItem) Items.LEATHER_LEGGINGS).setColor(legs, 0x1E1E1E);
        this.setItemSlot(EquipmentSlot.CHEST, chest);
        this.setItemSlot(EquipmentSlot.LEGS, legs);
        for (EquipmentSlot s : EquipmentSlot.values()) this.setDropChance(s, 0.0f);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, MIN_RANGE + 1, 1.0, 1.35));
        this.goalSelector.addGoal(2, new TrayGoal());
        this.goalSelector.addGoal(3, new ApproachGoal());
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.2, false));
    }

    @Override public void performRangedAttack(LivingEntity target, float power) { }
    @Override protected boolean isSunBurnTick() { return false; }
    @Override protected SoundEvent getStepSound() { return SoundEvents.SKELETON_STEP; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.SKELETON_AMBIENT; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s) { return SoundEvents.SKELETON_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.SKELETON_DEATH; }

    private class ApproachGoal extends Goal {
        ApproachGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            LivingEntity t = getTarget();
            return t != null && t.isAlive() && distanceToSqr(t) > 12 * 12;
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() { LivingEntity t = getTarget(); if (t != null) getNavigation().moveTo(t, 1.1); }
    }

    private class TrayGoal extends Goal {
        private int cooldown = 30;
        TrayGoal() { setFlags(EnumSet.of(Flag.LOOK)); }

        @Override public boolean canUse() {
            if (cooldown > 0) { cooldown--; return false; }
            LivingEntity t = getTarget();
            double d = t == null ? 0 : distanceToSqr(t);
            return t != null && t.isAlive() && d <= MAX_RANGE * MAX_RANGE && hasLineOfSight(t);
        }

        @Override public void start() {
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 40f, 40f);
            swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            Vec3 from = getEyePosition().add(0, -0.2, 0);
            Vec3 to = t.getEyePosition();
            double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            int cal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());
            for (int i = 0; i < ModServerConfig.globSpitCount.get(); i++) {
                LardGlobEntity glob = new LardGlobEntity(level(), ButlerEntity.this, cal, false);
                glob.setFullStack(true);
                glob.setPos(from.x, from.y, from.z);
                glob.shoot(dx, dy + dist * 0.06, dz, 1.1f, 7.0f);
                glob.setMaxLifeTicks(70);
                level().addFreshEntity(glob);
            }
            playSound(SoundEvents.SNOWBALL_THROW, 1.0f, 0.6f);
            cooldown = 50;
        }

        @Override public boolean canContinueToUse() { return false; }
    }
}
