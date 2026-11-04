package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.event.PlayerConsumeHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
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

public class MawChestEntity extends Monster {

    private static final EntityDataAccessor<Boolean> HOLDING =
            SynchedEntityData.defineId(MawChestEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID prey;
    private int cooldown = 0;
    private int chewTimer = 0;

    public MawChestEntity(EntityType<? extends MawChestEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 7.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HOLDING, false);
    }

    public boolean isHolding() { return this.entityData.get(HOLDING); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SwallowGoal());
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        if (cooldown > 0) cooldown--;
        if (prey != null) {
            this.getNavigation().stop();
            if (!PlayerConsumeHandler.isConsumed(prey)) {

                prey = null;
                this.entityData.set(HOLDING, false);
                cooldown = 200;
            } else if (++chewTimer % 15 == 0 && this.level() instanceof ServerLevel sl) {
                this.playSound(SoundEvents.GENERIC_EAT, 0.9f, 0.6f);
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 0.9, getZ(), 2, 0.3, 0.2, 0.3, 0.0);
            }
        }
    }

    private void swallow(ServerPlayer p) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        PlayerConsumeHandler.startMobConsume(this, p, sl);
        prey = p.getUUID();
        chewTimer = 0;
        this.entityData.set(HOLDING, true);
        this.setTarget(null);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) PlayerConsumeHandler.releaseForPredator(this.getUUID());
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && prey != null) PlayerConsumeHandler.releaseForPredator(this.getUUID());
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double d) { return prey == null; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); }

    private int hopCooldown = 0;

    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s) { return SoundEvents.WOOD_HIT; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return SoundEvents.WOOD_BREAK; }
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState st) { }

    private class SwallowGoal extends Goal {
        SwallowGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            return prey == null && cooldown == 0 && getTarget() instanceof ServerPlayer p
                    && !p.isCreative() && !p.isSpectator() && p.isAlive();
        }

        @Override
        public boolean canContinueToUse() { return canUse(); }

        @Override
        public void tick() {
            if (!(getTarget() instanceof ServerPlayer p)) return;
            getLookControl().setLookAt(p, 30f, 30f);
            if (distanceToSqr(p) < 2.2 * 2.2) {
                swallow(p);
            } else if (onGround()) {

                if (hopCooldown > 0) { hopCooldown--; }
                else {
                    Vec3 d = p.position().subtract(position());
                    Vec3 h = new Vec3(d.x, 0, d.z).normalize().scale(0.5);
                    setDeltaMovement(h.x, 0.45, h.z);
                    hasImpulse = true;
                    hopCooldown = 14 + random.nextInt(10);
                    playSound(SoundEvents.WOOD_STEP, 1.0f, 0.6f);
                }
            }
        }
    }
}
