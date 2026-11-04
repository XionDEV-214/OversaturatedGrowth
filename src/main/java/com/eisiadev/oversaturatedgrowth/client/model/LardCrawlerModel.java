package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.LardCrawlerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class LardCrawlerModel extends EntityModel<LardCrawlerEntity> {
    private final ModelPart root;
    private final ModelPart[] leg = new ModelPart[9];

    public LardCrawlerModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        for (int i = 1; i <= 8; i++) leg[i] = root.getChild("leg" + i);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();
        pd.addOrReplaceChild("rear_end", CubeListBuilder.create().texOffs(0, 10).mirror(true)
                .addBox(-9f, -15f, -7f, 8, 8, 8),
                PartPose.offsetAndRotation(4f, 8f, 4f, 0f, 0f, 0f));
        pd.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(18, 0).mirror(true)
                .addBox(-1f, -1f, -1f, 16, 2, 2),
                PartPose.offsetAndRotation(6f, 16f, 2f, 0f, 0.2792527f, 0.1919862f));
        pd.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(18, 0).mirror(true)
                .addBox(-1f, -1f, -1f, 16, 2, 2),
                PartPose.offsetAndRotation(6f, 16f, 4f, 0f, -0.2792527f, 0.1919862f));
        pd.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(18, 0).mirror(true)
                .addBox(-1f, -1f, -1f, 16, 2, 2),
                PartPose.offsetAndRotation(6f, 16f, 6f, 0f, -0.5759587f, 0.1919862f));
        pd.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(18, 5).mirror(true)
                .addBox(-15f, -1f, -1f, 16, 2, 2),
                PartPose.offsetAndRotation(-6f, 16f, 2f, 0f, -0.2792527f, -0.1919862f));
        pd.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(18, 5).mirror(true)
                .addBox(-15f, -1f, -1f, 16, 2, 2),
                PartPose.offsetAndRotation(-6f, 16f, 4f, 0f, 0.2792527f, -0.1919862f));
        pd.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(18, 5).mirror(true)
                .addBox(-15f, -1f, -1f, 16, 2, 2),
                PartPose.offsetAndRotation(-6f, 16f, 6f, 0f, 0.5759587f, -0.1919862f));
        pd.addOrReplaceChild("leg8", CubeListBuilder.create().texOffs(18, 0).mirror(true)
                .addBox(-1f, -1f, -1f, 16, 2, 2),
                PartPose.offsetAndRotation(6f, 16f, -1f, 0f, 0.5759587f, 0.1919862f));
        pd.addOrReplaceChild("leg7", CubeListBuilder.create().texOffs(18, 5).mirror(true)
                .addBox(-15f, -1f, -1f, 16, 2, 2),
                PartPose.offsetAndRotation(-6f, 16f, -1f, 0f, -0.5759587f, -0.1919862f));
        pd.addOrReplaceChild("rear_end2", CubeListBuilder.create().texOffs(70, 6).mirror(true)
                .addBox(-8f, -6f, -6f, 14, 12, 14),
                PartPose.offsetAndRotation(0f, 12f, 1f, 0f, 0f, 0f));
        pd.addOrReplaceChild("rear_end3", CubeListBuilder.create().texOffs(35, 10).mirror(true)
                .addBox(-7f, -7f, -5f, 4, 5, 4),
                PartPose.offsetAndRotation(4f, 8f, 4f, 0f, 0f, 0f));
        return LayerDefinition.create(mesh, 128, 32);
    }

    @Override
    public void setupAnim(LardCrawlerEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        final float var8 = 0.7853982f;
        leg[1].zRot = -var8;
        leg[2].zRot = var8;
        leg[3].zRot = -var8 * 0.74f;
        leg[4].zRot = var8 * 0.74f;
        leg[5].zRot = -var8 * 0.74f;
        leg[6].zRot = var8 * 0.74f;
        leg[7].zRot = -var8;
        leg[8].zRot = var8;
        final float var10 = 0.3926991f;
        leg[1].yRot = var10 * 2.0f;
        leg[2].yRot = -var10 * 2.0f;
        leg[3].yRot = var10;
        leg[4].yRot = -var10;
        leg[5].yRot = -var10;
        leg[6].yRot = var10;
        leg[7].yRot = -var10 * 2.0f;
        leg[8].yRot = var10 * 2.0f;
        final float var11 = -(Mth.cos(limbSwing * 0.6662f * 2.0f) * 0.4f) * limbSwingAmount;
        final float var12 = -(Mth.cos(limbSwing * 0.6662f * 2.0f + 3.1415927f) * 0.4f) * limbSwingAmount;
        final float var13 = -(Mth.cos(limbSwing * 0.6662f * 2.0f + 1.5707964f) * 0.4f) * limbSwingAmount;
        final float var14 = -(Mth.cos(limbSwing * 0.6662f * 2.0f + 4.712389f) * 0.4f) * limbSwingAmount;
        final float var15 = Math.abs(Mth.sin(limbSwing * 0.6662f) * 0.4f) * limbSwingAmount;
        final float var16 = Math.abs(Mth.sin(limbSwing * 0.6662f + 3.1415927f) * 0.4f) * limbSwingAmount;
        final float var17 = Math.abs(Mth.sin(limbSwing * 0.6662f + 1.5707964f) * 0.4f) * limbSwingAmount;
        final float var18 = Math.abs(Mth.sin(limbSwing * 0.6662f + 4.712389f) * 0.4f) * limbSwingAmount;
        leg[1].yRot += var11;  leg[2].yRot += -var11;
        leg[3].yRot += var12;  leg[4].yRot += -var12;
        leg[5].yRot += var13;  leg[6].yRot += -var13;
        leg[7].yRot += var14;  leg[8].yRot += -var14;
        leg[1].zRot += var15;  leg[2].zRot += -var15;
        leg[3].zRot += var16;  leg[4].zRot += -var16;
        leg[5].zRot += var17;  leg[6].zRot += -var17;
        leg[7].zRot += var18;  leg[8].zRot += -var18;
        if (e.isAttached()) {

            float w = Mth.sin(age * 0.9f) * 0.25f;
            for (int i = 1; i <= 8; i++) leg[i].yRot += (i % 2 == 0 ? w : -w);
        }
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
