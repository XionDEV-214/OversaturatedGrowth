package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.init.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

public class LardfinEntity extends PathfinderMob {

    private BlockPos swimTarget = null;
    private int retarget = 0;

    public LardfinEntity(EntityType<? extends LardfinEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    private boolean inLard() { return this.isInFluidType(ModFluids.REFINED_LARD_TYPE.get()); }

    private boolean isLardAt(BlockPos p) {
        return this.level().getFluidState(p).getFluidType() == ModFluids.REFINED_LARD_TYPE.get();
    }

    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public boolean canDrownInFluidType(FluidType type) { return false; }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void aiStep() {
        super.aiStep();
        boolean lard = inLard();
        this.setNoGravity(lard);
        if (this.level().isClientSide) return;

        if (lard) {
            if (--retarget <= 0 || swimTarget == null || !isLardAt(swimTarget)
                    || this.distanceToSqr(Vec3.atCenterOf(swimTarget)) < 1.0) {
                retarget = 40 + this.random.nextInt(60);
                swimTarget = null;
                for (int i = 0; i < 12; i++) {
                    BlockPos p = this.blockPosition().offset(this.random.nextInt(9) - 4, this.random.nextInt(7) - 3, this.random.nextInt(9) - 4);
                    if (isLardAt(p) && isLardAt(p.above())) { swimTarget = p; break; }
                }
            }
            Vec3 motion = this.getDeltaMovement().scale(0.85);
            if (swimTarget != null) {
                Vec3 d = Vec3.atCenterOf(swimTarget).subtract(this.position());
                motion = motion.add(d.normalize().scale(0.012));
            } else {
                motion = motion.add(0, -0.005, 0);
            }
            this.setDeltaMovement(motion);
        } else if (this.onGround() && this.random.nextInt(8) == 0) {

            this.setDeltaMovement((this.random.nextFloat() - 0.5f) * 0.2, 0.3, (this.random.nextFloat() - 0.5f) * 0.2);
            this.setYRot(this.random.nextFloat() * 360f);
        }
        Vec3 v = this.getDeltaMovement();
        if (v.horizontalDistanceSqr() > 1.0E-4) {
            float yaw = (float) (Mth.atan2(v.z, v.x) * 180.0 / Math.PI) - 90f;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.setXRot((float) -(Mth.atan2(v.y, v.horizontalDistance()) * 180.0 / Math.PI));
        }
    }

    @Override
    public void travel(Vec3 input) {
        if (inLard()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            super.travel(input);
        }
    }

    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.COD_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.COD_DEATH; }
}
