package com.eisiadev.oversaturatedgrowth.client;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.config.ModClientConfig;
import com.eisiadev.oversaturatedgrowth.effect.InflationHelper;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.willsbr.gluttonousgrowth.client.ClientCalorieMeter;
import net.willsbr.gluttonousgrowth.client.ClientWeightBarData;
import net.willsbr.gluttonousgrowth.config.GluttonousClientConfig;

/**
 * Oversaturated Growth HUD (replaces Gluttonous Growth's stomach / belly icons), docked to the bottom-right corner.
 * <pre>
 *  [belly] [ weight: progress in the stage | queued ][NN] [ berry ]   NN = weight stage (pixel digits)
 *  [stomach][ calories current / max                              ]   [  lvl  ]
 *           [ 1px: time until calories start turning into weight  ]
 *           [ 1px: progress to the next stomach capacity upgrade  ]
 *  [ inflation: progress inside the current level (number = completed levels)  ]
 * </pre>
 * Everything is drawn from {@code textures/gui/og_hud.png} (256x64): the empty panel at (0,0) 110x35 and one fill strip
 * per bar, cut to length; pixel digits 1..9,0 (3x5 each, 4 px apart) at (0,35).
 */
public class WeightGaugeOverlay {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "textures/gui/og_hud.png");
    private static final ResourceLocation GG_HUD =
            ResourceLocation.fromNamespaceAndPath("gluttonousgrowth", "stuffedbar");

    private static final int TEX_W = 256, TEX_H = 64;
    public static final int W = 110, H = 35;

    private static final int BAR_X = 13, BAR_W = 69, WEIGHT_W = 61;
    private static final int STAGE_X = 74, STAGE_Y = 5, STAGE_W = 9;
    private static final int INFL_X = 3, INFL_Y = 28, INFL_W = 104;

    private static ItemStack berry;

    public static final IGuiOverlay WEIGHT_GAUGE = WeightGaugeOverlay::render;

    private static void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || player.isSpectator() || !ModClientConfig.hudEnabled.get()) return;

        int x = screenWidth - W - ModClientConfig.weightGaugeX.get();
        int y = screenHeight - H - ModClientConfig.weightGaugeY.get();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(TEX, x, y, 0, 0, W, H, TEX_W, TEX_H);

        // ---- weight (progress inside the current stage) + queued weight
        float[] weight = weightStage();
        int ww = px(weight[0], WEIGHT_W);
        g.blit(TEX, x + BAR_X, y + 4, 112, 0, ww, 7, TEX_W, TEX_H);
        int qw = Math.min(WEIGHT_W - ww, px(weight[1], WEIGHT_W));
        if (qw > 0) g.blit(TEX, x + BAR_X + ww, y + 4, 112 + ww, 8, qw, 7, TEX_W, TEX_H);

        drawNumber(g, (int) weight[2], x + STAGE_X, y + STAGE_Y, STAGE_W);

        // ---- calories
        int cur = ClientCalorieMeter.getCurrentCalories();
        int max = ClientCalorieMeter.getMax();
        g.blit(TEX, x + BAR_X, y + 14, 112, 16, px(max > 0 ? (float) cur / max : 0f, BAR_W), 7, TEX_W, TEX_H);

        // ---- 1px: calorie clear timer (drains to 0), stomach growth progress
        g.blit(TEX, x + BAR_X, y + 22, 112, 24, px(clearTimer(player), BAR_W), 1, TEX_W, TEX_H);
        int interval = ClientCalorieMeter.getInterval();
        float growth = interval > 0 ? (float) ClientCalorieMeter.getCurrentLost() / interval : 0f;
        g.blit(TEX, x + BAR_X, y + 24, 112, 26, px(growth, BAR_W), 1, TEX_W, TEX_H);

        // ---- icons
        g.blit(TEX, x + 3, y + 4, 184, 0, 7, 7, TEX_W, TEX_H);
        g.blit(TEX, x + 3, y + 14, 192, 0, 7, 7, TEX_W, TEX_H);

        // ---- inflation: number = completed levels, bar = progress inside the next one (full at the cap)
        float amount = Math.max(0f, Math.min(InflationHelper.MAX_AMOUNT, InflationHelper.amount(player)));
        int levels = (int) Math.floor(amount + 1.0E-4f);
        boolean capped = amount >= InflationHelper.MAX_AMOUNT - 1.0E-4f;
        float within = capped ? 1f : amount - levels;
        g.blit(TEX, x + INFL_X, y + INFL_Y, 112, 28, px(within, INFL_W), 4, TEX_W, TEX_H);

        if (berry == null) berry = new ItemStack(ModItems.FATTABERRY.get());
        g.renderItem(berry, x + 89, y + 5);
        if (levels > 0) {
            Font font = mc.font;
            String s = String.valueOf(levels);
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            g.drawString(font, s, x + 107 - font.width(s), y + 14, 0xFFFFFF, true);
            g.pose().popPose();
        }
        RenderSystem.disableBlend();
    }

    /** 1-2 pixel digits centred in a {@code width} wide box (99 at most). */
    private static void drawNumber(GuiGraphics g, int n, int boxX, int y, int width) {
        if (n <= 0) return;
        String s = String.valueOf(Math.min(99, n));
        int textW = s.length() * 4 - 1;
        int cx = boxX + (width - textW + 1) / 2;
        for (int i = 0; i < s.length(); i++) {
            int d = s.charAt(i) - '0';
            int glyph = d == 0 ? 9 : d - 1;
            g.blit(TEX, cx + i * 4, y, glyph * 4, 35, 3, 5, TEX_W, TEX_H);
        }
    }

    private static int px(float f, int width) {
        return Math.round(Math.max(0f, Math.min(1f, f)) * width);
    }

    /** [progress inside the current weight stage, queued weight as a share of one stage, stage number (1-based)]. */
    private static float[] weightStage() {
        int current = ClientWeightBarData.getPlayerWeight();
        int min = GluttonousClientConfig.getMinWeight();
        int max = GluttonousClientConfig.getMaxWeight();
        int stages = GluttonousClientConfig.totalStages.get();
        if (max <= min || stages <= 0) return new float[] {0f, 0f, 0f};
        int stageWidth = Math.max(1, (max - min) / stages);
        int offset = Math.max(0, current - min);
        int stage = Math.min(offset / stageWidth, stages - 1);
        int stageMin = min + stage * stageWidth;
        int stageMax = stage == stages - 1 ? max : stageMin + stageWidth;
        float span = Math.max(1, stageMax - stageMin);
        return new float[] {(current - stageMin) / span, ClientWeightBarData.getQueuedWeight() / span, stage + 1};
    }

    /** 1 right after eating, draining to 0 when the calories start turning into weight. */
    private static float clearTimer(LocalPlayer player) {
        long saved = ClientCalorieMeter.getCurrentSavedTick();
        int delay = ClientCalorieMeter.getCurrentDelay();
        if (saved == -1 || delay <= 0) return 0f;
        long left = delay - (player.tickCount - saved);
        return (float) left / delay;
    }

    /** Hides Gluttonous Growth's own HUD (its debug view still shows while GG's debug option is on). */
    @Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class HideGluttonousHud {

        private HideGluttonousHud() {}

        @SubscribeEvent
        public static void onOverlay(RenderGuiOverlayEvent.Pre event) {
            if (!GG_HUD.equals(event.getOverlay().id())) return;
            if (!ModClientConfig.hudEnabled.get() || !ModClientConfig.hideGluttonousHud.get()) return;
            if (GluttonousClientConfig.debugView.get()) return;
            event.setCanceled(true);
        }
    }
}
