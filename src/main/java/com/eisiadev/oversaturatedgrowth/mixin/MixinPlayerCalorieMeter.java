package com.eisiadev.oversaturatedgrowth.mixin;

import com.eisiadev.oversaturatedgrowth.accessory.IOGCapacityBonus;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryHooks;
import net.willsbr.gluttonousgrowth.StuffedBar.PlayerCalorieMeter;
import net.willsbr.gluttonousgrowth.config.GluttonousWorldConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerCalorieMeter.class)
public abstract class MixinPlayerCalorieMeter implements IOGCapacityBonus {

    @Shadow(remap = false)
    private int maxCalories;

    @Unique
    private int og$capacityBonus;

    @Override
    public int og$getCapacityBonus() {
        return og$capacityBonus;
    }

    @Override
    public void og$setCapacityBonus(int bonus) {
        this.og$capacityBonus = Math.max(0, bonus);
    }

    @Override
    public int og$rawMaxCalories() {
        return this.maxCalories;
    }

    @Inject(method = "getMaxCalories", at = @At("RETURN"), cancellable = true, remap = false)
    private void og$addBonus(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValueI() + this.og$capacityBonus);
    }

    @Inject(method = "setMaxCalories", at = @At("HEAD"), cancellable = true, remap = false)
    private void og$setMax(int value, CallbackInfo ci) {
        if (AccessoryHooks.inStuffedSystem()) {
            // The only setMaxCalories call inside stuffedSystem is GG's interval growth.
            int grown = this.maxCalories + GluttonousWorldConfig.calCapIncrement.get();
            this.maxCalories = Math.min(grown, GluttonousWorldConfig.absCalCap.get());
        } else {
            this.maxCalories = Math.max(1, value - this.og$capacityBonus);
        }
        ci.cancel();
    }

    @Redirect(method = "addCalories",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
                    target = "Lnet/willsbr/gluttonousgrowth/StuffedBar/PlayerCalorieMeter;maxCalories:I"),
            remap = false)
    private int og$totalMaxInAdd(PlayerCalorieMeter self) {
        return this.maxCalories + this.og$capacityBonus;
    }

    @Redirect(method = "copyFrom",
            at = @At(value = "INVOKE",
                    target = "Lnet/willsbr/gluttonousgrowth/StuffedBar/PlayerCalorieMeter;getMaxCalories()I"),
            remap = false)
    private int og$copyStoredMax(PlayerCalorieMeter source) {
        return ((IOGCapacityBonus) (Object) source).og$rawMaxCalories();
    }
}
