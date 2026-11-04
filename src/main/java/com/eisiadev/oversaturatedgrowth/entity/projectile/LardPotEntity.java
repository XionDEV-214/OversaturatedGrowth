package com.eisiadev.oversaturatedgrowth.entity.projectile;

import com.eisiadev.oversaturatedgrowth.entity.LardPuddleEntity;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class LardPotEntity extends ThrowableItemProjectile {
    public LardPotEntity(EntityType<? extends LardPotEntity> type, Level level) { super(type, level); }

    public LardPotEntity(Level level, LivingEntity shooter) { super(ModEntities.LARD_POT.get(), shooter, level); }

    @Override protected Item getDefaultItem() { return Items.CAULDRON; }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;
        LardPuddleEntity.spawn(level(), result.getLocation(), 3.0f, 120);
        level().playSound(null, blockPosition(), SoundEvents.HONEY_BLOCK_BREAK, SoundSource.HOSTILE, 1.0f, 0.7f);
        discard();
    }
}
