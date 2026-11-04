package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.entity.projectile.LardDripEntity;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LardDripperEntity extends PathfinderMob {

    private int dripCooldown;

    public LardDripperEntity(EntityType<? extends LardDripperEntity> type, Level level) {
        super(type, level);
        this.dripCooldown = 100 + this.random.nextInt(300);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() { }

    private boolean hasCeiling() {
        BlockPos base = BlockPos.containing(this.getX(), this.getY() + this.getBbHeight() + 0.05, this.getZ());
        for (int i = 0; i < 2; i++) {
            BlockPos p = base.above(i);
            if (this.level().getBlockState(p).isFaceSturdy(this.level(), p, Direction.DOWN)) {
                double y = p.getY() - this.getBbHeight();
                if (Math.abs(this.getY() - y) > 0.01) this.setPos(this.getX(), y, this.getZ());
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        boolean support = hasCeiling();
        this.setNoGravity(support);
        if (support) this.setDeltaMovement(Vec3.ZERO);
        super.tick();
        if (!this.level().isClientSide && support && --dripCooldown <= 0) {
            dripCooldown = 200 + this.random.nextInt(500);
            LardDripEntity drip = new LardDripEntity(ModEntities.LARD_DRIP.get(), this.level(),
                    this.getX(), this.getY() + 0.1, this.getZ());
            drip.setOwner(this);
            drip.setDeltaMovement(0, -0.05, 0);
            this.level().addFreshEntity(drip);
            this.playSound(SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA, 0.8f, 0.6f);
        }
    }

    @Override public boolean isPushable() { return false; }
    @Override public void push(Entity e) { }
    @Override public void knockback(double s, double x, double z) { }
    @Override public boolean causeFallDamage(float d, float m, DamageSource s) { return false; }
    @Override public boolean removeWhenFarAway(double d) { return true; }

    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.SLIME_HURT_SMALL; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.SLIME_DEATH_SMALL; }

    @Override public void addAdditionalSaveData(CompoundTag t) { super.addAdditionalSaveData(t); }
}
