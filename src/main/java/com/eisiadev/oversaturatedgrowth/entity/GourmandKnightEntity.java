package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.event.PlayerConsumeHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.UUID;

public class GourmandKnightEntity extends Monster {
    private static final EntityDataAccessor<Boolean> GRABBING =
            SynchedEntityData.defineId(GourmandKnightEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID prey;
    private int cooldown = 60;
    private int stun = 0;

    public GourmandKnightEntity(EntityType<? extends GourmandKnightEntity> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override protected void defineSynchedData() { super.defineSynchedData(); this.entityData.define(GRABBING, false); }
    public boolean isGrabbing() { return this.entityData.get(GRABBING); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GrabGoal());
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (cooldown > 0) cooldown--;
        if (stun > 0) { stun--; getNavigation().stop(); }
        if (prey != null) {
            getNavigation().stop();
            if (!PlayerConsumeHandler.isConsumed(prey)) {
                prey = null;
                cooldown = 200;
            } else if (tickCount % 15 == 0) {
                playSound(SoundEvents.GENERIC_EAT, 0.9f, 0.5f);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)) amount *= 0.5f;
        if (prey != null) amount *= 1.5f;
        if (isGrabbing() && source.getEntity() instanceof Player) {
            this.entityData.set(GRABBING, false);
            stun = 30;
            cooldown = 80;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) PlayerConsumeHandler.releaseForPredator(getUUID());
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && prey != null) PlayerConsumeHandler.releaseForPredator(getUUID());
        super.remove(reason);
    }

    @Override public boolean removeWhenFarAway(double d) { return prey == null; }

    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.IRON_GOLEM_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.IRON_GOLEM_DEATH; }
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState st) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 0.8f);
    }

    private class GrabGoal extends Goal {
        private int wind = 0;
        GrabGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        private boolean valid(ServerPlayer p) { return !p.isCreative() && !p.isSpectator() && p.isAlive(); }

        @Override public boolean canUse() {
            return prey == null && cooldown == 0 && stun == 0 && getTarget() instanceof ServerPlayer p && valid(p)
                    && distanceToSqr(p) < 2.8 * 2.8;
        }

        @Override public boolean canContinueToUse() {
            return isGrabbing() && stun == 0 && getTarget() instanceof ServerPlayer p && valid(p) && distanceToSqr(p) < 3.4 * 3.4;
        }

        @Override public void start() { wind = 0; entityData.set(GRABBING, true); getNavigation().stop(); }

        @Override public void tick() {
            if (!(getTarget() instanceof ServerPlayer p)) return;
            getLookControl().setLookAt(p, 30f, 30f);
            if (++wind >= 24 && level() instanceof ServerLevel sl) {
                PlayerConsumeHandler.startMobConsume(GourmandKnightEntity.this, p, sl, 0.8);
                prey = p.getUUID();
                setTarget(null);
                entityData.set(GRABBING, false);
            }
        }

        @Override public void stop() { entityData.set(GRABBING, false); wind = 0; }
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); }
}
