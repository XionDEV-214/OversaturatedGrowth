package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FattadiumBeetleEntity extends PathfinderMob {

    public BlockPos eatingPos = null;

    public FattadiumBeetleEntity(EntityType<? extends FattadiumBeetleEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(3, new EatOreGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    private class EatOreGoal extends Goal {
        private BlockPos ore;
        private int eatTimer;
        private int giveUp;

        EatOreGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (getTarget() != null || getRandom().nextInt(60) != 0) return false;
            BlockPos origin = blockPosition();
            BlockPos found = null;
            double best = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(origin.offset(-8, -4, -8), origin.offset(8, 4, 8))) {
                if (level().getBlockState(p).is(ModBlocks.FATTADIUM_ORE.get())) {
                    double d = p.distSqr(origin);
                    if (d < best) { best = d; found = p.immutable(); }
                }
            }
            ore = found;
            return ore != null;
        }

        @Override
        public boolean canContinueToUse() {
            return getTarget() == null && ore != null
                    && level().getBlockState(ore).is(ModBlocks.FATTADIUM_ORE.get())
                    && eatTimer < 100 && giveUp < 300;
        }

        @Override
        public void start() {
            eatTimer = -(getRandom().nextInt(40));
            giveUp = 0;
            getNavigation().moveTo(getNavigation().createPath(ore, 1), 1.0);
        }

        @Override
        public void tick() {
            giveUp++;
            Vec3 c = Vec3.atCenterOf(ore);
            getLookControl().setLookAt(c.x, c.y, c.z);
            if (distanceToSqr(c) > 6.25) {
                if (getNavigation().isDone()) getNavigation().moveTo(getNavigation().createPath(ore, 1), 1.0);
                eatingPos = null;
                return;
            }
            getNavigation().stop();
            eatingPos = ore;
            eatTimer++;
            if (eatTimer > 0 && eatTimer % 8 == 0 && level() instanceof ServerLevel sl) {
                playSound(SoundEvents.GENERIC_EAT, 0.8f, 2.0f);
                BlockState st = level().getBlockState(ore);
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st),
                        c.x, c.y, c.z, 8, 0.3, 0.3, 0.3, 0.05);
            }
        }

        @Override
        public void stop() {
            eatingPos = null;
            ore = null;
            getNavigation().stop();
        }
    }
}
