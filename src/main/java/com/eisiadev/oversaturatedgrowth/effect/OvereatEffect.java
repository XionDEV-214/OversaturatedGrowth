package com.eisiadev.oversaturatedgrowth.effect;

import com.eisiadev.oversaturatedgrowth.bridge.GgBridge;
import com.eisiadev.oversaturatedgrowth.init.ModItems;
import com.eisiadev.oversaturatedgrowth.init.ModSounds;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import com.eisiadev.oversaturatedgrowth.entity.LardPuddleEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class OvereatEffect extends MobEffect {
    private static final int RETCH_INTERVAL = 100;

    public OvereatEffect() {
        super(MobEffectCategory.HARMFUL, 0xB8862B);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "7d1f6c52-3a9b-4f0e-9a43-5b1a0c2e7d11", -0.15,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) { retch(entity, true); }

    public static void retch(LivingEntity entity, boolean full) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        Vec3 mouth = entity.getEyePosition().add(entity.getLookAngle().scale(0.3)).add(0, -0.15, 0);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), net.minecraft.sounds.SoundEvents.PLAYER_BURP, SoundSource.PLAYERS,
                full ? 1.0f : 0.6f, 0.5f);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.LARD_SLUDGE_BALL.get())),
                mouth.x, mouth.y, mouth.z, full ? 12 : 6, 0.12, 0.08, 0.12, 0.08);
        if (entity instanceof ServerPlayer player) {
            if (full) {
                GgBridge.drainCalories(player, Math.max(1, GgBridge.currentCalories(player) / 2));
            } else {
                double mult = 1.0 + 0.25 * InflationHelper.effectiveLevel(player);
                GgBridge.drainCalories(player, Math.max(1, (int) Math.round(GgBridge.maxCalories(player) * 0.02 * mult)));
            }
        }
        if (full) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, true));
            LardPuddleEntity.spawn(level, entity.position(), 2.0f, 100);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % RETCH_INTERVAL == 0; }
}
