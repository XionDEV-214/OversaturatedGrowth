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

public class LardRetcherEntity extends Monster {

    private static final double RANGED_DISTANCE    = 8.0;
        private static final float  SPREAD_DEGREES     = 6.0f;
    private static final int    SHOOT_COOLDOWN     = 50;
    private static final float  DEATH_PELLET_SPEED = 0.4f;
    private static final int    DEATH_PELLETS      = 10;

    private int shootCooldown = 0;

    public LardRetcherEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    public EntityType<?> getType() {
        return ModEntities.LARD_RETCHER.get();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (shootCooldown > 0) shootCooldown--;

            Player target = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 28, true);
            if (target != null) {
                double dist = this.distanceTo(target);
                if (dist >= RANGED_DISTANCE && shootCooldown <= 0) {
                    shootShotgun(target);
                    shootCooldown = SHOOT_COOLDOWN;
                }
            }
        }
    }

    private static final double GRAVITY        = 0.025;
    private static final double MIN_SPEED      = 0.65;
    private static final double MAX_SPEED      = 1.6;

    private void shootShotgun(Player target) {
        this.level().playSound(null, this.blockPosition(),
                SoundEvents.PLAYER_BURP, SoundSource.HOSTILE, 1.2f, 0.6f);
        int pelletCal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());

        Vec3 shootFrom = this.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        Vec3 ballisticDir = calcBallisticVelocity(shootFrom, targetPos);
        int flightTicks = calcFlightTicks(shootFrom, targetPos, ballisticDir.length());

        for (int i = 0; i < ModServerConfig.globSpitCount.get(); i++) {
            spawnPellet(applySpread(ballisticDir, SPREAD_DEGREES), false, pelletCal, flightTicks);
        }
    }

    private void spawnPellet(Vec3 velocity, boolean homing, int calorie, int flightTicks) {
        LardGlobEntity glob = new LardGlobEntity(this.level(), this, calorie, homing);
        double spawnX = this.getX();
        double spawnY = this.getEyeY();
        double spawnZ = this.getZ();
        glob.setPos(spawnX, spawnY, spawnZ);
        glob.setDeltaMovement(velocity);
        glob.setMaxLifeTicks(flightTicks + 10);
        this.level().addFreshEntity(glob);

        if (this.level() instanceof ServerLevel serverLevel) {
            ItemParticleOption particle = new ItemParticleOption(
                    ParticleTypes.ITEM, new ItemStack(ModItems.LARD_SLUDGE_BALL.get()));
            serverLevel.sendParticles(particle, spawnX, spawnY, spawnZ,
                    4, 0.15, 0.15, 0.15, 0.08);
        }
    }

    private Vec3 calcBallisticVelocity(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        double t = Math.max(0.0, Math.min(1.0, (hDist - RANGED_DISTANCE) / (28.0 - RANGED_DISTANCE)));
        double speedH = MIN_SPEED + t * (MAX_SPEED - MIN_SPEED);

        double T = (hDist < 0.001) ? 1.0 : hDist / speedH;

        double Vy = (dy + 0.5 * GRAVITY * T * T) / T;

        double invH = (hDist < 0.001) ? 0.0 : speedH / hDist;
        return new Vec3(dx * invH, Vy, dz * invH);
    }

    private int calcFlightTicks(Vec3 from, Vec3 to, double speed) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double t = Math.max(0.0, Math.min(1.0, (hDist - RANGED_DISTANCE) / (28.0 - RANGED_DISTANCE)));
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
            int meleeCal = BalanceHelper.scaleCal(ModServerConfig.retcherMeleeCalPercent.get());
            applyCalories(player, meleeCal);
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                        this.getX(), this.getY() + this.getBbHeight() / 2, this.getZ(),
                        12, 0.4, 0.4, 0.4, 0.05);
                serverLevel.playSound(null, this.blockPosition(),
                        net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                        net.minecraft.sounds.SoundSource.HOSTILE, 0.8f, 1.2f);
            }
            int pelletCal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());
            for (int i = 0; i < DEATH_PELLETS; i++) {
                Vec3 randomDir = new Vec3(
                        this.random.nextDouble() * 2 - 1,
                        this.random.nextDouble() * 2 - 1,
                        this.random.nextDouble() * 2 - 1
                ).normalize();
                LardGlobEntity glob = new LardGlobEntity(this.level(), this, pelletCal, false);
                glob.setGravityAffected(true);
                glob.setPos(this.getX(), this.getEyeY(), this.getZ());
                glob.setDeltaMovement(randomDir.scale(DEATH_PELLET_SPEED));
                this.level().addFreshEntity(glob);
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
