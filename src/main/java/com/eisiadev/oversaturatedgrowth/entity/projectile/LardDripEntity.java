package com.eisiadev.oversaturatedgrowth.entity.projectile;

import com.eisiadev.oversaturatedgrowth.init.ModItems;
import com.eisiadev.oversaturatedgrowth.util.LardColors;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LardDripEntity extends ThrowableItemProjectile {

    public LardDripEntity(EntityType<? extends LardDripEntity> type, Level level) { super(type, level); }

    public LardDripEntity(EntityType<? extends LardDripEntity> type, Level level, double x, double y, double z) {
        super(type, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() { return ModItems.LARD_SLUDGE_BALL.get(); }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) return;
        if (result instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        }
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        cloud.setRadius(1.4f);
        cloud.setDuration(160);
        cloud.setRadiusPerTick(-1.4f / 160f);
        cloud.setFixedColor(LardColors.SLUDGE);
        cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        this.level().addFreshEntity(cloud);
        this.playSound(net.minecraft.sounds.SoundEvents.SLIME_SQUISH_SMALL, 0.8f, 0.7f);
        this.discard();
    }
}
