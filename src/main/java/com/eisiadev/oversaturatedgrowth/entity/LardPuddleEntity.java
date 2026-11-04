package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class LardPuddleEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(LardPuddleEntity.class, EntityDataSerializers.FLOAT);
    private int life = 120;

    public LardPuddleEntity(EntityType<? extends LardPuddleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void spawn(Level level, Vec3 pos, float radius, int life) {
        LardPuddleEntity p = new LardPuddleEntity(ModEntities.LARD_PUDDLE.get(), level);
        p.setPos(pos.x, pos.y, pos.z);
        p.entityData.set(RADIUS, radius);
        p.life = life;
        level.addFreshEntity(p);
    }

    @Override protected void defineSynchedData() { this.entityData.define(RADIUS, 3.0f); }
    public float radius() { return this.entityData.get(RADIUS); }

    @Override
    public void tick() {
        super.tick();
        float r = radius();
        if (level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                double a = random.nextDouble() * Math.PI * 2, d = Math.sqrt(random.nextDouble()) * r;
                level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.LARD_SLUDGE_BALL.get())),
                        getX() + Math.cos(a) * d, getY() + 0.05, getZ() + Math.sin(a) * d, 0, 0.02, 0);
            }
            return;
        }
        if (--life <= 0) { discard(); return; }
        if (tickCount % 10 != 0) return;
        AABB box = new AABB(getX() - r, getY() - 0.5, getZ() - r, getX() + r, getY() + 2.0, getZ() + r);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, box)) {
            double dx = e.getX() - getX(), dz = e.getZ() - getZ();
            if (dx * dx + dz * dz > r * r) continue;
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false, true));
            if (tickCount % 20 == 0 && e instanceof ServerPlayer sp) GgBridge.addCalories(sp, BalanceHelper.scaleCal(0.5));
        }
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        life = tag.getInt("Life"); entityData.set(RADIUS, tag.contains("Radius") ? tag.getFloat("Radius") : 3.0f);
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putInt("Life", life); tag.putFloat("Radius", radius()); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
