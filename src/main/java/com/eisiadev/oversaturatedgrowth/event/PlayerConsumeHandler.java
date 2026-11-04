package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.PlayerConsumedS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.PredatorConsumeStateS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.PlayerConsumeResultS2CPacket;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.willsbr.gluttonousgrowth.CPMCompat.Capability.CPMData;
import net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeterProvider;
import net.willsbr.gluttonousgrowth.config.GluttonousClientConfig;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.CalorieMeterDelaySyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.OverfullFoodDataSyncPacketS2C;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;

import com.eisiadev.oversaturatedgrowth.event.api.EntityConsumedEvent;
import net.minecraftforge.common.MinecraftForge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerConsumeHandler {

    private static final Map<UUID, ConsumeData> activeConsumeMap = new HashMap<>();

    private static class ConsumeData {
        final UUID predatorId;
        final int timeLimitTicks;
        final int requiredPresses;
        final GameType savedGameType;
        long startTick;
        int currentPresses;

        ConsumeData(UUID predatorId, int timeLimitTicks, int requiredPresses, long startTick, GameType savedGameType) {
            this.predatorId = predatorId;
            this.timeLimitTicks = timeLimitTicks;
            this.requiredPresses = requiredPresses;
            this.startTick = startTick;
            this.currentPresses = 0;
            this.savedGameType = savedGameType;
        }
    }

    public static void startPlayerConsume(ServerPlayer predator, ServerPlayer prey, ServerLevel level) {
        int timeSec = ModServerConfig.playerConsumeEscapeTimeSec.get();
        int timeTicks = (timeSec == -1) ? -1 : timeSec * 20;
        int requiredPresses = ModServerConfig.playerConsumeEscapeKeyPresses.get();

        var srv = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        long startTick = (srv != null) ? srv.getTickCount() : level.getGameTime();
        activeConsumeMap.put(prey.getUUID(),
                new ConsumeData(predator.getUUID(), timeTicks, requiredPresses, startTick,
                        prey.gameMode.getGameModeForPlayer()));

        applyConsumedState(prey);

        ModMessages.sendToPlayer(
                new PlayerConsumedS2CPacket(predator.getId(), timeTicks, requiredPresses),
                prey);

        ModMessages.sendToPlayer(new PredatorConsumeStateS2CPacket(true), predator);

        level.sendParticles(ParticleTypes.HEART,
                predator.getX(), predator.getY() + predator.getBbHeight() / 2, predator.getZ(),
                8, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, predator.blockPosition(),
                com.eisiadev.oversaturatedgrowth.init.ModSounds.SWALLOW.get(), SoundSource.PLAYERS,
                1.0f, 0.8f + (float) Math.random() * 0.4f);

        OversaturatedGrowth.LOGGER.info("[PlayerConsume] {} consumed {} (timeTicks={}, required={})",
                predator.getName().getString(), prey.getName().getString(), timeTicks, requiredPresses);
    }

    public static void startMobConsume(net.minecraft.world.entity.LivingEntity predator, ServerPlayer prey, ServerLevel level) {
        startMobConsume(predator, prey, level, 1.0);
    }

    public static void startMobConsume(net.minecraft.world.entity.LivingEntity predator, ServerPlayer prey, ServerLevel level, double pressFactor) {
        int timeSec = ModServerConfig.playerConsumeEscapeTimeSec.get();
        int timeTicks = (timeSec == -1) ? -1 : timeSec * 20;
        int requiredPresses = Math.max(1, (int) Math.round(ModServerConfig.playerConsumeEscapeKeyPresses.get() * pressFactor));
        var srv = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        long startTick = (srv != null) ? srv.getTickCount() : level.getGameTime();
        activeConsumeMap.put(prey.getUUID(),
                new ConsumeData(predator.getUUID(), timeTicks, requiredPresses, startTick,
                        prey.gameMode.getGameModeForPlayer()));
        applyConsumedState(prey);
        ModMessages.sendToPlayer(new PlayerConsumedS2CPacket(predator.getId(), timeTicks, requiredPresses), prey);
        level.playSound(null, predator.blockPosition(),
                com.eisiadev.oversaturatedgrowth.init.ModSounds.SWALLOW.get(), SoundSource.HOSTILE,
                1.0f, 0.7f + (float) Math.random() * 0.3f);
    }

    public static boolean isConsumed(UUID preyId) {
        return activeConsumeMap.containsKey(preyId);
    }

    public static void onEscapeKeyPress(ServerPlayer prey, int totalPresses) {
        ConsumeData data = activeConsumeMap.get(prey.getUUID());
        if (data == null) return;

        data.currentPresses = totalPresses;

        if (data.currentPresses >= data.requiredPresses) {
            escape(prey, data);
        }
    }

    private static void applyConsumedState(ServerPlayer prey) {
        prey.setGameMode(GameType.SPECTATOR);
    }

    private static void removeConsumedState(ServerPlayer prey, GameType restoreType) {
        prey.setGameMode(restoreType != null ? restoreType : GameType.SURVIVAL);
    }

    public static void tickServer(long currentTick, Iterable<ServerLevel> allLevels) {
        if (activeConsumeMap.isEmpty()) return;

        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        List<UUID> toProcess = new ArrayList<>(activeConsumeMap.keySet());

        for (UUID preyId : toProcess) {
            ConsumeData data = activeConsumeMap.get(preyId);
            if (data == null) continue;

            ServerPlayer prey = server.getPlayerList().getPlayer(preyId);
            if (prey == null) {
                activeConsumeMap.remove(preyId);
                continue;
            }

            if (data.timeLimitTicks < 0) continue;

            long elapsed = currentTick - data.startTick;
            OversaturatedGrowth.LOGGER.debug("[PlayerConsume] tick={} startTick={} elapsed={}/{}",
                    currentTick, data.startTick, elapsed, data.timeLimitTicks);

            if (elapsed >= data.timeLimitTicks) {
                OversaturatedGrowth.LOGGER.info("[PlayerConsume] timeout! consuming {}", prey.getName().getString());
                consumePlayer(prey, data, server);
            }
        }
    }

    private static void escape(ServerPlayer prey, ConsumeData data) {
        activeConsumeMap.remove(prey.getUUID());
        removeConsumedState(prey, data.savedGameType);

        var srv2 = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (srv2 != null) {
            ServerPlayer predator2 = srv2.getPlayerList().getPlayer(data.predatorId);
            if (predator2 != null) ModMessages.sendToPlayer(new PredatorConsumeStateS2CPacket(false), predator2);
        }

        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            net.minecraft.world.entity.Entity predator = server.getPlayerList().getPlayer(data.predatorId);
            if (predator == null && prey.level() instanceof ServerLevel pl) predator = pl.getEntity(data.predatorId);
            if (predator != null) {

                double ox = 0, oz = 0;
                if (!(predator instanceof ServerPlayer)) {
                    double yaw = Math.toRadians(predator.getYRot());
                    ox = -Math.sin(yaw) * 1.6; oz = Math.cos(yaw) * 1.6;
                }
                prey.teleportTo(
                        (ServerLevel) predator.level(),
                        predator.getX() + ox, predator.getY(), predator.getZ() + oz,
                        prey.getYRot(), prey.getXRot());

                if (predator.level() instanceof ServerLevel sLevel) {
                    sLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            predator.getX(), predator.getY() + predator.getBbHeight() * 0.8, predator.getZ(),
                            12, 0.4, 0.4, 0.4, 0.05);
                    sLevel.playSound(null, predator.blockPosition(),
                            com.eisiadev.oversaturatedgrowth.init.ModSounds.SWALLOW.get(), SoundSource.PLAYERS,
                            1.0f, 0.7f + (float) Math.random() * 0.3f);
                }
            }
        }

        ModMessages.sendToPlayer(new PlayerConsumeResultS2CPacket(true), prey);
        OversaturatedGrowth.LOGGER.info("[PlayerConsume] {} escaped!", prey.getName().getString());
    }

    private static void consumePlayer(ServerPlayer prey, ConsumeData data, net.minecraft.server.MinecraftServer server) {
        activeConsumeMap.remove(prey.getUUID());

        ModMessages.sendToPlayer(new PredatorConsumeStateS2CPacket(false),
                server.getPlayerList().getPlayer(data.predatorId));

        ModMessages.sendToPlayer(new PlayerConsumeResultS2CPacket(false), prey);
        removeConsumedState(prey, data.savedGameType);

        prey.setHealth(0.0f);
        prey.die(prey.level().damageSources().starve());

        ServerPlayer predator = server.getPlayerList().getPlayer(data.predatorId);
        if (predator == null) {
            if (prey.level() instanceof ServerLevel pl && pl.getEntity(data.predatorId) instanceof net.minecraft.world.entity.LivingEntity mob) {
                mob.heal(prey.getMaxHealth() / 2f);
            }
            return;
        }

        final ServerPlayer finalPredator = predator;
        int calPerHealth = BalanceHelper.scaleCal(ModServerConfig.consumeCalPerHealthPercent.get());
        int calories = Math.max(1, (int)(prey.getMaxHealth() * calPerHealth));
        final int finalCalories = calories;

        GgBridge.addCalories(finalPredator, finalCalories);

        if (finalPredator.level() instanceof ServerLevel sLevel) {
            sLevel.sendParticles(ParticleTypes.HEART,
                    finalPredator.getX(), finalPredator.getY() + finalPredator.getBbHeight() / 2, finalPredator.getZ(),
                    8, 0.3, 0.3, 0.3, 0.05);
            sLevel.playSound(null, finalPredator.blockPosition(),
                    com.eisiadev.oversaturatedgrowth.init.ModSounds.SWALLOW.get(), SoundSource.PLAYERS,
                    1.0f, 0.8f + (float) Math.random() * 0.4f);
        }

        MinecraftForge.EVENT_BUS.post(new EntityConsumedEvent(finalPredator, prey, finalCalories));

        OversaturatedGrowth.LOGGER.info("[PlayerConsume] {} was fully consumed by {} for {} cal",
                prey.getName().getString(), finalPredator.getName().getString(), finalCalories);
    }

    public static void forceCancelForPredator(UUID predatorId) {
        activeConsumeMap.entrySet().removeIf(e -> e.getValue().predatorId.equals(predatorId));
    }

    public static void releaseForPredator(UUID predatorId) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (var e : new ArrayList<>(activeConsumeMap.entrySet())) {
            if (!e.getValue().predatorId.equals(predatorId)) continue;
            ServerPlayer prey = server.getPlayerList().getPlayer(e.getKey());
            if (prey == null) { activeConsumeMap.remove(e.getKey()); continue; }
            escape(prey, e.getValue());
        }
    }
}
