package com.eisiadev.oversaturatedgrowth.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FattaberryAteEffect extends MobEffect {
    public FattaberryAteEffect() {
        super(MobEffectCategory.HARMFUL, 0x9B4DCA);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "5b3d9c04-71aa-4e2f-b9c8-0d6e4a1f2c93", -0.05,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
