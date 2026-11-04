package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.LardBloatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class LardBloatModel extends EntityModel<LardBloatEntity> {
    private static final int N = 4;
    private final ModelPart root;
    private final ModelPart[] upper = new ModelPart[N];
    private final ModelPart[] lower = new ModelPart[N];

    public LardBloatModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        for (int i = 0; i < N; i++) {
            upper[i] = root.getChild("t" + i);
            lower[i] = upper[i].getChild("l");
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();
        pd.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-6, -6, -6, 12, 12, 12), PartPose.offset(0, 12, 0));
        pd.addOrReplaceChild("nozzle", CubeListBuilder.create().texOffs(0, 24)
                .addBox(-2, 0, -2, 4, 3, 4), PartPose.offset(0, 18, 0));
        float[][] pos = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int i = 0; i < N; i++) {
            PartDefinition t = pd.addOrReplaceChild("t" + i, CubeListBuilder.create().texOffs(0, 40)
                    .addBox(-1, 0, -1, 2, 6, 2), PartPose.offset(pos[i][0], 18, pos[i][1]));
            t.addOrReplaceChild("l", CubeListBuilder.create().texOffs(8, 40)
                    .addBox(-1, 0, -1, 2, 6, 2), PartPose.offset(0, 6, 0));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LardBloatEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        for (int i = 0; i < N; i++) {
            float ph = i * 1.7f;
            upper[i].xRot = (float) Math.sin(age * 0.09f + ph) * 0.25f;
            upper[i].zRot = (float) Math.cos(age * 0.07f + ph) * 0.25f;
            lower[i].xRot = (float) Math.sin(age * 0.09f + ph - 0.9f) * 0.45f;
            lower[i].zRot = (float) Math.cos(age * 0.07f + ph - 0.9f) * 0.45f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
