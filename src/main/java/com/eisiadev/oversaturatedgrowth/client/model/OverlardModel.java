package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.OverlardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class OverlardModel extends EntityModel<OverlardEntity> {
    private static final String[] NAMES = {"core", "topA", "topB", "sideL", "sideR", "front", "back", "blobA", "blobB", "footL", "footR"};
    private final ModelPart root;
    private final ModelPart[] lumps = new ModelPart[NAMES.length];

    public OverlardModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        for (int i = 0; i < NAMES.length; i++) lumps[i] = root.getChild(NAMES[i]);
    }

    private static void lump(PartDefinition pd, String n, int v, float x, float y, float z, float w, float h, float d) {
        pd.addOrReplaceChild(n, CubeListBuilder.create().texOffs(0, v).addBox(x, y, z, w, h, d), PartPose.offset(0, 24, 0));
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();

        lump(pd, "core",    0, -10, -16, -10, 20, 16, 20);
        lump(pd, "topA",   48,  -8, -23,  -7, 15, 10, 14);
        lump(pd, "topB",    0,  -4, -28,  -3,  9,  8,  9);
        lump(pd, "sideL",  48, -13, -12,  -6,  6, 10, 12);
        lump(pd, "sideR",   0,   7, -14,  -5,  6, 12, 10);
        lump(pd, "front",  48,  -7, -11, -13, 14,  9,  5);
        lump(pd, "back",    0,  -6, -13,   8, 12, 11,  5);
        lump(pd, "blobA",  48,   3, -20, -12,  7,  7,  6);
        lump(pd, "blobB",   0, -12, -19,   0,  6,  7,  7);
        lump(pd, "footL",  48, -12,  -5, -12,  6,  5,  6);
        lump(pd, "footR",   0,   6,  -5,   6,  6,  5,  6);
        return LayerDefinition.create(mesh, 128, 96);
    }

    @Override
    public void setupAnim(OverlardEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {

        for (int i = 0; i < lumps.length; i++) {
            lumps[i].y = 24 + Mth.sin(age * 0.09f + i * 1.7f) * 0.4f;
            lumps[i].x = Mth.cos(age * 0.07f + i * 2.3f) * 0.3f;
            lumps[i].z = Mth.sin(age * 0.06f + i * 1.1f) * 0.3f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
