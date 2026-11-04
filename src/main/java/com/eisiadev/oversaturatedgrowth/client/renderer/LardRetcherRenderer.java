package com.eisiadev.oversaturatedgrowth.client.renderer;

import com.eisiadev.oversaturatedgrowth.entity.LardRetcherEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class LardRetcherRenderer extends HumanoidMobRenderer<LardRetcherEntity, HumanoidModel<LardRetcherEntity>> {

    private static final ResourceLocation HUSK_TEXTURE =
            ResourceLocation.tryParse("minecraft:textures/entity/zombie/husk.png");

    public LardRetcherRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        this.addLayer(new ColorTintLayer<>(this, HUSK_TEXTURE, 1.0f, 0.82f, 0.25f, 0.55f));
    }

    @Override
    public ResourceLocation getTextureLocation(LardRetcherEntity entity) {
        return HUSK_TEXTURE;
    }
}
