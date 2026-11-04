package com.eisiadev.oversaturatedgrowth.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

public class LardBloatEntity extends PathfinderMob {

    public static final float MAX_SCALE = 4.0f;
    private boolean survivedHit = false;
    private static final EntityDataAccessor<Integer> SWELL =
            SynchedEntityData.defineId(LardBloatEntity.class, EntityDataSerializers.INT);

    public LardBloatEntity(EntityType<? extends LardBloatEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.FLYING_SPEED, 0.25)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SWELL, 0);
    }

    public int getSwell() { return this.entityData.get(SWELL); }

    public float getBloatScale() { return 1.0f + (MAX_SCALE - 1.0f) * getSwell() / 100f; }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        return super.getDimensions(pose).scale(getBloatScale());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (SWELL.equals(key)) this.refreshDimensions();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) { return false; }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState s,
                                   net.minecraft.core.BlockPos p) { }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive()) {
            survivedHit = true;
            float lost = 1.0f - this.getHealth() / this.getMaxHealth();
            this.entityData.set(SWELL, Mth.clamp(Math.round(lost * 100f), 0, 100));
            this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, 0.8f, 0.8f + lost * 0.4f);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {

        if (!this.level().isClientSide && survivedHit) {
            com.eisiadev.oversaturatedgrowth.event.GasCloudManager.spawn((net.minecraft.server.level.ServerLevel) this.level(),
                    this.position().add(0, this.getBbHeight() * 0.5, 0), 0.9f * getBloatScale(), 200);
            this.playSound(SoundEvents.SLIME_SQUISH, 1.0f, 0.5f);
        }
        super.die(source);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.SLIME_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.SLIME_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Swell", getSwell());
        tag.putBoolean("Survived", survivedHit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SWELL, tag.getInt("Swell"));
        survivedHit = tag.getBoolean("Survived");
    }
}
