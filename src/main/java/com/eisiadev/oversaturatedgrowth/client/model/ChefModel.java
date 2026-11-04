package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.ChefEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ChefModel extends EntityModel<ChefEntity> {
    private static final String[] NAMES = {"head", "body", "rightArm", "leftArm", "rightLeg", "leftLeg", "r1", "head2", "head3", "head4", "head5", "head6", "head7", "body2", "body3", "body4", "body5", "rightArm2", "rightArm3", "rightArm4"};
    private final ModelPart root;
    private final ModelPart rightLeg, leftLeg, leftArm, r1, rightArm, rightArm2, rightArm3, rightArm4;

    public ChefModel(ModelPart root) {
        this.root = root;
        this.rightLeg = root.getChild("rightLeg");
        this.leftLeg = root.getChild("leftLeg");
        this.leftArm = root.getChild("leftArm");
        this.r1 = root.getChild("r1");
        this.rightArm = root.getChild("rightArm");
        this.rightArm2 = root.getChild("rightArm2");
        this.rightArm3 = root.getChild("rightArm3");
        this.rightArm4 = root.getChild("rightArm4");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();
        pd.addOrReplaceChild("head", CubeListBuilder.create().texOffs(46, 3).addBox(4.0F, -13.0F, 1.0F, 1F, 4F, 2F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 37).addBox(-0.5F, 6.0F, -2.0F, 1F, 4F, 4F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(38, 42).addBox(-3.0F, -14.0F, -7.0F, 4F, 5F, 2F),
                PartPose.offsetAndRotation(-5.0F, -3.0F, 0.0F, 0.3490659F, 0.0F, 0.0F));
        pd.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4F, 16F, 4F),
                PartPose.offsetAndRotation(5.0F, -3.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4F, 12F, 4F),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4F, 12F, 4F),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("r1", CubeListBuilder.create().texOffs(52, 8).addBox(-2.0F, -14.0F, -1.0F, 4F, 6F, 2F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(46, 3).addBox(-5.0F, -13.0F, 1.0F, 1F, 4F, 2F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("head3", CubeListBuilder.create().texOffs(26, 3).addBox(4.0F, -9.0F, -4.0F, 1F, 4F, 8F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("head4", CubeListBuilder.create().texOffs(26, 3).addBox(-5.0F, -9.0F, -4.0F, 1F, 4F, 8F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("head5", CubeListBuilder.create().texOffs(39, 3).addBox(4.0F, -11.0F, -3.0F, 1F, 2F, 2F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("head6", CubeListBuilder.create().texOffs(39, 3).addBox(-5.0F, -11.0F, -3.0F, 1F, 2F, 2F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("head7", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8F, 2F, 8F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8F, 6F, 4F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(16, 26).addBox(-4.0F, 10.0F, -2.0F, 8F, 6F, 4F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(16, 37).addBox(-3.0F, 6.0F, -2.0F, 1F, 4F, 4F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("body5", CubeListBuilder.create().texOffs(16, 37).addBox(2.0F, 6.0F, -2.0F, 1F, 4F, 4F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("rightArm2", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4F, 16F, 4F),
                PartPose.offsetAndRotation(-5.0F, -3.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        pd.addOrReplaceChild("rightArm3", CubeListBuilder.create().texOffs(56, 20).addBox(-2.0F, -5.0F, -7.0F, 2F, 28F, 2F),
                PartPose.offsetAndRotation(-5.0F, -3.0F, 0.0F, 0.3490659F, 0.0F, 0.0F));
        pd.addOrReplaceChild("rightArm4", CubeListBuilder.create().texOffs(38, 37).addBox(-4.0F, -8.0F, -7.0F, 6F, 2F, 2F),
                PartPose.offsetAndRotation(-5.0F, -3.0F, 0.0F, 0.3490659F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ChefEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        r1.yRot = age * 0.067F * 1.25F;
        float arm = Mth.cos(limbSwing * 0.6662F + Mth.PI) * limbSwingAmount;
        float atk = attackTime > 0f ? -Mth.sin(attackTime * Mth.PI) * 1.4F : (e.isFrenzied() ? -0.5F + Mth.sin(age * 0.9F) * 0.4F : 0f);
        rightArm.xRot = arm + 0.349F + atk;
        rightArm2.xRot = arm + atk;
        rightArm3.xRot = arm + 0.349F + atk;
        rightArm4.xRot = arm + 0.349F + atk;
        leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
