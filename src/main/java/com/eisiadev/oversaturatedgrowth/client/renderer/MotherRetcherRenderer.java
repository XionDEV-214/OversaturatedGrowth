package com.eisiadev.oversaturatedgrowth.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.eisiadev.oversaturatedgrowth.entity.MotherRetcherEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MotherRetcherRenderer extends HumanoidMobRenderer<MotherRetcherEntity, HumanoidModel<MotherRetcherEntity>> {

    private static final ResourceLocation HUSK_TEXTURE =
            ResourceLocation.tryParse("minecraft:textures/entity/zombie/husk.png");

    public MotherRetcherRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 1.0f);
        this.addLayer(new ColorTintLayer<>(this, HUSK_TEXTURE, 1.0f, 0.82f, 0.25f, 0.55f));
    }

    @Override
    public ResourceLocation getTextureLocation(MotherRetcherEntity entity) {
        return HUSK_TEXTURE;
    }

    @Override
    protected void scale(MotherRetcherEntity entity, PoseStack poseStack, float partialTick) {
        float k = MotherRetcherEntity.MODEL_SCALE;
        poseStack.scale(k, k, k);
    }
}
