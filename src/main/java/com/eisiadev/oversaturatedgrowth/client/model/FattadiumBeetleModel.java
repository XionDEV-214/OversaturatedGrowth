package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.FattadiumBeetleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class FattadiumBeetleModel extends EntityModel<FattadiumBeetleEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart[] legs = new ModelPart[6];

    public FattadiumBeetleModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.head = root.getChild("head");
        for (int i = 0; i < 6; i++) legs[i] = root.getChild("leg" + i);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();
        pd.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5, -3, -5, 10, 6, 12), PartPose.offset(0, 17, 0));
        pd.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 18)
                .addBox(-4, -2, -4, 8, 2, 10), PartPose.offset(0, 14, 0));
        pd.addOrReplaceChild("head", CubeListBuilder.create().texOffs(44, 0)
                .addBox(-3, -2, -4, 6, 4, 4)
                .texOffs(44, 8).addBox(-3, 1, -6, 2, 1, 2)
                .texOffs(44, 8).addBox(1, 1, -6, 2, 1, 2), PartPose.offset(0, 17, -5));
        for (int i = 0; i < 6; i++) {
            boolean left = i % 2 == 1;
            float z = -3 + (i / 2) * 3.5f;
            CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 30).addBox(left ? 0 : -6, -0.5f, -0.5f, 6, 1, 1);
            pd.addOrReplaceChild("leg" + i, leg, PartPose.offset(left ? 4 : -4, 19, z));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(FattadiumBeetleEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        head.yRot = yaw * ((float) Math.PI / 180f);
        head.xRot = pitch * ((float) Math.PI / 180f);
        float splay = 0.5f;
        for (int i = 0; i < 6; i++) {
            boolean left = i % 2 == 1;
            int row = i / 2;
            float side = left ? 1 : -1;
            legs[i].zRot = side * 0.45f;
            legs[i].yRot = side * (row - 1) * -splay * 0.5f;
            float phase = (row % 2 == 0 ? 0 : (float) Math.PI) + (left ? (float) Math.PI : 0);
            legs[i].yRot += Mth.cos(limbSwing * 0.6662f + phase) * 0.6f * limbSwingAmount;
            legs[i].zRot += side * Math.abs(Mth.sin(limbSwing * 0.6662f + phase)) * 0.4f * limbSwingAmount;
        }
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
