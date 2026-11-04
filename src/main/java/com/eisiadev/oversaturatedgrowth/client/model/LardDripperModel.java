package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.LardDripperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class LardDripperModel extends EntityModel<LardDripperEntity> {
    private final ModelPart root;
    private final ModelPart cluster;
    private final ModelPart tip;

    public LardDripperModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.cluster = root.getChild("cluster");
        this.tip = root.getChild("tip");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();

        pd.addOrReplaceChild("cluster", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, 0, -4, 8, 3, 8)
                .texOffs(0, 11).addBox(-3, 3, -3, 6, 4, 6)
                .texOffs(0, 21).addBox(0, 4, -3, 4, 4, 4)
                .texOffs(16, 21).addBox(-3.5f, 6, 0, 3, 4, 3),
                PartPose.offset(0, 10, 0));
        pd.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.5f, 0, -1.5f, 3, 4, 3), PartPose.offset(0, 17, 0));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LardDripperEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        cluster.xRot = (float) Math.sin(age * 0.05f) * 0.04f;
        cluster.zRot = (float) Math.cos(age * 0.04f) * 0.04f;
        tip.xRot = cluster.xRot * 2.0f;
        tip.zRot = cluster.zRot * 2.0f;
        tip.y = 17 + (float) Math.sin(age * 0.08f) * 0.6f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
