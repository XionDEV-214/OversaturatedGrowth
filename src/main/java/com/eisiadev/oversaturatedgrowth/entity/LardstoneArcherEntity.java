package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.entity.projectile.LardShardEntity;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.BlockConsumeVisualS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LardstoneArcherEntity extends AbstractSkeleton {

    private static final EntityDataAccessor<Integer> HELD =
            SynchedEntityData.defineId(LardstoneArcherEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ARMS_UP =
            SynchedEntityData.defineId(LardstoneArcherEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int PICK_TICKS = 20;
    private static final int HOLD_TICKS = 18;
    private static final int PICK_RADIUS = 5;

    public LardstoneArcherEntity(EntityType<? extends LardstoneArcherEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELD, 0);
        this.entityData.define(ARMS_UP, false);
    }

    public BlockState getHeldState() {
        int id = this.entityData.get(HELD);
        return id == 0 ? null : Block.stateById(id);
    }

    public boolean isArmsUp() { return this.entityData.get(ARMS_UP); }

    @Override
    public void reassessWeaponGoal() { }

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance diff) {
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new ThrowBlockGoal());
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) { }

    @Override
    protected boolean isSunBurnTick() { return false; }

    @Override
    protected SoundEvent getStepSound() { return SoundEvents.SKELETON_STEP; }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.SKELETON_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s) { return SoundEvents.SKELETON_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.SKELETON_DEATH; }

    private boolean isThrowable(BlockState s) {
        return s.is(ModBlocks.LARDSTONE.get()) || s.is(ModBlocks.WEATHERED_LARDSTONE.get())
                || s.is(ModBlocks.GREASY_LARDSTONE.get()) || s.is(ModBlocks.WEATHERED_GREASY_LARDSTONE.get());
    }

    private BlockPos findSource() {
        BlockPos o = this.blockPosition();
        for (int i = 0; i < 48; i++) {
            BlockPos p = o.offset(random.nextInt(PICK_RADIUS * 2 + 1) - PICK_RADIUS,
                    random.nextInt(5) - 2, random.nextInt(PICK_RADIUS * 2 + 1) - PICK_RADIUS);
            if (isThrowable(this.level().getBlockState(p))) return p;
        }
        return null;
    }

    private class ThrowBlockGoal extends Goal {
        private int phase = 0;
        private int timer = 0;
        private int cooldown = 40;
        private BlockState state;

        ThrowBlockGoal() { setFlags(EnumSet.of(Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (cooldown > 0) { cooldown--; return false; }
            LivingEntity t = getTarget();
            if (t == null || !t.isAlive() || distanceToSqr(t) > 18 * 18 || !hasLineOfSight(t)) return false;
            return findSource() != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity t = getTarget();
            return phase != 0 && t != null && t.isAlive();
        }

        @Override
        public void start() {
            BlockPos src = findSource();
            if (src == null) { phase = 0; return; }
            state = level().getBlockState(src);
            phase = 1; timer = 0;
            entityData.set(ARMS_UP, true);
            getNavigation().stop();
            if (level() instanceof ServerLevel sl) {

                ModMessages.sendToNearby(new BlockConsumeVisualS2CPacket(src, Block.getId(state), getId(), 8),
                        sl, Vec3.atCenterOf(src), 48);
            }
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 40f, 40f);
            getNavigation().stop();
            timer++;
            if (phase == 1 && timer >= PICK_TICKS) {
                entityData.set(HELD, Block.getId(state));
                playSound(SoundEvents.STONE_PLACE, 1.0f, 0.7f);
                phase = 2; timer = 0;
            } else if (phase == 2 && timer >= HOLD_TICKS) {
                throwAt(t);
                phase = 0;
            }
        }

        private void throwAt(LivingEntity t) {
            LardShardEntity rock = new LardShardEntity(ModEntities.LARDSTONE_SHARD.get(), level(), LardstoneArcherEntity.this);
            rock.setItem(new ItemStack(state.getBlock().asItem()));
            Vec3 from = getEyePosition().add(0, -0.2, 0);
            rock.setPos(from.x, from.y, from.z);
            Vec3 to = t.getEyePosition();
            double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            rock.shoot(dx, dy + dist * 0.12, dz, 1.3f, 2.0f);
            level().addFreshEntity(rock);
            playSound(SoundEvents.SNOWBALL_THROW, 1.0f, 0.5f);
            entityData.set(HELD, 0);
            entityData.set(ARMS_UP, false);
            cooldown = 50 + random.nextInt(40);
        }

        @Override
        public void stop() {
            phase = 0;
            entityData.set(HELD, 0);
            entityData.set(ARMS_UP, false);
        }
    }
}
