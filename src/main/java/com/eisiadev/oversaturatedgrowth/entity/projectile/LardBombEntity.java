package com.eisiadev.oversaturatedgrowth.entity.projectile;

import com.eisiadev.oversaturatedgrowth.entity.OverlardEntity;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class LardBombEntity extends ThrowableItemProjectile {
    public LardBombEntity(EntityType<? extends LardBombEntity> type, Level level) { super(type, level); }

    public LardBombEntity(Level level, LivingEntity shooter) { super(ModEntities.LARD_BOMB.get(), shooter, level); }

    @Override protected Item getDefaultItem() { return ModBlocks.LARD_BLOCK.get().asItem(); }

    @Override protected float getGravity() { return 0.06f; }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;
        if (getOwner() instanceof LivingEntity o && level() instanceof ServerLevel sl) {
            OverlardEntity.globBurst(sl, o, result.getLocation(), 18, 0.6);
        }
        discard();
    }
}
