package com.eisiadev.oversaturatedgrowth.entity.projectile;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LardShardEntity extends ThrowableItemProjectile {

    public static final float DAMAGE = 4.0f;

    public LardShardEntity(EntityType<? extends LardShardEntity> type, Level level) {
        super(type, level);
    }

    public LardShardEntity(EntityType<? extends LardShardEntity> type, Level level, LivingEntity owner) {
        super(type, owner, level);
    }

    @Override
    protected Item getDefaultItem() { return ModBlocks.LARDSTONE.get().asItem(); }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), DAMAGE);
        if (result.getEntity() instanceof ServerPlayer p) {
            GgBridge.addCalories(p, BalanceHelper.scaleCal(ModServerConfig.globCalPercent.get() * 0.5));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel sl) {
            Block b = Block.byItem(this.getItem().getItem());
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, b.defaultBlockState()),
                    this.getX(), this.getY(), this.getZ(), 16, 0.2, 0.2, 0.2, 0.05);
            this.playSound(net.minecraft.sounds.SoundEvents.STONE_BREAK, 1.0f, 0.9f);
            this.discard();
        }
    }
}
