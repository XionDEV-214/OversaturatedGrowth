package com.eisiadev.oversaturatedgrowth.mixin;

import com.eisiadev.oversaturatedgrowth.accessory.AccessoryHooks;
import net.minecraftforge.event.TickEvent;
import net.willsbr.gluttonousgrowth.Event.ModEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModEvent.class)
public abstract class MixinModEvent {

    @Inject(method = "stuffedSystem", at = @At("HEAD"), remap = false)
    private static void og$enterStuffed(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        AccessoryHooks.enterStuffed(event.player);
    }

    @Inject(method = "stuffedSystem", at = @At("RETURN"), remap = false)
    private static void og$exitStuffed(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        AccessoryHooks.exitStuffed();
    }
}
