package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeterProvider;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;
import net.willsbr.gluttonousgrowth.networking.ModMessages;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.CalorieMeterDelaySyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.OverfullFoodDataSyncPacketS2C;

import com.eisiadev.oversaturatedgrowth.event.api.EntityConsumedEvent;
import net.minecraftforge.common.MinecraftForge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityConsumeHandler {

    private static final Map<UUID, HoldData> holdMap = new HashMap<>();

    private static final int CONSUME_HOLD_TICKS = 20;

    private static final int CANCEL_TIMEOUT_TICKS = 3;

    private static final float[] EAT_PITCHES = {1.0f, 1.33f, 1.67f, 2.0f};
    private static final int SOUND_INTERVAL = 3;

    private static final ItemStack QUARTZ_STACK = new ItemStack(Items.QUARTZ_BLOCK);

    private static class HoldData {
        final int entityId;
        final long startTick;
        long lastPacketTick;
        int soundStage;

        HoldData(int entityId, long startTick) {
            this.entityId = entityId;
            this.startTick = startTick;
            this.lastPacketTick = startTick;
            this.soundStage = 0;
        }
    }

    public static void onHoldTick(ServerPlayer player, int targetId) {
        if (player.hasEffect(com.eisiadev.oversaturatedgrowth.init.ModEffects.OVEREAT.get())) { cancelHold(player); return; }
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        var entity = serverLevel.getEntity(targetId);
        if (!(entity instanceof LivingEntity target)) {
            cancelHold(player);
            return;
        }

        if (target instanceof ServerPlayer targetPlayer) {

            if (targetPlayer.getUUID().equals(player.getUUID())) {
                cancelHold(player);
                return;
            }

            UUID playerId = player.getUUID();
            long currentTick = serverLevel.getGameTime();
            HoldData existing = holdMap.get(playerId);
            if (existing != null && existing.entityId != targetId) {
                holdMap.remove(playerId);
                existing = null;
            }
            if (existing == null) {
                holdMap.put(playerId, new HoldData(targetId, currentTick));
                return;
            }
            existing.lastPacketTick = currentTick;
            long elapsed = currentTick - existing.startTick;

            if (elapsed % SOUND_INTERVAL == 0 && existing.soundStage < EAT_PITCHES.length) {
                float pitch = EAT_PITCHES[existing.soundStage];
                serverLevel.playSound(null, target.blockPosition(),
                        SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0f, pitch);
                existing.soundStage++;
            }
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, QUARTZ_STACK),
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    3, 0.2, 0.2, 0.2, 0.1);

            if (elapsed >= CONSUME_HOLD_TICKS) {
                holdMap.remove(playerId);
                PlayerConsumeHandler.startPlayerConsume(player, targetPlayer, serverLevel);
            }
            return;
        }

        UUID playerId = player.getUUID();
        long currentTick = serverLevel.getGameTime();
        HoldData existing = holdMap.get(playerId);

        if (existing != null && existing.entityId != targetId) {

            restoreAI(serverLevel, existing.entityId);
            holdMap.remove(playerId);
            existing = null;
        }

        if (existing == null) {
            holdMap.put(playerId, new HoldData(targetId, currentTick));
            if (target instanceof Mob mob) mob.setNoAi(true);
            return;
        }

        existing.lastPacketTick = currentTick;

        long elapsed = currentTick - existing.startTick;
        if (elapsed % SOUND_INTERVAL == 0 && existing.soundStage < EAT_PITCHES.length) {
            float pitch = EAT_PITCHES[existing.soundStage];
            serverLevel.playSound(null, target.blockPosition(),
                    SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0f, pitch);
            existing.soundStage++;
        }

        serverLevel.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, QUARTZ_STACK),
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                3, 0.2, 0.2, 0.2, 0.1);

        if (elapsed >= CONSUME_HOLD_TICKS) {
            holdMap.remove(playerId);
            consumeEntity(player, target, serverLevel);
        }
    }

    public static void cancelHold(ServerPlayer player) {
        HoldData data = holdMap.remove(player.getUUID());
        if (data != null && player.level() instanceof ServerLevel serverLevel) {
            restoreAI(serverLevel, data.entityId);
        }
    }

    public static void tickServer(long currentTick, Iterable<ServerLevel> allLevels) {
        if (holdMap.isEmpty()) return;

        List<UUID> toCancel = new ArrayList<>();
        for (var entry : holdMap.entrySet()) {
            if (currentTick - entry.getValue().lastPacketTick > CANCEL_TIMEOUT_TICKS) {
                toCancel.add(entry.getKey());
            }
        }

        for (UUID id : toCancel) {
            HoldData data = holdMap.remove(id);
            if (data == null) continue;
            for (ServerLevel level : allLevels) {
                restoreAI(level, data.entityId);
            }
        }
    }

    private static void restoreAI(ServerLevel level, int entityId) {
        var entity = level.getEntity(entityId);
        if (entity instanceof Mob mob) mob.setNoAi(false);
    }

    private static void consumeEntity(ServerPlayer player, LivingEntity target, ServerLevel serverLevel) {
        if (target instanceof Mob mob) mob.setNoAi(false);

        int calPerHealth = BalanceHelper.scaleCal(ModServerConfig.consumeCalPerHealthPercent.get());
        int calories = (int)(target.getMaxHealth() * calPerHealth);
        calories = Math.max(1, calories);
        final int finalCalories = calories;

        GgBridge.addCalories(player, finalCalories);

        serverLevel.sendParticles(
                ParticleTypes.HEART,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                8, 0.3, 0.3, 0.3, 0.05);
        serverLevel.playSound(null, target.blockPosition(),
                com.eisiadev.oversaturatedgrowth.init.ModSounds.SWALLOW.get(), SoundSource.PLAYERS,
                1.0f, 0.8f + (float)Math.random() * 0.4f);

        MinecraftForge.EVENT_BUS.post(new EntityConsumedEvent(player, target, finalCalories));
        target.discard();

        OversaturatedGrowth.LOGGER.debug("Player {} consumed entity {} for {} calories",
                player.getName().getString(), target.getType().toShortString(), finalCalories);
    }
}
