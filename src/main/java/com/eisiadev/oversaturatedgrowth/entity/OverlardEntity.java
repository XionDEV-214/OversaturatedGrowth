package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.entity.projectile.LardGlobEntity;
import com.eisiadev.oversaturatedgrowth.event.PlayerConsumeHandler;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.BlockConsumeVisualS2CPacket;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class OverlardEntity extends Monster {
    public static final int MAX_SIZE = 5;
    private static final float[] SCALE = {1.0f, 1.0f, 1.3f, 1.7f, 2.2f, 2.8f};
    public static final int ST_IDLE = 0, ST_PICKUP = 1, ST_SWALLOW = 2, ST_SLAM = 3, ST_ROAR = 4, ST_THROW = 5;
    private static final UUID RAGE_MOD = UUID.fromString("5b7f2c0e-8c1d-4a36-9a55-0d0e7f2a1c11");

    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(OverlardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(OverlardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(OverlardEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.oversaturatedgrowth.overlard"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    private double gauge = 0;
    private int introTicks = 0, transitionTicks = 0, stun = 0, vulnTicks = 0, actionTicks = 0;
    private int biteCd = 100, throwCd = 60, summonCd = 200, slamCd = 100, swallowCd = 120, rainCd = 200, rainTicks = 0;
    private int swallowWind = 0, slamTimer = 0, slamPhase = 0, airTicks = 0, noPlayerTicks = 0;
    private UUID prey;
    private UUID held;
    private boolean feedMode = false;
    private int feedTicks = 0, feeds = 0, novaTicks = 0, novaRings = 0, gap = 40, grabCd = 240, novaCd = 120, bombCd = 220, foodCd = 60;
    private float holdDamage = 0f;
    private boolean eating = false;
    private LivingEntity slamTarget;

    public OverlardEntity(EntityType<? extends OverlardEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 800.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SIZE, 1);
        this.entityData.define(STATE, ST_IDLE);
        this.entityData.define(AWAKE, false);
    }

    public int size() { return this.entityData.get(SIZE); }
    public int state() { return this.entityData.get(STATE); }
    public boolean isAwake() { return this.entityData.get(AWAKE); }
    public float factor() { return SCALE[Math.max(1, Math.min(MAX_SIZE, size()))]; }
    private void setState(int s) { this.entityData.set(STATE, s); }

    @Override
    public EntityDimensions getDimensions(Pose pose) { return super.getDimensions(pose).scale(factor()); }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (SIZE.equals(key)) refreshDimensions();
        super.onSyncedDataUpdated(key);
    }

    @Override protected float getBlockSpeedFactor() { return 1.0f; }
    @Override protected float getBlockJumpFactor() { return 1.0f; }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public boolean removeWhenFarAway(double d) { return false; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EatGoal());
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private boolean canAct() {
        return isAwake() && introTicks == 0 && transitionTicks == 0 && stun == 0 && prey == null && held == null && novaTicks == 0
                && slamPhase == 0 && actionTicks == 0 && state() == ST_IDLE && isAlive();
    }

    private static boolean isMinion(Mob e) {
        return e instanceof ButlerEntity || e instanceof ChefEntity || e instanceof GourmandKnightEntity;
    }

    private static boolean validPrey(ServerPlayer p) { return !p.isCreative() && !p.isSpectator() && p.isAlive(); }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || !(level() instanceof ServerLevel sl) || !isAlive()) return;
        serverTick(sl);
    }

    private void serverTick(ServerLevel sl) {
        if (!isAwake()) { tryWake(sl); return; }

        if (tickCount % 4 == 0) bossEvent.setProgress(Math.max(0f, Math.min(1f, getHealth() / getMaxHealth())));
        if (tickCount % 40 == 0) syncBossBar(sl);

        if (sl.getNearestPlayer(getX(), getY(), getZ(), 56, true) == null) {
            if (++noPlayerTicks > 300) { resetToDormant(); return; }
        } else noPlayerTicks = 0;

        if (stun > 0) stun--;
        if (vulnTicks > 0) vulnTicks--;
        if (biteCd > 0) biteCd--;
        if (throwCd > 0) throwCd--;
        if (summonCd > 0) summonCd--;
        if (slamCd > 0) slamCd--;
        if (swallowCd > 0) swallowCd--;
        if (rainCd > 0 && rainTicks == 0) rainCd--;
        if (gap > 0) gap--;
        if (grabCd > 0) grabCd--;
        if (novaCd > 0) novaCd--;
        if (bombCd > 0) bombCd--;
        if (foodCd > 0) foodCd--;

        if (introTicks > 0) {
            getNavigation().stop();
            if (--introTicks % 20 == 0) playSound(SoundEvents.RAVAGER_ROAR, 3.0f, 0.5f);
            if (introTicks == 0) setState(ST_IDLE);
            return;
        }
        if (transitionTicks > 0) {
            getNavigation().stop();
            if (--transitionTicks == 0) setState(ST_IDLE);
            return;
        }
        if (actionTicks > 0 && --actionTicks == 0 && state() != ST_SLAM) setState(ST_IDLE);

        if (prey != null) {
            getNavigation().stop();
            if (!PlayerConsumeHandler.isConsumed(prey)) { prey = null; swallowCd = 240; setState(ST_IDLE); }
            else if (tickCount % 15 == 0) playSound(SoundEvents.GENERIC_EAT, 1.0f, 0.4f);
        }

        if (size() < MAX_SIZE && getHealth() <= getMaxHealth() * (1.0f - 0.2f * size())) {
            grow(size() + 1);
            return;
        }

        tickRain(sl);
        tickTrail(sl);
        tickSlam(sl);
        tickSwallow(sl);
        tickHold(sl);
        tickNova(sl);
        if (canAct() && !eating && gap == 0) tickPatterns(sl);
    }

    private void tryWake(ServerLevel sl) {
        Player p = sl.getNearestPlayer(getX(), getY(), getZ(), 14, true);
        if (p == null || Math.abs(p.getY() - getY()) > 10) return;
        wake(sl);
    }

    private void wake(ServerLevel sl) {
        double base = 800.0 * switch (sl.getDifficulty()) {
            case EASY -> 0.75;
            case HARD -> 1.25;
            default -> 1.0;
        };
        AttributeInstance hp = getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(base);
        setHealth((float) base);
        this.entityData.set(AWAKE, true);
        introTicks = 60;
        setState(ST_ROAR);
        playSound(SoundEvents.RAVAGER_ROAR, 3.0f, 0.5f);
        syncBossBar(sl);
    }

    private void resetToDormant() {
        if (prey != null) { PlayerConsumeHandler.releaseForPredator(getUUID()); prey = null; }
        held = null; novaTicks = 0;
        this.entityData.set(AWAKE, false);
        this.entityData.set(SIZE, 1);
        refreshDimensions();
        applyStats();
        setState(ST_IDLE);
        gauge = 0; rainTicks = 0; slamPhase = 0; actionTicks = 0; stun = 0; transitionTicks = 0; introTicks = 0;
        setTarget(null);
        setHealth(getMaxHealth());
        bossEvent.removeAllPlayers();
    }

    private void syncBossBar(ServerLevel sl) {
        for (ServerPlayer sp : sl.players()) {
            if (sp.distanceToSqr(this) < 64 * 64) bossEvent.addPlayer(sp); else bossEvent.removePlayer(sp);
        }
    }

    @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); if (isAwake()) bossEvent.addPlayer(p); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bossEvent.removePlayer(p); }

    private void addGauge(double amount, double healPercent) {
        setHealth(Math.min(getMaxHealth(), getHealth() + (float) (getMaxHealth() * healPercent)));
        if (size() >= MAX_SIZE) return;
        gauge += amount;
        if (gauge >= 100) { gauge -= 100; grow(size() + 1); }
    }

    private void grow(int newSize) {
        newSize = Math.min(MAX_SIZE, newSize);
        this.entityData.set(SIZE, newSize);
        refreshDimensions();
        applyStats();
        transitionTicks = 60;
        held = null; novaTicks = 0;
        setState(ST_ROAR);
        getNavigation().stop();
        playSound(SoundEvents.RAVAGER_ROAR, 3.0f, 0.4f + 0.05f * newSize);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + getBbHeight() * 0.5, getZ(), 6, getBbWidth() * 0.4, 0.3, getBbWidth() * 0.4, 0.0);
            summonWave(sl, 2 + newSize);
            if (newSize == 4) floorHazard();
        }
    }

    private void applyStats() {
        AttributeInstance spd = getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd == null) return;
        spd.removeModifier(RAGE_MOD);
        if (size() >= MAX_SIZE) spd.addTransientModifier(new AttributeModifier(RAGE_MOD, "overlard_rage", -0.35, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private void tickPatterns(ServerLevel sl) {
        LivingEntity t = getTarget();
        if (t == null || !t.isAlive()) return;
        double d = Math.sqrt(distanceToSqr(t));
        float f = factor();
        double reach = getBbWidth() * 0.5 + 1.8;

        if (size() >= 3 && t instanceof ServerPlayer p && validPrey(p) && d < reach) {
            boolean canSwallow = swallowCd == 0, canFeed = grabCd == 0;
            if (canSwallow || canFeed) {
                feedMode = canFeed && (!canSwallow || random.nextBoolean());
                setState(ST_SWALLOW); swallowWind = 0; getNavigation().stop(); return;
            }
        }

        if (size() >= 2 && slamCd == 0 && d > 3.0 && d < 16 && random.nextInt(15) == 0) {
            setState(ST_SLAM); slamPhase = 1; slamTimer = 0; slamTarget = t; getNavigation().stop(); gap = 20; return;
        }

        if (novaCd == 0 && d < 26 && random.nextInt(10) == 0) {
            novaTicks = size() == MAX_SIZE ? 56 : 40; novaRings = 0;
            novaCd = size() == MAX_SIZE ? 160 : 240;
            setState(ST_ROAR); getNavigation().stop(); gap = 30; return;
        }

        if (size() >= 2 && bombCd == 0 && d < 28 && random.nextInt(10) == 0) {
            dropBombs(sl);
            bombCd = size() >= 4 ? 200 : 300;
            setState(ST_THROW); actionTicks = 16; gap = 30; return;
        }

        if (size() < MAX_SIZE && foodCd == 0) {
            foodCd = 90;
            if (countFood(sl) < 3) {
                throwFood(sl, 1 + random.nextInt(2));
                setState(ST_THROW); actionTicks = 14; gap = 20; return;
            }
        }
        if (throwCd == 0 && d < 22 && d > 3 && hasLineOfSight(t)) {
            throwGlobs(t);
            throwCd = size() == MAX_SIZE ? 50 : 80;
            setState(ST_THROW); actionTicks = 12; gap = 20; return;
        }
        if (summonCd == 0) {
            if (countMinions(sl) < 2 + size()) summonWave(sl, 2);
            summonCd = 360;
            setState(ST_ROAR); actionTicks = 20; gap = 20; return;
        }
        if (size() >= 4 && rainCd == 0 && rainTicks == 0) {
            rainTicks = 100; rainCd = 360; gap = 20;
            playSound(SoundEvents.RAVAGER_ROAR, 2.0f, 0.7f);
        }
    }

    public static void globRing(ServerLevel sl, LivingEntity shooter, Vec3 c, int n, double speed, double phase) {
        int cal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());
        for (int i = 0; i < n; i++) {
            double a = phase + i * Math.PI * 2 / n;
            LardGlobEntity g = new LardGlobEntity(sl, shooter, cal, false);
            g.setFullStack(true);
            g.setGravityAffected(false);
            g.setPos(c.x, c.y, c.z);
            g.setDeltaMovement(Math.cos(a) * speed, 0, Math.sin(a) * speed);
            g.setMaxLifeTicks(60);
            sl.addFreshEntity(g);
        }
    }

    public static void globBurst(ServerLevel sl, LivingEntity shooter, Vec3 c, int n, double speed) {
        int cal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());
        sl.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y, c.z, 6, 0.4, 0.4, 0.4, 0.05);
        sl.playSound(null, net.minecraft.core.BlockPos.containing(c), SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
        for (int i = 0; i < n; i++) {
            Vec3 dir = new Vec3(sl.random.nextDouble() * 2 - 1, sl.random.nextDouble() * 2 - 0.6, sl.random.nextDouble() * 2 - 1).normalize();
            LardGlobEntity g = new LardGlobEntity(sl, shooter, cal, false);
            g.setFullStack(true);
            g.setGravityAffected(true);
            g.setPos(c.x, c.y + 0.3, c.z);
            g.setDeltaMovement(dir.scale(speed));
            g.setMaxLifeTicks(60);
            sl.addFreshEntity(g);
        }
    }

    private void tickNova(ServerLevel sl) {
        if (novaTicks <= 0) return;
        getNavigation().stop();
        novaTicks--;
        if (novaTicks % 8 == 0) {
            int n = size() == MAX_SIZE ? 20 : 14;
            double y = getY() + 0.9 + (novaRings % 2) * 0.7;
            globRing(sl, this, new Vec3(getX(), y, getZ()), n, 0.55, novaRings * (Math.PI / n));
            novaRings++;
            playSound(SoundEvents.SLIME_SQUISH, 1.5f, 0.6f + 0.05f * novaRings);
        }
        if (novaTicks == 0) setState(ST_IDLE);
    }

    private void dropBombs(ServerLevel sl) {
        List<ServerPlayer> ps = new ArrayList<>();
        for (ServerPlayer sp : sl.players()) if (!sp.isSpectator() && sp.distanceToSqr(this) < 30 * 30) ps.add(sp);
        if (ps.isEmpty()) return;
        int n = 2 + size() / 2;
        for (int i = 0; i < n; i++) {
            ServerPlayer t = ps.get(random.nextInt(ps.size()));
            double x = t.getX() + (random.nextDouble() - 0.5) * 7, z = t.getZ() + (random.nextDouble() - 0.5) * 7;
            BlockPos bp = BlockPos.containing(x, t.getY() + 2, z);
            int up = 0;
            while (up < 14 && sl.getBlockState(bp.above()).isAir()) { bp = bp.above(); up++; }
            if (up < 3) continue;
            com.eisiadev.oversaturatedgrowth.entity.projectile.LardBombEntity b =
                    new com.eisiadev.oversaturatedgrowth.entity.projectile.LardBombEntity(sl, this);
            b.setPos(x, bp.getY() + 0.5, z);
            b.setDeltaMovement(0, -0.25, 0);
            sl.addFreshEntity(b);
        }
        playSound(SoundEvents.RAVAGER_ROAR, 2.0f, 0.8f);
    }

    private int countFood(ServerLevel sl) {
        int n = 0;
        BlockPos o = blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-13, -2, -13), o.offset(13, 3, 13)))
            if (sl.getBlockState(p).is(ModBlocks.LARD_BLOCK.get())) n++;
        return n;
    }

    private void throwFood(ServerLevel sl, int count) {
        for (int k = 0; k < count; k++) {
            for (int tries = 0; tries < 20; tries++) {
                double a = random.nextDouble() * Math.PI * 2, r = 4.0 + random.nextDouble() * 6.0;
                BlockPos land = BlockPos.containing(getX() + Math.cos(a) * r, getY(), getZ() + Math.sin(a) * r);
                if (!sl.getBlockState(land).isAir() || !sl.getBlockState(land.below()).isFaceSturdy(sl, land.below(), net.minecraft.core.Direction.UP)) continue;
                Vec3 from = new Vec3(getX(), getY() + getBbHeight() * 0.8, getZ());
                BlockPos fp = BlockPos.containing(from);
                if (!sl.getBlockState(fp).isAir()) break;
                net.minecraft.world.entity.item.FallingBlockEntity fb =
                        net.minecraft.world.entity.item.FallingBlockEntity.fall(sl, fp, ModBlocks.LARD_BLOCK.get().defaultBlockState());
                fb.dropItem = false;
                fb.setPos(from.x, from.y, from.z);

                double vy = 0.5, y = from.y, v = vy;
                int ticks = 1;
                while (ticks < 80) { y += v; v = (v - 0.04) * 0.98; if (y <= land.getY() && v < 0) break; ticks++; }
                double dx = land.getX() + 0.5 - from.x, dz = land.getZ() + 0.5 - from.z;
                double k2 = (1 - Math.pow(0.98, ticks)) / 0.02;
                fb.setDeltaMovement(dx / k2, vy, dz / k2);
                fb.hasImpulse = true;
                break;
            }
        }
        swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        playSound(SoundEvents.SNOWBALL_THROW, 1.5f, 0.4f);
    }

    private void throwGlobs(LivingEntity t) {
        getLookControl().setLookAt(t, 40f, 40f);
        swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        Vec3 from = getEyePosition().add(0, -0.2, 0);
        Vec3 to = t.getEyePosition();
        double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        int cal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());
        int n = size() == MAX_SIZE ? 5 : ModServerConfig.globSpitCount.get();
        for (int i = 0; i < n; i++) {
            LardGlobEntity g = new LardGlobEntity(level(), this, cal, false);
            g.setFullStack(true);
            g.setPos(from.x, from.y, from.z);
            g.shoot(dx, dy + dist * 0.06, dz, 1.1f, size() == MAX_SIZE ? 9.0f : 6.0f);
            g.setMaxLifeTicks(80);
            level().addFreshEntity(g);
        }
        playSound(SoundEvents.SNOWBALL_THROW, 1.5f, 0.5f);
    }

    private int countMinions(ServerLevel sl) {
        return sl.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(28), OverlardEntity::isMinion).size();
    }

    private void summonWave(ServerLevel sl, int count) {
        int placed = 0;
        for (int tries = 0; tries < count * 12 && placed < count; tries++) {
            double a = random.nextDouble() * Math.PI * 2, r = 3.0 + random.nextDouble() * 5.0;
            BlockPos pos = BlockPos.containing(getX() + Math.cos(a) * r, getY(), getZ() + Math.sin(a) * r);
            if (!sl.getBlockState(pos).isAir() || !sl.getBlockState(pos.above()).isAir()
                    || !sl.getBlockState(pos.below()).isFaceSturdy(sl, pos.below(), net.minecraft.core.Direction.UP)) continue;
            EntityType<? extends Mob> type = random.nextInt(100) < 55 ? ModEntities.BUTLER.get()
                    : (size() >= 3 && random.nextInt(100) < 35 ? ModEntities.GOURMAND_KNIGHT.get() : ModEntities.CHEF.get());
            Mob m = type.create(sl);
            if (m == null) continue;
            m.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0f);
            m.finalizeSpawn(sl, sl.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
            sl.addFreshEntity(m);
            sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.LARD_SLUDGE_BALL.get())),
                    m.getX(), m.getY() + 0.3, m.getZ(), 14, 0.4, 0.3, 0.4, 0.08);
            placed++;
        }
        if (placed > 0) playSound(SoundEvents.SLIME_SQUISH, 1.5f, 0.5f);
    }

    private void tickTrail(ServerLevel sl) {
        if (size() < 2 || tickCount % 10 != 0 || !onGround()) return;
        Vec3 v = getDeltaMovement();
        if (v.x * v.x + v.z * v.z < 0.0009) return;
        LardPuddleEntity.spawn(sl, position(), 0.9f * factor(), 80);
    }

    private void tickSlam(ServerLevel sl) {
        if (slamPhase == 0) return;
        if (slamPhase == 1) {
            getNavigation().stop();
            if (slamTarget != null && slamTarget.isAlive()) getLookControl().setLookAt(slamTarget, 40f, 40f);
            if (++slamTimer >= 20) {
                if (slamTarget == null || !slamTarget.isAlive()) { slamPhase = 0; setState(ST_IDLE); slamCd = 60; return; }
                Vec3 d = slamTarget.position().subtract(position());
                double hd = Math.max(0.1, Math.sqrt(d.x * d.x + d.z * d.z));
                double sp = Math.max(0.3, Math.min(1.2, hd / 8.5));
                setDeltaMovement(d.x / hd * sp, 0.75, d.z / hd * sp);
                this.hasImpulse = true;
                slamPhase = 2; airTicks = 0;
                playSound(SoundEvents.SLIME_JUMP, 2.0f, 0.4f);
            }
        } else {
            airTicks++;
            if ((onGround() && airTicks > 4) || airTicks > 70) {
                shockwave(sl);
                slamPhase = 0; vulnTicks = 40;
                slamCd = size() == MAX_SIZE ? 100 : 160;
                setState(ST_IDLE);
            }
        }
    }

    private void shockwave(ServerLevel sl) {
        float rm = size() == MAX_SIZE ? 1.6f : 1.0f;
        double r = (2.0 + 0.9 * factor()) * rm;
        playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.5f);
        for (int i = 0; i < 28; i++) {
            double a = i * Math.PI * 2 / 28;
            sl.sendParticles(ParticleTypes.POOF, getX() + Math.cos(a) * r * 0.8, getY() + 0.2, getZ() + Math.sin(a) * r * 0.8, 1, 0.1, 0.05, 0.1, 0.02);
            sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.LARD_SLUDGE_BALL.get())),
                    getX() + Math.cos(a) * r * 0.6, getY() + 0.3, getZ() + Math.sin(a) * r * 0.6, 2, 0.2, 0.2, 0.2, 0.15);
        }
        for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(r, 3, r))) {
            if (p.isSpectator()) continue;
            double dx = p.getX() - getX(), dz = p.getZ() - getZ();
            double h = Math.sqrt(dx * dx + dz * dz);
            if (h > r) continue;
            p.hurt(damageSources().mobAttack(this), 8.0f);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
            double nx = h < 0.01 ? 0 : dx / h, nz = h < 0.01 ? 0 : dz / h;
            p.setDeltaMovement(p.getDeltaMovement().add(nx * 0.9, 0.55, nz * 0.9));
            p.hurtMarked = true;
        }
    }

    private void tickSwallow(ServerLevel sl) {
        if (state() != ST_SWALLOW) return;
        getNavigation().stop();
        if (!(getTarget() instanceof ServerPlayer p) || !validPrey(p) || distanceToSqr(p) > sq(getBbWidth() * 0.5 + 3.0)) {
            setState(ST_IDLE); swallowCd = 60; grabCd = 60; return;
        }
        getLookControl().setLookAt(p, 30f, 30f);
        if (++swallowWind >= 24) {
            if (feedMode) {
                held = p.getUUID(); feeds = 0; feedTicks = 0; holdDamage = 0f;
                playSound(SoundEvents.SLIME_SQUISH, 2.0f, 0.5f);
            } else {
                PlayerConsumeHandler.startMobConsume(this, p, sl, 0.8);
                prey = p.getUUID();
            }
            setTarget(null);
            setState(ST_IDLE);
        }
    }

    private void tickHold(ServerLevel sl) {
        if (held == null) return;
        getNavigation().stop();
        ServerPlayer p = sl.getServer().getPlayerList().getPlayer(held);
        if (p == null || !validPrey(p) || distanceToSqr(p) > 8 * 8) { releaseHeld(40); return; }

        Vec3 dir = p.position().subtract(position());
        Vec3 flat = new Vec3(dir.x, 0, dir.z);
        flat = flat.lengthSqr() < 1.0e-4 ? new Vec3(0, 0, 1) : flat.normalize();
        Vec3 target = new Vec3(getX() + flat.x * (getBbWidth() * 0.5 + 0.6), getY() + 0.2, getZ() + flat.z * (getBbWidth() * 0.5 + 0.6));
        p.setDeltaMovement(target.subtract(p.position()).scale(0.5));
        p.hurtMarked = true;
        p.fallDistance = 0;
        getLookControl().setLookAt(p, 30f, 30f);
        if (++feedTicks % 8 == 0) {
            GgBridge.addCalories(p, BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get() * 1.5));
            playSound(SoundEvents.GENERIC_EAT, 1.2f, 0.5f);
            feeds++;
            if (feeds >= 7) {
                p.addEffect(new MobEffectInstance(com.eisiadev.oversaturatedgrowth.init.ModEffects.OVERSATURATED.get(), 300, 2));
                p.addEffect(new MobEffectInstance(com.eisiadev.oversaturatedgrowth.init.ModEffects.OVEREAT.get(), 300, 0));
                releaseHeld(30);
            }
        }
    }

    private void releaseHeld(int vuln) {
        held = null; feeds = 0; feedTicks = 0;
        grabCd = 260; vulnTicks = Math.max(vulnTicks, vuln);
        setState(ST_IDLE);
    }

    private static double sq(double v) { return v * v; }

    private void tickRain(ServerLevel sl) {
        if (rainTicks <= 0) return;
        rainTicks--;
        if (rainTicks % 5 != 0) return;
        List<ServerPlayer> ps = new ArrayList<>();
        for (ServerPlayer sp : sl.players()) if (!sp.isSpectator() && sp.distanceToSqr(this) < 30 * 30) ps.add(sp);
        if (ps.isEmpty()) return;
        ServerPlayer t = ps.get(random.nextInt(ps.size()));
        double x = t.getX() + (random.nextDouble() - 0.5) * 6, z = t.getZ() + (random.nextDouble() - 0.5) * 6;
        double y = t.getY() + 9;
        BlockPos bp = BlockPos.containing(x, y, z);
        while (!sl.getBlockState(bp).isAir() && y > t.getY() + 2) { y -= 1; bp = BlockPos.containing(x, y, z); }
        if (!sl.getBlockState(bp).isAir()) return;
        LardGlobEntity g = new LardGlobEntity(level(), this, BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get()), false);
        g.setFullStack(true);
        g.setPos(x, y, z);
        g.shoot(0, -1, 0, 0.8f, 2.0f);
        g.setMaxLifeTicks(80);
        level().addFreshEntity(g);
    }

    private void floorHazard() {
        BlockPos c = blockPosition();
        int n = 0;
        for (int i = 0; i < 160 && n < 18; i++) {
            BlockPos p = c.offset(random.nextInt(17) - 8, -1 - random.nextInt(2), random.nextInt(17) - 8);
            if (level().getBlockState(p).is(ModBlocks.POLISHED_LARDSTONE.get())) {
                level().setBlock(p, ModBlocks.LARD_SLUDGE_BLOCK.get().defaultBlockState(), 3);
                n++;
            }
        }
    }

    private class EatGoal extends Goal {
        private Mob minion;
        private BlockPos src;
        private int mode = 0, timer = 0, searchCd = 0;

        EatGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        private Mob findMinion() {
            Mob best = null; double bd = 1e9;
            for (Mob m : level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(12), OverlardEntity::isMinion)) {
                double d = distanceToSqr(m);
                if (d < bd) { bd = d; best = m; }
            }
            return best;
        }

        private BlockPos findFood() {
            BlockPos o = blockPosition(), best = null; double bd = 1e9;
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-13, -2, -13), o.offset(13, 3, 13))) {
                if (!level().getBlockState(p).is(ModBlocks.LARD_BLOCK.get())) continue;
                double d = p.distSqr(o);
                if (d < bd) { bd = d; best = p.immutable(); }
            }
            return best;
        }

        @Override public boolean canUse() {
            if (size() >= MAX_SIZE || biteCd > 0 || !canAct()) return false;
            LivingEntity t = getTarget();
            if (t != null && distanceToSqr(t) < sq(4 + 1.5 * factor())) return false;
            if (searchCd > 0) { searchCd--; return false; }
            searchCd = 20;
            minion = findMinion();
            if (minion != null) { mode = 1; timer = 0; return true; }
            src = findFood();
            if (src != null) { mode = 3; timer = 0; return true; }
            return false;
        }

        @Override public boolean canContinueToUse() {
            return mode != 0 && isAlive() && prey == null && held == null && transitionTicks == 0 && slamPhase == 0;
        }

        @Override public void start() { eating = true; }

        @Override public void tick() {
            timer++;
            if (mode == 1) {
                if (minion == null || !minion.isAlive() || timer > 160) { mode = 0; biteCd = 60; return; }
                getNavigation().moveTo(minion, 1.2);
                getLookControl().setLookAt(minion, 30f, 30f);
                if (distanceToSqr(minion) < sq(getBbWidth() * 0.5 + 1.6)) {
                    mode = 2; timer = 0; setState(ST_PICKUP); getNavigation().stop();
                }
            } else if (mode == 2) {
                getNavigation().stop();
                if (minion != null) getLookControl().setLookAt(minion, 30f, 30f);
                if (timer >= 14) {
                    if (minion != null && minion.isAlive() && level() instanceof ServerLevel sl) {
                        sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.LARD_SLUDGE_BALL.get())),
                                minion.getX(), minion.getY() + minion.getBbHeight() * 0.5, minion.getZ(), 16, 0.3, 0.4, 0.3, 0.1);
                        minion.discard();
                        playSound(SoundEvents.GENERIC_EAT, 1.5f, 0.4f);
                        addGauge(25, 0.04);
                    }
                    biteCd = 80; mode = 0;
                }
            } else if (mode == 3) {
                if (src == null || !level().getBlockState(src).is(ModBlocks.LARD_BLOCK.get()) || timer > 200) { mode = 0; biteCd = 40; return; }
                getNavigation().moveTo(src.getX() + 0.5, src.getY(), src.getZ() + 0.5, 1.2);
                getLookControl().setLookAt(Vec3.atCenterOf(src));
                double dx = src.getX() + 0.5 - getX(), dz = src.getZ() + 0.5 - getZ();
                if (dx * dx + dz * dz < sq(getBbWidth() * 0.5 + 1.8)) {
                    mode = 4; timer = 0; setState(ST_PICKUP); getNavigation().stop();
                    if (level() instanceof ServerLevel sl)
                        ModMessages.sendToNearby(new BlockConsumeVisualS2CPacket(src, Block.getId(level().getBlockState(src)), getId(), 8), sl, Vec3.atCenterOf(src), 48);
                }
            } else if (mode == 4) {
                getNavigation().stop();
                getLookControl().setLookAt(Vec3.atCenterOf(src));
                if (timer >= 20) {
                    if (level().getBlockState(src).is(ModBlocks.LARD_BLOCK.get())) {
                        level().removeBlock(src, false);
                        playSound(SoundEvents.GENERIC_EAT, 1.2f, 0.5f);
                        addGauge(12, 0.015);
                    }
                    biteCd = 50; mode = 0;
                }
            }
        }

        @Override public void stop() {
            mode = 0; minion = null; src = null; eating = false;
            if (state() == ST_PICKUP) setState(ST_IDLE);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!isAwake() && level() instanceof ServerLevel sl) wake(sl);
        if (state() == ST_SWALLOW && source.getEntity() instanceof Player) {
            setState(ST_IDLE); stun = 30; swallowCd = 100; grabCd = 100;
        }
        if (prey != null || held != null || vulnTicks > 0) amount *= 1.5f;
        boolean r = super.hurt(source, amount);
        if (r && held != null) {
            holdDamage += amount;
            if (holdDamage >= getMaxHealth() * 0.05f) { stun = 40; releaseHeld(40); }
        }
        return r;
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            PlayerConsumeHandler.releaseForPredator(getUUID());
            prey = null; held = null;
            if (level() instanceof ServerLevel sl)
                for (Mob m : sl.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(40), OverlardEntity::isMinion)) m.kill();
            bossEvent.removeAllPlayers();
        }
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && prey != null) PlayerConsumeHandler.releaseForPredator(getUUID());
        bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.SLIME_SQUISH; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
    @Override protected void playStepSound(BlockPos pos, BlockState st) { playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.8f, 0.5f); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("OLSize", size());
        tag.putBoolean("OLAwake", isAwake());
        tag.putDouble("OLGauge", gauge);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("OLSize")) this.entityData.set(SIZE, Math.max(1, Math.min(MAX_SIZE, tag.getInt("OLSize"))));
        this.entityData.set(AWAKE, tag.getBoolean("OLAwake"));
        gauge = tag.getDouble("OLGauge");
        refreshDimensions();
        applyStats();
    }
}
