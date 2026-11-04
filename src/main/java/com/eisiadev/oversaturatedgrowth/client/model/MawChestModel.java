package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.MawChestEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class MawChestModel extends EntityModel<MawChestEntity> {
    private final ModelPart root, lid, tongue;

    public MawChestModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.lid = root.getChild("lid");
        this.tongue = root.getChild("base").getChild("tongue");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition pd = mesh.getRoot();
        PartDefinition base = pd.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 19)
                .addBox(-7, -10, -7, 14, 10, 14), PartPose.offset(0, 24, 0));

        base.addOrReplaceChild("teeth_low", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-6, -12, -6.5f, 2, 2, 2).addBox(-2, -12, -6.5f, 2, 2, 2).addBox(2, -12, -6.5f, 2, 2, 2).addBox(4, -12, -6.5f, 2, 2, 2)
                .addBox(-6, -12, 4.5f, 2, 2, 2).addBox(-2, -12, 4.5f, 2, 2, 2).addBox(2, -12, 4.5f, 2, 2, 2).addBox(4, -12, 4.5f, 2, 2, 2),
                PartPose.ZERO);
        base.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(32, 48)
                .addBox(-3, -10.5f, -5, 6, 1, 10), PartPose.ZERO);
        PartDefinition lid = pd.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-7, -5, -14, 14, 5, 14), PartPose.offset(0, 14, 7));

        lid.addOrReplaceChild("teeth_up", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-6, 0, -13.5f, 2, 2, 2).addBox(-2, 0, -13.5f, 2, 2, 2).addBox(2, 0, -13.5f, 2, 2, 2).addBox(4, 0, -13.5f, 2, 2, 2)
                .addBox(-6, 0, -2.5f, 2, 2, 2).addBox(-2, 0, -2.5f, 2, 2, 2).addBox(2, 0, -2.5f, 2, 2, 2).addBox(4, 0, -2.5f, 2, 2, 2),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(MawChestEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        float open;
        if (e.isHolding()) open = 0.15f + (Mth.sin(age * 0.6f) * 0.5f + 0.5f) * 0.25f;
        else if (e.getTarget() != null) open = 0.55f + Mth.sin(age * 0.3f) * 0.1f;
        else open = 0f;
        lid.xRot = -open * 1.6f;
        tongue.y = (float) Math.sin(age * 0.15f) * 0.4f;
        root.zRot = e.isHolding() ? Mth.sin(age * 1.3f) * 0.04f : 0f;
        root.y = 0f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
