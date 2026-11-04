package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModEffects;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID)
public class GasCloudManager {

    private static final float DUST_SIZE = 1.0f;
    private static final Vector3f COLOR = new Vector3f(0.96f, 0.60f, 0.13f);

    private static class Cloud {
        final ServerLevel level; final Vec3 pos; final float radius; int life;
        Cloud(ServerLevel l, Vec3 p, float r, int life) { level = l; pos = p; radius = r; this.life = life; }
    }

    private static final List<Cloud> CLOUDS = new ArrayList<>();

    public static void spawn(ServerLevel level, Vec3 pos, float radius, int ticks) {
        CLOUDS.add(new Cloud(level, pos, radius, ticks));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || CLOUDS.isEmpty()) return;
        for (Iterator<Cloud> it = CLOUDS.iterator(); it.hasNext(); ) {
            Cloud c = it.next();
            if (--c.life <= 0 || c.level.getServer() == null) { it.remove(); continue; }
            float fade = Math.min(1f, c.life / 40f);
            float r = c.radius * (0.6f + 0.4f * fade);
            if (c.life % 2 == 0) {
                int n = (int) (6 * fade) + 2;
                for (int i = 0; i < n; i++) {
                    double ox = (c.level.random.nextDouble() * 2 - 1) * r * 0.8;
                    double oy = (c.level.random.nextDouble() * 2 - 1) * r * 0.5;
                    double oz = (c.level.random.nextDouble() * 2 - 1) * r * 0.8;

                    c.level.sendParticles(ParticleTypes.ENTITY_EFFECT, c.pos.x + ox, c.pos.y + oy, c.pos.z + oz,
                            0, COLOR.x, COLOR.y, COLOR.z, 1.0);
                }
                c.level.sendParticles(new DustParticleOptions(COLOR, DUST_SIZE), c.pos.x, c.pos.y, c.pos.z,
                        (int) (5 * fade) + 1, r * 0.5, r * 0.3, r * 0.5, 0.0);
            }
            if (c.life % 10 == 0) {
                var box = new net.minecraft.world.phys.AABB(c.pos, c.pos).inflate(r, r * 0.7, r);
                for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, box)) {
                    e.addEffect(new MobEffectInstance(ModEffects.OVERSATURATED.get(), 100, 0));
                }
            }
        }
    }
}
