package com.eisiadev.oversaturatedgrowth.client;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.effect.InflationHelper;
import com.eisiadev.oversaturatedgrowth.init.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class InflationClient {
    private InflationClient() {}

    @Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
            float s = InflationHelper.scale(event.getEntity());
            if (s > 1.001f) event.getPoseStack().scale(s, s, s);

            tinted = false;
            var p = event.getEntity();
            if (p.isInvisible()) return;
            float a = 0.06f * Math.min(InflationHelper.MAX_LEVEL, InflationHelper.amount(p));
            if (a <= 0.003f) return;
            flush(event.getMultiBufferSource());
            RenderSystem.setShaderColor(1.0f - a * 1.6f, 1.0f - a * 2.2f, 1.0f, 1.0f);
            tinted = true;
        }

        @SubscribeEvent
        public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
            if (!tinted) return;
            flush(event.getMultiBufferSource());
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            tinted = false;
        }

        private static boolean tinted;

        private static void flush(MultiBufferSource buf) {
            if (buf instanceof MultiBufferSource.BufferSource bs) bs.endBatch();
        }

        @SubscribeEvent
        public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
            var player = Minecraft.getInstance().player;
            if (player == null) return;
            MobEffectInstance e = player.getEffect(ModEffects.OVEREAT.get());
            if (e == null) return;
            int phase = e.getDuration() % 100;
            if (phase >= 10 && phase < 90) return;
            int t = phase >= 90 ? phase - 90 : phase + 10;
            float amp = (float) Math.sin(t * Math.PI / 19.0) * 3.0f;
            event.setRoll(event.getRoll() + (float) Math.sin(t * 1.7) * amp);
        }
    }
}
