package com.eisiadev.oversaturatedgrowth.client;

import com.eisiadev.oversaturatedgrowth.networking.ModMessages;
import com.eisiadev.oversaturatedgrowth.networking.packet.EscapeKeyPressC2SPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.willsbr.gluttonousgrowth.client.ClientCalorieMeter;

public class PlayerConsumedOverlay {

    public static final IGuiOverlay PLAYER_CONSUMED = PlayerConsumedOverlay::render;

    private static boolean active = false;
    private static int predatorEntityId = -1;
    private static int timeLimitTicks = -1;
    private static int requiredPresses = 50;
    private static int remainingTicks = 0;
    private static int currentPresses = 0;

    private static boolean wasW = false, wasA = false, wasS = false, wasD = false;

    private static CameraType savedCameraType = null;

    public static void startConsumed(int predatorId, int timeTicks, int required) {
        active = true;
        predatorEntityId = predatorId;
        timeLimitTicks = timeTicks;
        requiredPresses = required;
        remainingTicks = timeTicks;
        currentPresses = 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        savedCameraType = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        camera = new net.minecraft.world.entity.decoration.ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, mc.level);
        camera.setInvisible(true);
        camera.setNoGravity(true);
        camera.noPhysics = true;
        updateCamera(mc, true);
        mc.setCameraEntity(camera);
    }

    private static net.minecraft.world.entity.decoration.ArmorStand camera;

    private static final double CAM_DIST = 5.5, CAM_HEIGHT = 5.5, CAM_AZIMUTH = 45.0;

    private static void updateCamera(Minecraft mc, boolean snap) {
        if (camera == null || mc.level == null) return;
        Entity pred = mc.level.getEntity(predatorEntityId);
        if (pred == null) return;
        double tx = pred.getX(), ty = pred.getY() + pred.getBbHeight() * 0.5, tz = pred.getZ();
        double az = Math.toRadians(CAM_AZIMUTH);
        double cx = tx + Math.sin(az) * CAM_DIST, cz = tz + Math.cos(az) * CAM_DIST, cy = ty + CAM_HEIGHT;
        double vx = tx - cx, vy = ty - cy, vz = tz - cz;
        float yaw = (float) Math.toDegrees(Math.atan2(-vx, vz));
        float pitch = (float) -Math.toDegrees(Math.atan2(vy, Math.sqrt(vx * vx + vz * vz)));
        double eye = camera.getEyeHeight();
        if (!snap) camera.setOldPosAndRot();
        camera.moveTo(cx, cy - eye, cz, yaw, pitch);
        camera.setYHeadRot(yaw);
        if (snap) camera.setOldPosAndRot();
    }

    public static void end(boolean escaped) {
        active = false;
        predatorEntityId = -1;
        currentPresses = 0;

        camera = null;
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
        }

        if (savedCameraType != null) {
            mc.options.setCameraType(savedCameraType);
            savedCameraType = null;
        }
    }

    public static boolean isActive() { return active; }

    public static void tickConsumed(Minecraft mc) {
        if (!active) return;
        if (mc.player == null || mc.level == null) { end(false); return; }

        if (timeLimitTicks >= 0) {
            remainingTicks = Math.max(0, remainingTicks - 1);
        }

        if (camera != null) {
            updateCamera(mc, false);
            if (mc.getCameraEntity() != camera) mc.setCameraEntity(camera);
        }

        boolean nowW = mc.options.keyUp.isDown();
        boolean nowA = mc.options.keyLeft.isDown();
        boolean nowS = mc.options.keyDown.isDown();
        boolean nowD = mc.options.keyRight.isDown();

        int newPresses = 0;
        if (nowW && !wasW) newPresses++;
        if (nowA && !wasA) newPresses++;
        if (nowS && !wasS) newPresses++;
        if (nowD && !wasD) newPresses++;

        wasW = nowW; wasA = nowA; wasS = nowS; wasD = nowD;

        if (newPresses > 0) {
            currentPresses = Math.min(currentPresses + newPresses, requiredPresses);

            ModMessages.sendToServer(new EscapeKeyPressC2SPacket(currentPresses));
        }
    }

    private static void render(ForgeGui gui, GuiGraphics gfx, float partialTick,
                                int sw, int sh) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        PoseStack pose = gfx.pose();
        pose.pushPose();

        final int PW = 240;
        final int PH = 82;
        final int px = (sw - PW) / 2;
        final int py = 8;

        gfx.fill(px - 1, py - 1, px + PW + 1, py + PH + 1, 0xCC000000);
        drawBorder(gfx, px, py, PW, PH);

        Component title = Component.translatable("hud.oversaturatedgrowth.consumed_title");
        int tw = mc.font.width(title);
        gfx.drawString(mc.font, title, px + (PW - tw) / 2, py + 5, 0xFF5555, false);

        Component hint = Component.translatable("hud.oversaturatedgrowth.consumed_hint");
        int hw = mc.font.width(hint);
        gfx.drawString(mc.font, hint, px + (PW - hw) / 2, py + 16, 0xAAAAAA, false);

        float escapePct = requiredPresses > 0 ? (float) currentPresses / requiredPresses : 0f;
        int escapeLabel = (int)(escapePct * 100);
        Component escapeText = Component.translatable("hud.oversaturatedgrowth.consumed_escape",
                currentPresses, requiredPresses);
        gfx.drawString(mc.font, escapeText, px + 6, py + 30, 0x55FF55, false);
        drawGauge(gfx, px + 6, py + 40, PW - 12, 10, escapePct, 0x55FF55, 0x115511);

        if (timeLimitTicks < 0) {
            Component unlim = Component.translatable("hud.oversaturatedgrowth.consumed_unlimited");
            int uw = mc.font.width(unlim);
            gfx.drawString(mc.font, unlim, px + (PW - uw) / 2, py + 60, 0xFF9900, false);
        } else {
            float timePct = timeLimitTicks > 0 ? (float) remainingTicks / timeLimitTicks : 0f;
            int secLeft = (remainingTicks + 19) / 20;
            Component timeText = Component.translatable("hud.oversaturatedgrowth.consumed_time", secLeft);
            gfx.drawString(mc.font, timeText, px + 6, py + 60, 0xFF5555, false);
            drawGauge(gfx, px + 6, py + 72, PW - 12, 6, timePct, 0xFF4444, 0x551111);
        }

        pose.popPose();
    }

    private static void drawGauge(GuiGraphics gfx, int x, int y, int w, int h,
                                   float pct, int fillColor, int bgColor) {
        gfx.fill(x, y, x + w, y + h, 0xFF222222);
        if (pct > 0) {
            int filled = (int)(pct * w);
            gfx.fill(x, y, x + filled, y + h, 0xFF000000 | fillColor);
        }

        gfx.fill(x - 1, y - 1, x + w + 1, y, 0xFF555555);
        gfx.fill(x - 1, y + h, x + w + 1, y + h + 1, 0xFF555555);
        gfx.fill(x - 1, y - 1, x, y + h + 1, 0xFF555555);
        gfx.fill(x + w, y - 1, x + w + 1, y + h + 1, 0xFF555555);
    }

    private static void drawBorder(GuiGraphics gfx, int px, int py, int pw, int ph) {
        gfx.fill(px - 1, py - 1, px + pw + 1, py,       0xFF555555);
        gfx.fill(px - 1, py + ph, px + pw + 1, py + ph + 1, 0xFF555555);
        gfx.fill(px - 1, py, px, py + ph,              0xFF555555);
        gfx.fill(px + pw, py, px + pw + 1, py + ph,    0xFF555555);
    }
}
