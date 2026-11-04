package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PlumpHelper {
    private PlumpHelper() {}

    public static <T extends Animal & IPlump> InteractionResult tryFeed(T animal, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.REFINED_LARD.get()) || animal.getPlumpLevel() >= IPlump.MAX_PLUMP || animal.isBaby()) return InteractionResult.PASS;
        if (!animal.level().isClientSide) {
            animal.setPlumpLevel(animal.getPlumpLevel() + 1);
            if (!player.getAbilities().instabuild) held.shrink(1);
            animal.playSound(SoundEvents.GENERIC_EAT, 1.0f, 0.7f);
            if (animal.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + animal.getBbHeight() * 0.8,
                        animal.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
            }
        }
        return InteractionResult.sidedSuccess(animal.level().isClientSide);
    }

    public static void dropExtra(Animal a, int level, int looting) {
        int n = level + a.getRandom().nextInt(1 + looting + level / 2);
        a.spawnAtLocation(new ItemStack(ModItems.RAW_LARDSOAKED_MEAT.get(), n));
    }
}
