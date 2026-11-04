package com.eisiadev.oversaturatedgrowth.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ColorTintLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private final ResourceLocation texture;

    private final float r, g, b, a;

    public ColorTintLayer(LivingEntityRenderer<T, M> renderer,
                          ResourceLocation texture,
                          float r, float g, float b, float a) {
        super(renderer);
        this.texture = texture;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        if (entity.isInvisible()) return;

        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityTranslucentEmissive(texture));

        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight,
                OverlayTexture.NO_OVERLAY,
                r, g, b, a);
    }
}
