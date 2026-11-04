package com.eisiadev.oversaturatedgrowth.sound;

import com.eisiadev.oversaturatedgrowth.init.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class OGSounds {
    private OGSounds() {}

    public static void swallow(Level level, Entity at) {
        level.playSound(null, at.getX(), at.getY(), at.getZ(), ModSounds.SWALLOW.get(), SoundSource.PLAYERS,
                1.0f, 0.9f + level.random.nextFloat() * 0.2f);
    }
}
