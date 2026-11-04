package com.eisiadev.oversaturatedgrowth.client.model;

import com.eisiadev.oversaturatedgrowth.entity.LardstoneArcherEntity;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;

public class LardstoneArcherModel extends SkeletonModel<LardstoneArcherEntity> {
    public LardstoneArcherModel(ModelPart root) { super(root); }

    @Override
    public void setupAnim(LardstoneArcherEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        super.setupAnim(e, limbSwing, limbSwingAmount, age, yaw, pitch);
        if (e.isArmsUp()) {
            this.rightArm.xRot = -1.35f;
            this.leftArm.xRot = -1.35f;
            this.rightArm.yRot = -0.3f;
            this.leftArm.yRot = 0.3f;
            this.rightArm.zRot = 0f;
            this.leftArm.zRot = 0f;
        }
    }
}
