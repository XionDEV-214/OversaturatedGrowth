package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
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

public class LardySheepEntity extends Sheep implements IPlump {

    private static final EntityDataAccessor<Integer> PLUMP =
            SynchedEntityData.defineId(LardySheepEntity.class, EntityDataSerializers.INT);

    public LardySheepEntity(EntityType<? extends LardySheepEntity> type, Level level) {
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
    public Sheep getBreedOffspring(ServerLevel level, AgeableMob other) {
        return ModEntities.LARDY_SHEEP.get().create(level);
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

    @Override
    public DyeColor getColor() { return DyeColor.ORANGE; }

    @Override
    public net.minecraft.resources.ResourceLocation getDefaultLootTable() {
        return this.isSheared()
                ? net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.eisiadev.oversaturatedgrowth.OversaturatedGrowth.MODID, "entities/lardy_sheep_sheared")
                : ModEntities.LARDY_SHEEP.get().getDefaultLootTable();
    }

    @Override
    public void shear(net.minecraft.sounds.SoundSource src) {
        this.level().playSound(null, this, net.minecraft.sounds.SoundEvents.SHEEP_SHEAR, src, 1.0F, 1.0F);
        this.setSheared(true);
        int n = 1 + this.random.nextInt(3);
        for (int j = 0; j < n; ++j) {
            net.minecraft.world.entity.item.ItemEntity e = this.spawnAtLocation(com.eisiadev.oversaturatedgrowth.init.ModBlocks.LARDY_WOOL.get(), 1);
            if (e != null) e.setDeltaMovement(e.getDeltaMovement().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
        }
    }

    @Override
    public java.util.List<net.minecraft.world.item.ItemStack> onSheared(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack item, Level level, net.minecraft.core.BlockPos pos, int fortune) {
        level.playSound(null, this, net.minecraft.sounds.SoundEvents.SHEEP_SHEAR, player == null ? net.minecraft.sounds.SoundSource.BLOCKS : net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level.isClientSide) return java.util.Collections.emptyList();
        this.setSheared(true);
        int n = 1 + this.random.nextInt(3);
        java.util.List<net.minecraft.world.item.ItemStack> items = new java.util.ArrayList<>();
        for (int j = 0; j < n; ++j) items.add(new net.minecraft.world.item.ItemStack(com.eisiadev.oversaturatedgrowth.init.ModBlocks.LARDY_WOOL.get()));
        return items;
    }
}
