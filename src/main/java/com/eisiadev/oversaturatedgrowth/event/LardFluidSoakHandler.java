package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.config.ModServerConfig;
import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.util.BalanceHelper;
import com.eisiadev.oversaturatedgrowth.init.ModFluids;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.TickEvent;
import com.eisiadev.oversaturatedgrowth.event.api.LardNeedsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeterProvider;
import net.willsbr.gluttonousgrowth.WeightSystem.PlayerWeightBarProvider;
import net.willsbr.gluttonousgrowth.networking.ModMessages;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.CalorieMeterDelaySyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.StuffedPackets.OverfullFoodDataSyncPacketS2C;
import net.willsbr.gluttonousgrowth.networking.packet.WeightPackets.WeightBarDataSyncPacketS2C;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LardFluidSoakHandler {

    private static final int TICK_INTERVAL  = 5;
    private static final int HUNGER_RESTORE = 1;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % TICK_INTERVAL != 0) return;
        if (!player.isInFluidType(ModFluids.REFINED_LARD_TYPE.get())) return;

        if (player.tickCount % 20 == 0) {
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.HONEY_DRINK, SoundSource.PLAYERS,
                    1.0f, 0.8f + player.level().random.nextFloat() * 0.4f);
        }

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                com.eisiadev.oversaturatedgrowth.init.ModEffects.OVERSATURATED.get(), 100, 0, false, true, true));

        FoodData food = player.getFoodData();
        int hunger = food.getFoodLevel();
        if (hunger < 20) {
            food.setFoodLevel(Math.min(hunger + HUNGER_RESTORE, 20));
            return;
        }

        if (!GgBridge.isCalorieFull(player)) {
            int calories = BalanceHelper.scaleCal(ModServerConfig.fluidCalPercent.get());
            if (GgBridge.addCalories(player, calories)) {
                MinecraftForge.EVENT_BUS.post(new LardNeedsEvent(player, calories));
            }
            return;
        }
        GgBridge.addWeight(player, BalanceHelper.scaleWeight(ModServerConfig.fluidWeightPercent.get()));
    }
}
