package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.GourmandKnightEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class KnightModel extends EntityModel<GourmandKnightEntity> {
    private final ModelPart root, head;
    private final ModelPart[] rightArms, leftArms, rightLegs, leftLegs;

    public KnightModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArms = new ModelPart[]{root.getChild("rightArm"), root.getChild("rightArm2"), root.getChild("rightArm3"), root.getChild("rightArm4")};
        this.leftArms = new ModelPart[]{root.getChild("leftArm"), root.getChild("leftArm2"), root.getChild("leftArm3"), root.getChild("leftArm4")};
        this.rightLegs = new ModelPart[]{root.getChild("rightLeg"), root.getChild("rightLeg2")};
        this.leftLegs = new ModelPart[]{root.getChild("leftLeg"), root.getChild("leftLeg2")};
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();
        pd.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4f, -8f, -4f, 8f, 8f, 8f),
                PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("body", CubeListBuilder.create().texOffs(25, 34).mirror().addBox(-5f, 10f, -3f, 10f, 2f, 6f),
                PartPose.offsetAndRotation(0f, 1f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(40, 3).mirror().addBox(-4f, -5f, -3f, 6f, 5f, 6f),
                PartPose.offsetAndRotation(-6f, 2f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(40, 3).mirror().addBox(-2f, -5f, -3f, 6f, 5f, 6f),
                PartPose.offsetAndRotation(6f, 2f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 34).mirror().addBox(-3f, 6f, -3f, 6f, 6f, 6f),
                PartPose.offsetAndRotation(-3f, 12f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 34).mirror().addBox(-3f, 6f, -3f, 6f, 6f, 6f),
                PartPose.offsetAndRotation(3f, 12f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("leftArm2", CubeListBuilder.create().texOffs(10, 47).mirror().addBox(5f, 2f, 0f, 3f, 12f, 0f),
                PartPose.offsetAndRotation(6f, 2f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("leftLeg2", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2f, 0f, -2f, 4f, 11f, 4f),
                PartPose.offsetAndRotation(3f, 13f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("rightLeg2", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2f, 0f, -2f, 4f, 11f, 4f),
                PartPose.offsetAndRotation(-3f, 13f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(16, 16).mirror().addBox(-4f, 0f, -2f, 8f, 12f, 4f),
                PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("rightArm2", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-3f, -2f, -2f, 4f, 12f, 4f),
                PartPose.offsetAndRotation(-6f, 2f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("leftArm3", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1f, -2f, -2f, 4f, 12f, 4f),
                PartPose.offsetAndRotation(6f, 2f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("rightArm3", CubeListBuilder.create().texOffs(28, 46).mirror().addBox(-5f, 2f, -2f, 2f, 12f, 4f),
                PartPose.offsetAndRotation(-6f, 2f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("rightArm4", CubeListBuilder.create().texOffs(2, 47).mirror().addBox(-8f, 2f, 0f, 3f, 12f, 0f),
                PartPose.offsetAndRotation(-6f, 2f, 0f, 0f, 0f, 0f));
        pd.addOrReplaceChild("leftArm4", CubeListBuilder.create().texOffs(41, 46).mirror().addBox(3f, 2f, -2f, 2f, 12f, 4f),
                PartPose.offsetAndRotation(6f, 2f, 0f, 0f, 0f, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(GourmandKnightEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float swingR = Mth.cos(limbSwing * 0.6662f + Mth.PI) * limbSwingAmount;
        float swingL = Mth.cos(limbSwing * 0.6662f) * limbSwingAmount;
        float rArm = swingR, lArm = swingL;
        if (e.isGrabbing()) { rArm = -1.45f; lArm = -1.45f; }
        else if (attackTime > 0f) rArm = -Mth.sin(attackTime * Mth.PI) * 1.6f;
        for (ModelPart p : rightArms) p.xRot = rArm;
        for (ModelPart p : leftArms) p.xRot = lArm;
        for (ModelPart p : rightLegs) p.xRot = Mth.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount;
        for (ModelPart p : leftLegs) p.xRot = Mth.cos(limbSwing * 0.6662f + Mth.PI) * 1.4f * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
