package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.entity.projectile.LardGlobEntity;
import com.eisiadev.oversaturatedgrowth.init.ModEffects;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LardCrawlerEntity extends Spider {

    private static final int FEED_INTERVAL = 20;
    private static final int FEEDS_UNTIL_OVEREAT = 8;

    private static final EntityDataAccessor<Integer> ATTACHED_ID =
            SynchedEntityData.defineId(LardCrawlerEntity.class, EntityDataSerializers.INT);

    private int attachCooldown = 0;
    private int feedTimer = 0;
    private int feedCount = 0;
    private int spitCooldown = 40;

    public LardCrawlerEntity(EntityType<? extends LardCrawlerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACHED_ID, -1);
    }

    public boolean isAttached() { return this.entityData.get(ATTACHED_ID) >= 0; }

    @Override
    public void tick() {
        super.tick();
        int id = this.entityData.get(ATTACHED_ID);
        if (id < 0) return;
        net.minecraft.world.entity.Entity e = this.level().getEntity(id);
        if (e instanceof LivingEntity m) {
            this.noPhysics = true;

            float hy = m.getYHeadRot();
            double yaw = Math.toRadians(hy);
            double d = 0.7;
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(m.getX() - Math.sin(yaw) * d, m.getY() + m.getEyeHeight() - 0.68, m.getZ() + Math.cos(yaw) * d);
            this.yBodyRot = hy;
            this.setYRot(hy);
            this.yRotO = hy;
            this.yHeadRot = hy;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        if (attachCooldown > 0) attachCooldown--;
        if (spitCooldown > 0) spitCooldown--;

        if (this.isAttached()) {
            if (!(this.level().getEntity(this.entityData.get(ATTACHED_ID)) instanceof ServerPlayer sp) || !sp.isAlive() || sp.isCreative() || sp.isSpectator()
                    || sp.hasEffect(ModEffects.OVEREAT.get())) {
                detach();
                return;
            }
            if (++feedTimer >= FEED_INTERVAL) {
                feedTimer = 0;
                GgBridge.addCalories(sp, BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get() * 0.4));
                this.playSound(SoundEvents.GENERIC_EAT, 0.6f, 0.6f);
                if (++feedCount >= FEEDS_UNTIL_OVEREAT) {
                    sp.addEffect(new MobEffectInstance(ModEffects.OVEREAT.get(), 600, 0));
                }
            }
            return;
        }

        LivingEntity t = this.getTarget();
        if (t instanceof ServerPlayer p && !p.isCreative() && !p.isSpectator()) {
            if (attachCooldown == 0 && !p.hasEffect(ModEffects.OVEREAT.get())
                    && this.distanceToSqr(p) < 1.6 * 1.6
                    && !isAnyCrawlerOn(p)) {
                attach(p);
            } else if (p.hasEffect(ModEffects.OVEREAT.get()) && spitCooldown == 0 && this.hasLineOfSight(p)) {
                double d = this.distanceTo(p);
                if (d > 3 && d < 14) { spit(p, d); spitCooldown = 50; }
            }
        }
    }

    private boolean isAnyCrawlerOn(ServerPlayer p) {
        return !this.level().getEntitiesOfClass(LardCrawlerEntity.class, p.getBoundingBox().inflate(2.0),
                c -> c != this && c.entityData.get(ATTACHED_ID) == p.getId()).isEmpty();
    }

    private void attach(ServerPlayer p) {
        this.getNavigation().stop();
        this.entityData.set(ATTACHED_ID, p.getId());
        this.setNoGravity(true);
        this.noPhysics = true;
        feedTimer = 0; feedCount = 0;
        this.playSound(SoundEvents.SLIME_SQUISH, 0.8f, 0.7f);
    }

    private void detach() {
        this.entityData.set(ATTACHED_ID, -1);
        this.setNoGravity(false);
        this.noPhysics = false;
        attachCooldown = 200;
        feedCount = 0;
        this.setDeltaMovement(this.getDeltaMovement().add(0, 0.3, 0));
        this.playSound(SoundEvents.SLIME_JUMP, 0.8f, 0.8f);
    }

    private void spit(ServerPlayer target, double dist) {
        int cal = BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get());
        LardGlobEntity glob = new LardGlobEntity(this.level(), this, cal, false);
        Vec3 from = this.getEyePosition();
        Vec3 to = target.getEyePosition();
        Vec3 dir = to.subtract(from).normalize();
        double speed = 0.9;
        glob.setPos(from.x, from.y, from.z);
        glob.setDeltaMovement(dir.x * speed, dir.y * speed + dist * 0.02, dir.z * speed);
        glob.setMaxLifeTicks((int) (dist / speed) + 15);
        this.level().addFreshEntity(glob);
        this.playSound(SoundEvents.LLAMA_SPIT, 1.0f, 0.7f);
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource src) {
        this.entityData.set(ATTACHED_ID, -1);
        this.noPhysics = false;
        super.die(src);
    }
}
