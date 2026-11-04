package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.event.PlayerConsumeHandler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GluttonToadEntity extends Frog {

    public static final int MAX_MOBS = 10;
    public static final float MAX_SIZE = 3.0f;
    private static final float MOB_GROWTH = 0.12f;
    private static final float ITEM_GROWTH = 0.01f;

    private static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(GluttonToadEntity.class, EntityDataSerializers.FLOAT);

    private final List<CompoundTag> swallowedMobs = new ArrayList<>();
    private final List<ItemStack> swallowedItems = new ArrayList<>();
    private UUID playerPrey;
    private int playerCooldown = 0;

    public GluttonToadEntity(EntityType<? extends GluttonToadEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Frog.createAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SIZE, 1.0f);
    }

    public float getToadSize() { return this.entityData.get(SIZE); }

    private void setToadSize(float s) {
        this.entityData.set(SIZE, Math.min(MAX_SIZE, s));
        this.refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (SIZE.equals(key)) this.refreshDimensions();
        super.onSyncedDataUpdated(key);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getToadSize());
    }

    private static double volume(Entity e) {
        return (double) e.getBbWidth() * e.getBbWidth() * e.getBbHeight();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TongueGoal());
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void customServerAiStep() { }

    @Override
    public boolean isFood(net.minecraft.world.item.ItemStack stack) { return false; }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (playerCooldown > 0) playerCooldown--;
            if (playerPrey != null && !PlayerConsumeHandler.isConsumed(playerPrey)) {
                playerPrey = null;
                playerCooldown = 200;
            }
        }
    }

    private boolean canSwallow(LivingEntity e) {
        if (e == this || !e.isAlive() || e instanceof GluttonToadEntity) return false;
        if (e.getType().is(net.minecraftforge.common.Tags.EntityTypes.BOSSES)) return false;
        if (volume(e) >= volume(this)) return false;
        if (e instanceof Player p) {
            return p instanceof ServerPlayer && !p.isCreative() && !p.isSpectator()
                    && playerPrey == null && playerCooldown == 0 && !PlayerConsumeHandler.isConsumed(p.getUUID());
        }
        return swallowedMobs.size() < MAX_MOBS && e.getType().canSerialize();
    }

    private class TongueGoal extends Goal {
        private Entity prey;
        private int t, cooldown = 40;

        TongueGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (cooldown > 0) { cooldown--; return false; }
            prey = findPrey();
            return prey != null;
        }

        private Entity findPrey() {
            var box = getBoundingBox().inflate(5.0 + getToadSize(), 2.0, 5.0 + getToadSize());
            Entity best = null; double bd = Double.MAX_VALUE;
            for (ItemEntity it : level().getEntitiesOfClass(ItemEntity.class, box, i -> i.isAlive() && !i.hasPickUpDelay())) {
                double d = distanceToSqr(it);
                if (d < bd) { bd = d; best = it; }
            }
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, box, GluttonToadEntity.this::canSwallow)) {
                if (!hasLineOfSight(e)) continue;
                double d = distanceToSqr(e);
                if (d < bd) { bd = d; best = e; }
            }
            return best;
        }

        @Override
        public boolean canContinueToUse() {
            return prey != null && prey.isAlive() && !prey.isRemoved() && t < 24 && distanceToSqr(prey) < 14 * 14;
        }

        @Override
        public void start() {
            t = 0;
            getNavigation().stop();
            setPose(Pose.USING_TONGUE);
            setTongueTarget(prey);
            playSound(SoundEvents.FROG_TONGUE, 1.0f, 0.8f);
        }

        @Override
        public void tick() {
            getLookControl().setLookAt(prey, 60f, 60f);
            t++;
            Vec3 mouth = position().add(0, getBbHeight() * 0.45, 0).add(getLookAngle().scale(getBbWidth() * 0.45));
            Vec3 to = prey.position().add(0, prey.getBbHeight() * 0.5, 0);
            if (level() instanceof ServerLevel sl) {
                Vec3 line = to.subtract(mouth);
                int n = Math.max(2, (int) (line.length() * 2));
                for (int i = 0; i <= n; i += 2) {
                    Vec3 p = mouth.add(line.scale((double) i / n));
                    sl.sendParticles(new DustParticleOptions(new Vector3f(0.85f, 0.25f, 0.35f), 0.8f), p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
            }
            if (t > 6) {
                Vec3 pull = mouth.subtract(prey.position()).normalize().scale(0.45);
                prey.setDeltaMovement(pull.x, pull.y + 0.05, pull.z);
                prey.hurtMarked = true;
            }
            if (prey.distanceTo(GluttonToadEntity.this) <= getBbWidth() * 0.9 + 0.6) {
                swallow(prey);
                prey = null;
            }
        }

        @Override
        public void stop() {
            setPose(Pose.STANDING);
            eraseTongueTarget();
            cooldown = 50;
            prey = null;
        }
    }

    private void swallow(Entity e) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (e instanceof ItemEntity it) {
            swallowedItems.add(it.getItem().copy());
            setToadSize(getToadSize() + ITEM_GROWTH * it.getItem().getCount());
            it.discard();
        } else if (e instanceof ServerPlayer p) {
            PlayerConsumeHandler.startMobConsume(this, p, sl);
            playerPrey = p.getUUID();
            this.setTarget(null);
            this.playSound(SoundEvents.FROG_EAT, 1.0f, 0.6f);
            return;
        } else if (e instanceof LivingEntity le) {
            CompoundTag tag = new CompoundTag();
            if (!le.save(tag)) return;
            tag.remove("UUID");
            swallowedMobs.add(tag);
            setToadSize(getToadSize() + MOB_GROWTH);
            le.discard();
        } else return;
        this.playSound(SoundEvents.FROG_EAT, 1.0f, 0.7f);
        this.heal(2.0f);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            PlayerConsumeHandler.releaseForPredator(this.getUUID());
            for (CompoundTag tag : swallowedMobs) {
                Optional<Entity> opt = EntityType.create(tag, sl);
                opt.ifPresent(m -> {
                    m.moveTo(getX() + (random.nextDouble() - 0.5) * getBbWidth(), getY(), getZ() + (random.nextDouble() - 0.5) * getBbWidth());
                    sl.addFreshEntity(m);
                });
            }
            for (ItemStack s : swallowedItems) this.spawnAtLocation(s);
            swallowedMobs.clear();
            swallowedItems.clear();
        }
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && playerPrey != null) PlayerConsumeHandler.releaseForPredator(this.getUUID());
        super.remove(reason);
    }

    @Override public boolean removeWhenFarAway(double d) { return swallowedMobs.isEmpty() && swallowedItems.isEmpty() && playerPrey == null; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("ToadSize", getToadSize());
        ListTag mobs = new ListTag();
        mobs.addAll(swallowedMobs);
        tag.put("Mobs", mobs);
        ListTag items = new ListTag();
        for (ItemStack s : swallowedItems) items.add(s.save(new CompoundTag()));
        tag.put("Items", items);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SIZE, Math.max(1.0f, Math.min(MAX_SIZE, tag.getFloat("ToadSize"))));
        swallowedMobs.clear();
        for (Tag t : tag.getList("Mobs", Tag.TAG_COMPOUND)) swallowedMobs.add((CompoundTag) t);
        swallowedItems.clear();
        for (Tag t : tag.getList("Items", Tag.TAG_COMPOUND)) swallowedItems.add(ItemStack.of((CompoundTag) t));
    }

    public static boolean isInCrater(net.minecraft.world.level.LevelAccessor lvl, net.minecraft.core.BlockPos pos) {
        if (!lvl.getBlockState(pos.below()).is(com.eisiadev.oversaturatedgrowth.init.ModBlocks.LARD_BLOCK.get())) return false;
        int[][] ring = {{3,0},{-3,0},{0,3},{0,-3},{3,3},{3,-3},{-3,3},{-3,-3}};
        int walls = 0;
        for (int[] o : ring) if (!lvl.getBlockState(pos.offset(o[0], 0, o[1])).isAir()) walls++;
        return walls >= 6;
    }
}
