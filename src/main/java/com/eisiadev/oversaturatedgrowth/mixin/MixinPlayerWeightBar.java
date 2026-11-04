package com.eisiadev.oversaturatedgrowth.mixin;

import com.eisiadev.oversaturatedgrowth.accessory.AccessoryHooks;
import net.willsbr.gluttonousgrowth.WeightSystem.PlayerWeightBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerWeightBar.class)
public abstract class MixinPlayerWeightBar {

    @ModifyVariable(method = "addWeightChanges", at = @At("HEAD"), argsOnly = true, remap = false)
    private int og$absorb(int input) {
        return AccessoryHooks.absorb(input);
    }
}
