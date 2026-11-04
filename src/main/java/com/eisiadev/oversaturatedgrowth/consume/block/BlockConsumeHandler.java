package com.eisiadev.oversaturatedgrowth.consume.block;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.BlockConsumeSyncS2CPacket;
import com.eisiadev.oversaturatedgrowth.networking.packet.BlockConsumeVisualS2CPacket;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID)
public final class BlockConsumeHandler {

    private static final int CANCEL_TIMEOUT_TICKS = 3;
    private static final double MAX_REACH_SQ = 7.0 * 7.0;

    private static final class Hold {
        final ServerLevel level;
        final BlockPos pos;
        final long start;
        long last;
        int lastStage = -1;

        Hold(ServerLevel level, BlockPos pos, long start) {
            this.level = level;
            this.pos = pos;
            this.start = start;
            this.last = start;
        }
    }

    private static final Map<UUID, Hold> HOLDS = new HashMap<>();

    private BlockConsumeHandler() {}

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(BlockConsumeData.INSTANCE);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        BlockConsumeSyncS2CPacket packet = new BlockConsumeSyncS2CPacket(
                BlockConsumeData.INSTANCE.blockIds(), BlockConsumeData.INSTANCE.tagIds());
        if (event.getPlayer() != null) {
            ModMessages.sendToPlayer(packet, event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(p -> ModMessages.sendToPlayer(packet, p));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof CakeBlock)) return;
        if (event.getItemStack().is(ItemTags.CANDLES)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    public static void onHold(ServerPlayer player, BlockPos pos) {
        if (player.hasEffect(com.eisiadev.oversaturatedgrowth.init.ModEffects.OVEREAT.get())) { cancel(player); return; }
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(pos);
        BlockConsumeEntry entry = BlockConsumeData.INSTANCE.find(state);
        if (entry == null || player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_REACH_SQ) {
            cancel(player);
            return;
        }

        long now = level.getGameTime();
        Hold hold = HOLDS.get(player.getUUID());
        if (hold != null && !hold.pos.equals(pos)) {
            clearProgress(player, hold);
            hold = null;
        }
        if (hold == null) {
            hold = new Hold(level, pos, now);
            HOLDS.put(player.getUUID(), hold);
        }
        hold.last = now;
        long elapsed = now - hold.start;

        if (elapsed % 4 == 0) {
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8f, 1.0f + Math.min(1.0f, elapsed / 40.0f));
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.25, 0.25, 0.25, 0.05);

        int stage = (int) Math.min(9, elapsed * 10 / entry.holdTicks());
        if (stage != hold.lastStage) {
            hold.lastStage = stage;
            level.destroyBlockProgress(player.getId(), pos, stage);
        }

        if (elapsed >= entry.holdTicks()) {
            HOLDS.remove(player.getUUID());
            level.destroyBlockProgress(player.getId(), pos, -1);
            complete(player, level, pos, state, entry);
        }
    }

    public static void cancel(ServerPlayer player) {
        Hold hold = HOLDS.remove(player.getUUID());
        if (hold != null) clearProgress(player, hold);
    }

    private static void clearProgress(ServerPlayer player, Hold hold) {
        hold.level.destroyBlockProgress(player.getId(), hold.pos, -1);
    }

    public static void tickServer(long currentTick, Iterable<ServerLevel> allLevels) {
        if (HOLDS.isEmpty()) return;
        List<UUID> expired = new ArrayList<>();
        for (Map.Entry<UUID, Hold> e : HOLDS.entrySet()) {
            long gameTime = e.getValue().level.getGameTime();
            if (gameTime - e.getValue().last > CANCEL_TIMEOUT_TICKS) expired.add(e.getKey());
        }
        for (UUID id : expired) {
            Hold hold = HOLDS.remove(id);
            if (hold == null) continue;
            var p = hold.level.getServer().getPlayerList().getPlayer(id);
            hold.level.destroyBlockProgress(p != null ? p.getId() : id.hashCode(), hold.pos, -1);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) cancel(sp);
    }

    private static void complete(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, BlockConsumeEntry entry) {

        if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player))) return;

        boolean wasFull = GgBridge.isCalorieFull(player);
        if (entry.caloriesPercent() > 0) GgBridge.addCalories(player, BalanceHelper.scaleCal(entry.caloriesPercent()));
        if (entry.weightPercent() > 0) GgBridge.addWeight(player, BalanceHelper.scaleWeight(entry.weightPercent()));
        for (MobEffectInstance e : entry.effects()) player.addEffect(new MobEffectInstance(e));
        if (wasFull) com.eisiadev.oversaturatedgrowth.effect.EffectStacking.rollOvereatOnEatWhenFull(player);

        int pieces = 8;
        if (entry.bite() && state.hasProperty(BlockStateProperties.BITES)) {
            int bites = state.getValue(BlockStateProperties.BITES);
            if (bites < 6) level.setBlock(pos, state.setValue(BlockStateProperties.BITES, bites + 1), Block.UPDATE_ALL);
            else level.removeBlock(pos, false);
            pieces = 2;
        } else {
            level.removeBlock(pos, false);
        }

        ModMessages.sendToNearby(new BlockConsumeVisualS2CPacket(pos, Block.getId(state), player.getId(), pieces),
                level, Vec3.atCenterOf(pos), 48);
    }
}
