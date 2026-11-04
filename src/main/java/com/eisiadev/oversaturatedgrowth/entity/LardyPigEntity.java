package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LardyPigEntity extends Pig implements IPlump {

    private static final EntityDataAccessor<Integer> PLUMP =
            SynchedEntityData.defineId(LardyPigEntity.class, EntityDataSerializers.INT);

    public LardyPigEntity(EntityType<? extends LardyPigEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PLUMP, 0);
    }

    @Override public int getPlumpLevel() { return this.entityData.get(PLUMP); }
    @Override public void setPlumpLevel(int l) { this.entityData.set(PLUMP, Math.max(0, Math.min(MAX_PLUMP, l))); }

    @Override
    public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> key) {
        if (PLUMP.equals(key)) this.refreshDimensions();
        super.onSyncedDataUpdated(key);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float s = IPlump.scaleFor(getPlumpLevel());
        return super.getDimensions(pose).scale(WIDE * s, s);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult r = PlumpHelper.tryFeed(this, player, hand);
        if (r.consumesAction()) return r;
        return super.mobInteract(player, hand);
    }

    @Override
    public Pig getBreedOffspring(ServerLevel level, AgeableMob other) {
        return ModEntities.LARDY_PIG.get().create(level);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource src, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(src, looting, recentlyHit);
        if (isPlump()) PlumpHelper.dropExtra(this, getPlumpLevel(), looting);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PlumpLevel", getPlumpLevel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPlumpLevel(tag.contains("PlumpLevel") ? tag.getInt("PlumpLevel") : (tag.getBoolean("Plump") ? 1 : 0));
    }
}
