package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.worldgen.GrottoRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID)
public class GrottoSpawnHandler {
    private static final int INTERVAL = 200;
    private static final int CAP = 14;
    private static final int MIN_DIST = 20, MAX_DIST = 44;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        if (sp.tickCount % INTERVAL != 0 || sp.isSpectator()) return;
        ServerLevel level = sp.serverLevel();
        if (!level.getBiome(sp.blockPosition()).is(GrottoRegion.LARDSLUDGE_GROTTO)) return;

        AABB box = sp.getBoundingBox().inflate(64);
        int existing = level.getEntitiesOfClass(Animal.class, box,
                a -> a instanceof com.eisiadev.oversaturatedgrowth.entity.IPlump).size();
        if (existing >= CAP) return;

        RandomSource rnd = level.random;
        List<EntityType<? extends Mob>> types = List.of(ModEntities.LARDY_PIG.get(), ModEntities.LARDY_COW.get(),
                ModEntities.LARDY_SHEEP.get(), ModEntities.LARDY_CHICKEN.get());
        for (int i = 0; i < 12; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2;
            double dist = MIN_DIST + rnd.nextDouble() * (MAX_DIST - MIN_DIST);
            BlockPos base = BlockPos.containing(sp.getX() + Math.cos(ang) * dist, sp.getY() + rnd.nextInt(17) - 8, sp.getZ() + Math.sin(ang) * dist);
            BlockPos floor = findFloor(level, base);
            if (floor == null) continue;
            if (!level.getBiome(floor).is(GrottoRegion.LARDSLUDGE_GROTTO)) continue;
            if (level.getNearestPlayer(floor.getX() + 0.5, floor.getY(), floor.getZ() + 0.5, MIN_DIST - 4, false) != null) continue;

            EntityType<? extends Mob> type = types.get(rnd.nextInt(types.size()));
            int n = 1 + rnd.nextInt(3);
            for (int k = 0; k < n; k++) {
                BlockPos p = k == 0 ? floor : findFloor(level, floor.offset(rnd.nextInt(5) - 2, 0, rnd.nextInt(5) - 2));
                if (p == null) continue;
                Mob mob = type.create(level);
                if (mob == null) continue;
                mob.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rnd.nextFloat() * 360F, 0F);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(p), MobSpawnType.NATURAL, null, null);
                level.addFreshEntityWithPassengers(mob);
            }
            return;
        }
    }

    private static BlockPos findFloor(ServerLevel level, BlockPos base) {
        for (int dy = 0; dy <= 6; dy++) {
            for (int s : new int[]{1, -1}) {
                BlockPos p = base.offset(0, dy * s, 0);
                if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
                        && level.getBlockState(p.above(2)).isAir()
                        && level.getFluidState(p).isEmpty()
                        && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) return p;
            }
        }
        return null;
    }
}
