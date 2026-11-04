package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.blockentity.LardExtractorBlockEntity;
import com.eisiadev.oversaturatedgrowth.client.BlockConsumeClient;
import com.eisiadev.oversaturatedgrowth.client.PlayerConsumedOverlay;
import com.eisiadev.oversaturatedgrowth.client.PredatorConsumeOverlay;
import com.eisiadev.oversaturatedgrowth.client.WeightGaugeOverlay;
import com.eisiadev.oversaturatedgrowth.init.ModKeybinds;
import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.HoldStateC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    private static boolean wasHolding = false;

    private static int effectTimer = 0;
    private static final Random RAND = new Random();

    private static LardExtractorBlockEntity focusedExtractor = null;

    @Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("weight_gauge",    WeightGaugeOverlay.WEIGHT_GAUGE);
            event.registerAboveAll("player_consumed", PlayerConsumedOverlay.PLAYER_CONSUMED);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        tickEntityConsume(mc);
        tickExtractorEffects(mc);
        PlayerConsumedOverlay.tickConsumed(mc);
        PredatorConsumeOverlay.tick();

        if (mc.screen != null) {
            focusedExtractor = null;
        } else {
            HitResult hit = mc.hitResult;
            if (hit instanceof BlockHitResult blockHit) {
                BlockEntity be = mc.level.getBlockEntity(blockHit.getBlockPos());
                focusedExtractor = (be instanceof LardExtractorBlockEntity e) ? e : null;
            } else {
                focusedExtractor = null;
            }
        }
    }

    private static void tickEntityConsume(Minecraft mc) {
        if (mc.screen != null) {
            if (wasHolding) { ModMessages.sendToServer(new HoldStateC2SPacket(false, -1)); wasHolding = false; }
            return;
        }
        boolean isHolding = ModKeybinds.CONSUME_ENTITY.isDown() && mc.options.keyUse.isDown();
        if (isHolding) {
            int targetId = -1;
            BlockPos blockTarget = null;
            HitResult hit = mc.hitResult;
            if (hit instanceof EntityHitResult entityHit) {
                Entity e = entityHit.getEntity();
                if (e instanceof LivingEntity && !(e instanceof Player && e == mc.player)) targetId = e.getId();
            } else if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK
                    && BlockConsumeClient.isConsumable(mc.level.getBlockState(blockHit.getBlockPos()))) {
                blockTarget = blockHit.getBlockPos();
            }
            if (targetId == -1 && blockTarget == null) {
                if (wasHolding) { ModMessages.sendToServer(new HoldStateC2SPacket(false, -1)); wasHolding = false; }
            } else {
                ModMessages.sendToServer(blockTarget != null
                        ? HoldStateC2SPacket.block(blockTarget)
                        : new HoldStateC2SPacket(true, targetId));
                wasHolding = true;
            }
        } else if (wasHolding) {
            ModMessages.sendToServer(new HoldStateC2SPacket(false, -1));
            wasHolding = false;
        }
    }

    private static void tickExtractorEffects(Minecraft mc) {
        effectTimer++;
        if (effectTimer % 8 != 0) return;

        Player player = mc.player;
        BlockPos playerPos = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-16, -16, -16),
                playerPos.offset( 16,  16,  16))) {
            BlockEntity be = mc.level.getBlockEntity(pos);
            if (!(be instanceof LardExtractorBlockEntity ext)) continue;
            if (!ext.isClientExtracting()) continue;

            double bx = pos.getX() + 0.5, by = pos.getY() + 0.5, bz = pos.getZ() + 0.5;
            int count = 2 + RAND.nextInt(3);
            for (int i = 0; i < count; i++) {
                mc.level.addParticle(ParticleTypes.FLAME,
                        bx + (RAND.nextDouble() - 0.5) * 0.8,
                        by + RAND.nextDouble() * 0.5,
                        bz + (RAND.nextDouble() - 0.5) * 0.8,
                        0, 0.02, 0);
            }
            if (effectTimer % 16 == 0) {
                mc.level.playLocalSound(bx, by, bz, SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS, 0.08f + RAND.nextFloat() * 0.04f,
                        1.8f + RAND.nextFloat() * 0.4f, false);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;

        LardExtractorBlockEntity extractor = focusedExtractor;
        if (extractor == null) return;

        Minecraft mc      = Minecraft.getInstance();
        GuiGraphics gfx   = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        final int PW = 200, PH = 52;
        final int ORIGIN_Y = sh / 2 + 14;

        int fluidAmt     = extractor.getClientFluidAmount();
        int capacity     = extractor.getClientCapacity();
        boolean extract  = extractor.isClientExtracting();
        boolean hasTank  = extractor.isClientHasTank();

        if (!hasTank) {
            Component warn = Component.translatable("message.oversaturatedgrowth.no_output_connected");
            int wx = (sw - mc.font.width(warn)) / 2;
            int wy = ORIGIN_Y + PH + 4;
            gfx.fill(wx - 4, wy - 2, wx + mc.font.width(warn) + 4, wy + 10, 0xAA000000);
            gfx.drawString(mc.font, warn, wx, wy, 0xFFAA44, false);
        }

        int px = (sw - PW) / 2, py = ORIGIN_Y;
        float ratio = capacity > 0 ? (float) fluidAmt / capacity : 0f;
        int pct = (int)(ratio * 100);

        gfx.fill(px-1, py-1, px+PW+1, py+PH+1, 0xAA000000);
        gfx.fill(px-1, py-1, px+PW+1, py,       0xFF555555);
        gfx.fill(px-1, py+PH, px+PW+1, py+PH+1, 0xFF555555);
        gfx.fill(px-1, py, px,       py+PH,      0xFF555555);
        gfx.fill(px+PW, py, px+PW+1, py+PH,      0xFF555555);

        gfx.drawString(mc.font,
                Component.translatable("block.oversaturatedgrowth.lard_extractor"),
                px+4, py+4, 0xAAAAAA, false);

        Component status = extract
                ? Component.translatable("message.oversaturatedgrowth.status_extracting")
                : Component.translatable("message.oversaturatedgrowth.status_idle");
        gfx.drawString(mc.font, status,
                px + PW - mc.font.width(status) - 4, py+4,
                extract ? 0xFF5555 : 0x777777, false);

        final int BX = px+4, BY = py+18, BW = PW-8, BH = 10;
        gfx.fill(BX, BY, BX+BW, BY+BH, 0xFF222222);
        if (ratio > 0) {
            int col = extract ? lerpColor(0xFFDD77, 0xFFAA00, ratio) : 0x556677;
            gfx.fill(BX, BY, BX+(int)(ratio*BW), BY+BH, 0xFF000000 | col);
        }
        gfx.fill(BX-1, BY-1, BX+BW+1, BY,      0xFF666666);
        gfx.fill(BX-1, BY+BH, BX+BW+1, BY+BH+1, 0xFF666666);
        gfx.fill(BX-1, BY-1, BX,        BY+BH+1, 0xFF666666);
        gfx.fill(BX+BW, BY-1, BX+BW+1,  BY+BH+1, 0xFF666666);

        gfx.drawString(mc.font,
                String.format("%,d / %,d mB  (%d%%)", fluidAmt, capacity, pct),
                px+4, py+32, 0xCCCCCC, false);
        String hint = "[RMB] Toggle";
        gfx.drawString(mc.font, hint, px+PW-mc.font.width(hint)-4, py+32, 0x555555, false);
    }

    private static int lerpColor(int from, int to, float t) {
        int fr=(from>>16)&0xFF, fg=(from>>8)&0xFF, fb=from&0xFF;
        int tr=(to>>16)  &0xFF, tg=(to>>8)  &0xFF, tb=to  &0xFF;
        return ((int)(fr+(tr-fr)*t)<<16)|((int)(fg+(tg-fg)*t)<<8)|(int)(fb+(tb-fb)*t);
    }
}
