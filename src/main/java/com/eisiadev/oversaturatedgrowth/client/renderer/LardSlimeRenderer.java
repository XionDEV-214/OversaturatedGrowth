package com.eisiadev.oversaturatedgrowth.client.renderer;

import com.eisiadev.oversaturatedgrowth.entity.LardSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LardSlimeRenderer extends MobRenderer<LardSlimeEntity, SlimeModel<LardSlimeEntity>> {

    private static final ResourceLocation SLIME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("oversaturatedgrowth", "textures/mobs/lard_slime.png");

    public LardSlimeRenderer(EntityRendererProvider.Context context) {
        super(context,
                new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)),
                0.25f);

        this.addLayer(new OuterLayer(this,
                new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME_OUTER))));
    }

    @Override
    protected void scale(LardSlimeEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999f, 0.999f, 0.999f);
        float size = entity.getSize();
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish)
                / (size * 0.5f + 1.0f);
        float xyScale = 1.0f / (squish + 1.0f);
        poseStack.scale(xyScale * size, 1.0f / xyScale * size, xyScale * size);
    }

    @Override
    public ResourceLocation getTextureLocation(LardSlimeEntity entity) {
        return SLIME_TEXTURE;
    }

    private static class OuterLayer extends RenderLayer<LardSlimeEntity, SlimeModel<LardSlimeEntity>> {

        private final SlimeModel<LardSlimeEntity> outerModel;

        public OuterLayer(MobRenderer<LardSlimeEntity, SlimeModel<LardSlimeEntity>> renderer,
                          SlimeModel<LardSlimeEntity> outerModel) {
            super(renderer);
            this.outerModel = outerModel;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                           LardSlimeEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isInvisible()) return;

            VertexConsumer consumer = bufferSource.getBuffer(
                    RenderType.entityTranslucent(SLIME_TEXTURE));
            this.outerModel.setupAnim(entity, limbSwing, limbSwingAmount,
                    ageInTicks, netHeadYaw, headPitch);
            this.outerModel.renderToBuffer(poseStack, consumer, packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
