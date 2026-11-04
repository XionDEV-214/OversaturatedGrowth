package com.eisiadev.oversaturatedgrowth.client.renderer;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.blockentity.LardExtractorBlockEntity;
import com.eisiadev.oversaturatedgrowth.init.ModBlockEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class LardExtractorRenderer implements BlockEntityRenderer<LardExtractorBlockEntity> {

    private static final float E = 0.04f / 16f;
    private static final float MIN = E, MAX = 1f - E;

    @Override
    public void render(LardExtractorBlockEntity be, float partial, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        FluidStack fs = be.getTank().getFluid();
        float target = fs.isEmpty() ? 0f : (float) fs.getAmount() / LardExtractorBlockEntity.TANK_CAPACITY;
        float fill = be.smoothFill(target);
        if (fill <= 0.002f || fs.isEmpty()) return;

        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fs.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(fs));
        int c = ext.getTintColor(fs);
        float a = (c >> 24 & 255) / 255f, r = (c >> 16 & 255) / 255f, g = (c >> 8 & 255) / 255f, b = (c & 255) / 255f;
        if (a <= 0f) a = 1f;

        float y0 = MIN;
        float y1 = MIN + (MAX - MIN) * Math.min(1f, fill);
        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();

        float u0 = sprite.getU(MIN * 16f), u1 = sprite.getU(MAX * 16f);
        float v0 = sprite.getV(MIN * 16f), v1 = sprite.getV(MAX * 16f);
        float sTop = sprite.getV(16f - y1 * 16f), sBot = sprite.getV(16f - y0 * 16f);

        quad(vc, m, n, MIN, y1, MIN, MIN, y1, MAX, MAX, y1, MAX, MAX, y1, MIN, u0, u1, v0, v1, 0, 1, 0, r * 1.0f, g * 1.0f, b * 1.0f, a, light);
        quad(vc, m, n, MIN, y0, MAX, MIN, y0, MIN, MAX, y0, MIN, MAX, y0, MAX, u0, u1, v0, v1, 0, -1, 0, r * 0.5f, g * 0.5f, b * 0.5f, a, light);

        quad(vc, m, n, MIN, y0, MIN, MIN, y1, MIN, MAX, y1, MIN, MAX, y0, MIN, u0, u1, sBot, sTop, 0, 0, -1, r * 0.8f, g * 0.8f, b * 0.8f, a, light);
        quad(vc, m, n, MAX, y0, MAX, MAX, y1, MAX, MIN, y1, MAX, MIN, y0, MAX, u0, u1, sBot, sTop, 0, 0, 1, r * 0.8f, g * 0.8f, b * 0.8f, a, light);
        quad(vc, m, n, MIN, y0, MAX, MIN, y1, MAX, MIN, y1, MIN, MIN, y0, MIN, u0, u1, sBot, sTop, -1, 0, 0, r * 0.6f, g * 0.6f, b * 0.6f, a, light);
        quad(vc, m, n, MAX, y0, MIN, MAX, y1, MIN, MAX, y1, MAX, MAX, y0, MAX, u0, u1, sBot, sTop, 1, 0, 0, r * 0.6f, g * 0.6f, b * 0.6f, a, light);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, Matrix3f n,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float u0, float u1, float va, float vb,
                             float nx, float ny, float nz, float r, float g, float b, float a, int light) {
        vc.vertex(m, x0, y0, z0).color(r, g, b, a).uv(u0, va).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, x1, y1, z1).color(r, g, b, a).uv(u0, vb).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, x2, y2, z2).color(r, g, b, a).uv(u1, vb).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, x3, y3, z3).color(r, g, b, a).uv(u1, va).uv2(light).normal(n, nx, ny, nz).endVertex();

        vc.vertex(m, x3, y3, z3).color(r, g, b, a).uv(u1, va).uv2(light).normal(n, -nx, -ny, -nz).endVertex();
        vc.vertex(m, x2, y2, z2).color(r, g, b, a).uv(u1, vb).uv2(light).normal(n, -nx, -ny, -nz).endVertex();
        vc.vertex(m, x1, y1, z1).color(r, g, b, a).uv(u0, vb).uv2(light).normal(n, -nx, -ny, -nz).endVertex();
        vc.vertex(m, x0, y0, z0).color(r, g, b, a).uv(u0, va).uv2(light).normal(n, -nx, -ny, -nz).endVertex();
    }

    @Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent
        public static void onRegister(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.LARD_EXTRACTOR.get(), ctx -> new LardExtractorRenderer());
        }
    }
}
