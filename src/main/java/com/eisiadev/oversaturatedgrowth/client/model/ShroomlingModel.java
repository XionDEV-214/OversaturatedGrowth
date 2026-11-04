package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.ShroomlingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class ShroomlingModel extends EntityModel<ShroomlingEntity> {
    private final ModelPart root;
    private final ModelPart cap, armL, armR, legL, legR;

    public ShroomlingModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.cap = root.getChild("cap");
        this.armL = root.getChild("arm_l");
        this.armR = root.getChild("arm_r");
        this.legL = root.getChild("leg_l");
        this.legR = root.getChild("leg_r");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();
        pd.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5, -4, -5, 10, 4, 10)
                .texOffs(0, 14).addBox(-3, -6, -3, 6, 2, 6), PartPose.offset(0, 14, 0));
        pd.addOrReplaceChild("stem", CubeListBuilder.create().texOffs(0, 22)
                .addBox(-2.5f, 0, -2.5f, 5, 6, 5), PartPose.offset(0, 14, 0));
        pd.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(24, 22)
                .addBox(0, 0, -1, 2, 5, 2), PartPose.offset(2.5f, 15, 0));
        pd.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(24, 22)
                .addBox(-2, 0, -1, 2, 5, 2), PartPose.offset(-2.5f, 15, 0));
        pd.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(36, 22)
                .addBox(-1, 0, -1, 2, 4, 2), PartPose.offset(1.5f, 20, 0));
        pd.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(36, 22)
                .addBox(-1, 0, -1, 2, 4, 2), PartPose.offset(-1.5f, 20, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(ShroomlingEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        cap.yRot = yaw * ((float) Math.PI / 180f) * 0.6f;
        cap.zRot = Mth.sin(age * 0.1f) * 0.03f + Mth.cos(limbSwing * 0.6662f) * 0.08f * limbSwingAmount;
        legR.xRot = Mth.cos(limbSwing * 0.6662f) * 1.2f * limbSwingAmount;
        legL.xRot = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * 1.2f * limbSwingAmount;
        armR.xRot = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * 0.8f * limbSwingAmount;
        armL.xRot = Mth.cos(limbSwing * 0.6662f) * 0.8f * limbSwingAmount;
        armR.zRot = 0.05f + Mth.cos(age * 0.09f) * 0.05f;
        armL.zRot = -0.05f - Mth.cos(age * 0.09f) * 0.05f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
