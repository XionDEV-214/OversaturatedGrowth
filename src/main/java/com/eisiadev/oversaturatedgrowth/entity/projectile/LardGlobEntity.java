package com.eisiadev.oversaturatedgrowth.entity.projectile;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import com.eisiadev.oversaturatedgrowth.event.api.LardNeedsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeterProvider;
import net.willsbr.gluttonousgrowth.WeightSystem.PlayerWeightBarProvider;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;
import net.willsbr.gluttonousgrowth.networking.ModMessages;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.CalorieMeterDelaySyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.OverfullFoodDataSyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.WeightPackets.WeightBarDataSyncPacketS2C;

public class LardGlobEntity extends Projectile {

    private static final int DEFAULT_MAX_LIFE_TICKS = 40;

    private static final double HOMING_STRENGTH = 0.022;

    private static final double GRAVITY = 0.025;

    private int lifeTicks = 0;
    private int maxLifeTicks = DEFAULT_MAX_LIFE_TICKS;
    private int calorieDamage = 8;
    private boolean homing = false;
    private boolean gravityAffected = true;
    private boolean fullStack = false;

    public LardGlobEntity(EntityType<? extends LardGlobEntity> type, Level level) {
        super(type, level);
    }

    public LardGlobEntity(Level level, LivingEntity shooter, int calorieDamage, boolean homing) {
        super(ModEntities.LARD_GLOB.get(), level);
        this.calorieDamage = calorieDamage;
        this.homing = homing;
        this.gravityAffected = true;
        this.setOwner(shooter);
        Vec3 pos = shooter.getEyePosition().add(0, -0.1, 0);
        this.setPos(pos.x, pos.y, pos.z);
    }

    public void setGravityAffected(boolean gravityAffected) {
        this.gravityAffected = gravityAffected;
    }

    public void setFullStack(boolean v) { this.fullStack = v; }

    public void setMaxLifeTicks(int ticks) {
        this.maxLifeTicks = ticks;
    }

    @Override
    protected void defineSynchedData() { }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (lifeTicks > maxLifeTicks) {
            this.discard();
            return;
        }

        if (homing && !this.level().isClientSide) {
            LivingEntity target = findNearestPlayer();
            if (target != null) {
                Vec3 toTarget = target.getEyePosition().subtract(this.position()).normalize();
                Vec3 current = this.getDeltaMovement();
                Vec3 steered = current.add(toTarget.scale(HOMING_STRENGTH)).normalize()
                        .scale(current.length());
                this.setDeltaMovement(steered);
            }
        }

        if (gravityAffected) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -GRAVITY, 0));
        }

        Vec3 delta = this.getDeltaMovement();

        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onHit(hit);
            return;
        }

        this.setPos(this.getX() + delta.x, this.getY() + delta.y, this.getZ() + delta.z);
        this.updateRotation();
    }

    private LivingEntity findNearestPlayer() {
        return this.level().getNearestPlayer(this, 32);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity == this.getOwner()) return false;

        return entity instanceof Player;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        Entity target = result.getEntity();
        if (!(target instanceof ServerPlayer player)) {

            return;
        }

        if (player.isBlocking()) {
            this.discard();
            return;
        }

        this.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.SLIME_SQUISH, net.minecraft.sounds.SoundSource.HOSTILE, 0.6f, 1.2f);

        applyCaloriesOrWeight(player, calorieDamage);

        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    private void applyCaloriesOrWeight(ServerPlayer player, int amount) {
        if (fullStack && GgBridge.isCalorieFull(player)) {
            com.eisiadev.oversaturatedgrowth.effect.EffectStacking.stackFullness(player);
        } else if (GgBridge.isCalorieFull(player)) {

            GgBridge.addWeight(player, BalanceHelper.scaleWeight(ModServerConfig.globWeightOnFullCalPercent.get()));
        } else if (GgBridge.addCalories(player, amount)) {
            MinecraftForge.EVENT_BUS.post(new LardNeedsEvent(player, amount));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LifeTicks", lifeTicks);
        tag.putInt("MaxLifeTicks", maxLifeTicks);
        tag.putInt("CalorieDamage", calorieDamage);
        tag.putBoolean("Homing", homing);
        tag.putBoolean("GravityAffected", gravityAffected);
        tag.putBoolean("FullStack", fullStack);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        lifeTicks = tag.getInt("LifeTicks");
        maxLifeTicks = tag.contains("MaxLifeTicks") ? tag.getInt("MaxLifeTicks") : DEFAULT_MAX_LIFE_TICKS;
        calorieDamage = tag.getInt("CalorieDamage");
        homing = tag.getBoolean("Homing");
        gravityAffected = tag.getBoolean("GravityAffected");
        fullStack = tag.getBoolean("FullStack");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
