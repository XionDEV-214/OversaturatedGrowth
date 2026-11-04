package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.init.ModEffects;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeterProvider;
import net.willsbr.gluttonousgrowth.WeightSystem.PlayerWeightBarProvider;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;
import net.willsbr.gluttonousgrowth.networking.ModMessages;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.CalorieMeterDelaySyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.OverfullFoodDataSyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.WeightPackets.WeightBarDataSyncPacketS2C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LardSlimeEntity extends Slime {

    private static final int EFFECT_DURATION = 100;

    public LardSlimeEntity(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    @Override
    public float getAttackDamage() {
        return super.getAttackDamage() * 0.5f;
    }

    @Override
    public @NotNull EntityType<? extends Slime> getType() {
        return ModEntities.LARD_SLIME.get();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {

        float d = 0.51f * (float) this.getSize();
        return EntityDimensions.scalable(d, d);
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
                                        @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData groupData,
                                        @Nullable CompoundTag tag) {

        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
        if (spawnType == MobSpawnType.SPAWN_EGG) {

            this.setSize(8, true);
        } else if (spawnType == MobSpawnType.NATURAL
                || spawnType == MobSpawnType.CHUNK_GENERATION) {

            int[] sizes = {2, 4, 8};
            int chosenSize = sizes[this.random.nextInt(sizes.length)];
            this.setSize(chosenSize, true);
        }
        return result;
    }

    private int getEffectAmplifier() {
        int size = this.getSize();
        if (size >= 32) return 5;
        if (size >= 16) return 4;
        if (size >= 8) return 3;
        if (size >= 4) return 2;
        if (size >= 2) return 1;
        return 0;
    }

    @Override
    protected void dealDamage(@NotNull LivingEntity target) {
        super.dealDamage(target);
        if (!this.level().isClientSide && target instanceof ServerPlayer player) {
            int amp = getEffectAmplifier();

            player.addEffect(new MobEffectInstance(
                    ModEffects.WEIGHT_GAINING.get(),
                    EFFECT_DURATION,
                    amp,
                    false,
                    true,
                    true
            ));

            int tier = Integer.numberOfTrailingZeros(Math.max(1, this.getSize()));

            int weightGain = BalanceHelper.scaleWeight(ModServerConfig.slimeBaseWeightPercent.get()) << tier;
            GgBridge.addWeight(player, weightGain);

            int calorieGain = BalanceHelper.scaleCal(ModServerConfig.slimeBaseCalPercent.get()) << tier;
            GgBridge.addCalories(player, calorieGain);
        }
    }

}
