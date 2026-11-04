package com.eisiadev.oversaturatedgrowth.client.renderer;

import com.eisiadev.oversaturatedgrowth.entity.projectile.LardGlobEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import net.minecraft.world.item.ItemStack;

public class LardGlobRenderer extends EntityRenderer<LardGlobEntity> {

    private static final ItemStack LARD_SLUDGE_BALL = new ItemStack(ModItems.LARD_SLUDGE_BALL.get());

    public LardGlobRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LardGlobEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1f, 1f, 1f);
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));

        Minecraft.getInstance().getItemRenderer().renderStatic(
                LARD_SLUDGE_BALL,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LardGlobEntity entity) {
        return ResourceLocation.tryParse("oversaturatedgrowth:textures/item/lard_sludge_ball.png");
    }
}
