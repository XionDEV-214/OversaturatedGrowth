package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.entity.projectile.LardGlobEntity;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeterProvider;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;
import net.willsbr.gluttonousgrowth.networking.ModMessages;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.CalorieMeterDelaySyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.OverfullFoodDataSyncPacketS2C;
public class MotherRetcherEntity extends Monster {

    public static final float MODEL_SCALE = 2.0f;

    private static final double RANGED_DISTANCE    = 8.0;

        private static final float  SPREAD_DEGREES     = 6.0f;

    private static final int    BURST_SHOTS        = 4;
    private static final int    BURST_INTERVAL     = 2;

    private static final int    SHOOT_COOLDOWN_MIN = 60;
    private static final int    SHOOT_COOLDOWN_MAX = 80;

    private static final float  DEATH_PELLET_SPEED = 0.4f;
    private static final int    DEATH_PELLETS      = 15;

    private int    shootCooldown  = 0;
    private int    burstRemaining = 0;
    private int    burstTimer     = 0;
    private Player burstTarget    = null;

    public MotherRetcherEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    public EntityType<?> getType() {
        return ModEntities.MOTHER_RETCHER.get();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (burstRemaining > 0) {
            burstTimer++;
            if (burstTimer >= BURST_INTERVAL) {
                burstTimer = 0;
                fireShotgunBurst();
                burstRemaining--;
            }
            return;
        }

        if (shootCooldown > 0) shootCooldown--;

        Player target = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 32, true);
        if (target != null && this.distanceTo(target) >= RANGED_DISTANCE && shootCooldown <= 0) {
            startBurst(target);
        }
    }

    private void startBurst(Player target) {
        burstTarget    = target;
        burstRemaining = BURST_SHOTS;
        burstTimer     = BURST_INTERVAL;

        shootCooldown  = SHOOT_COOLDOWN_MIN + this.random.nextInt(SHOOT_COOLDOWN_MAX - SHOOT_COOLDOWN_MIN + 1);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY() + this.getBbHeight() / 2, this.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private static final double GRAVITY        = 0.025;
    private static final double MIN_SPEED      = 0.65;
    private static final double MAX_SPEED      = 1.6;

    private void fireShotgunBurst() {
        if (burstTarget == null || !burstTarget.isAlive()) return;

        this.level().playSound(null, this.blockPosition(),
                SoundEvents.PLAYER_BURP, SoundSource.HOSTILE, 1.5f, 0.5f);

        Vec3 shootFrom = this.getEyePosition();

        Vec3 targetPos = new Vec3(burstTarget.getX(), burstTarget.getY() + 0.9, burstTarget.getZ());
        Vec3 ballisticDir = calcBallisticVelocity(shootFrom, targetPos);
        int flightTicks = calcFlightTicks(shootFrom, targetPos);
        int pelletCal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());

        double spawnX = shootFrom.x;
        double spawnY = shootFrom.y;
        double spawnZ = shootFrom.z;

        if (this.level() instanceof ServerLevel serverLevel) {
            ItemParticleOption particle = new ItemParticleOption(
                    ParticleTypes.ITEM, new ItemStack(ModItems.LARD_SLUDGE_BALL.get()));
            serverLevel.sendParticles(particle, spawnX, spawnY, spawnZ,
                    8, 0.2, 0.2, 0.2, 0.1);
        }

        for (int i = 0; i < ModServerConfig.globSpitCount.get(); i++) {
            Vec3 spread = applySpread(ballisticDir, SPREAD_DEGREES);
            LardGlobEntity glob = new LardGlobEntity(this.level(), this, pelletCal, false);
            glob.setPos(spawnX, spawnY, spawnZ);
            glob.setDeltaMovement(spread);
            glob.setMaxLifeTicks(flightTicks + 10);
            this.level().addFreshEntity(glob);
        }
    }

    private Vec3 calcBallisticVelocity(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        double t = Math.max(0.0, Math.min(1.0, (hDist - RANGED_DISTANCE) / (32.0 - RANGED_DISTANCE)));
        double speedH = MIN_SPEED + t * (MAX_SPEED - MIN_SPEED);

        double T = (hDist < 0.001) ? 1.0 : hDist / speedH;
        double Vy = (dy + 0.5 * GRAVITY * T * T) / T;

        double invH = (hDist < 0.001) ? 0.0 : speedH / hDist;
        return new Vec3(dx * invH, Vy, dz * invH);
    }

    private int calcFlightTicks(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double t = Math.max(0.0, Math.min(1.0, (hDist - RANGED_DISTANCE) / (32.0 - RANGED_DISTANCE)));
        double speedH = MIN_SPEED + t * (MAX_SPEED - MIN_SPEED);
        return (int) Math.ceil(hDist / speedH) + 5;
    }

    private Vec3 applySpread(Vec3 vel, float maxDegrees) {
        double speed = vel.length();
        Vec3 dir = (speed < 0.0001) ? vel : vel.scale(1.0 / speed);
        float yawOffset   = (this.random.nextFloat() * 2f - 1f) * maxDegrees;
        float pitchOffset = (this.random.nextFloat() * 2f - 1f) * maxDegrees;
        double yawRad   = Math.toRadians(yawOffset);
        double pitchRad = Math.toRadians(pitchOffset);
        double cos = Math.cos(pitchRad);
        Vec3 rotated = new Vec3(
                dir.x * Math.cos(yawRad) - dir.z * Math.sin(yawRad),
                dir.y * cos + Math.sqrt(dir.x * dir.x + dir.z * dir.z) * Math.sin(pitchRad),
                dir.x * Math.sin(yawRad) + dir.z * Math.cos(yawRad)
        ).normalize();
        return rotated.scale(speed);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && target instanceof ServerPlayer player) {
            int meleeCal = BalanceHelper.scaleCal(ModServerConfig.motherMeleeCalPercent.get());
            applyCalories(player, meleeCal);
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + this.getBbHeight() / 2, this.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.playSound(null, this.blockPosition(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.5f, 0.8f);

            int pelletCal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());
            for (int i = 0; i < DEATH_PELLETS; i++) {
                Vec3 randomDir = new Vec3(
                        this.random.nextDouble() * 2 - 1,
                        this.random.nextDouble() * 2 - 1,
                        this.random.nextDouble() * 2 - 1
                ).normalize();
                LardGlobEntity glob = new LardGlobEntity(this.level(), this, pelletCal, false);
                glob.setPos(this.getX(), this.getEyeY(), this.getZ());
                glob.setDeltaMovement(randomDir.scale(DEATH_PELLET_SPEED));
                glob.setGravityAffected(true);
                this.level().addFreshEntity(glob);
            }

            int count = 3 + this.random.nextInt(4);
            for (int i = 0; i < count; i++) {
                LardRetcherEntity retcher = new LardRetcherEntity(
                        ModEntities.LARD_RETCHER.get(), serverLevel);
                double angle = (2 * Math.PI / count) * i;
                retcher.setPos(
                        this.getX() + Math.cos(angle) * 1.5,
                        this.getY(),
                        this.getZ() + Math.sin(angle) * 1.5);
                retcher.finalizeSpawn(serverLevel,
                        serverLevel.getCurrentDifficultyAt(this.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, null, null);
                serverLevel.addFreshEntity(retcher);
            }
        }
    }

    private void applyCalories(ServerPlayer player, int amount) {
        GgBridge.addCalories(player, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }
}
