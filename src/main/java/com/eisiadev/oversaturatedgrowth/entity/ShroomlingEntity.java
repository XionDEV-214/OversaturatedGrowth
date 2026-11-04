package com.eisiadev.oversaturatedgrowth.entity;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ShroomlingEntity extends PathfinderMob {

    public ShroomlingEntity(EntityType<? extends ShroomlingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 80.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(3, new SporeShuttleGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    private static boolean isShroom(BlockState s) {
        return s.is(ModBlocks.LARDSHROOM.get());
    }

    private class SporeShuttleGoal extends Goal {
        private static final int RADIUS = 64, VRANGE = 10, PER_TICK = 3000, LEG_TIMEOUT = 900;
        private int phase;
        private int cooldown = 200 + getRandom().nextInt(400);
        private int cursor, legTicks;
        private final List<BlockPos> found = new ArrayList<>();
        private BlockPos a, b, mid;

        SporeShuttleGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override
        public boolean canUse() {
            if (getTarget() != null) return false;
            if (cooldown > 0) { cooldown--; return false; }
            return true;
        }

        @Override
        public boolean canContinueToUse() { return getTarget() == null && phase != 0; }

        @Override
        public void start() { phase = 1; cursor = 0; found.clear(); }

        @Override
        public void stop() {
            phase = 0;
            cooldown = 600 + getRandom().nextInt(600);
            getNavigation().stop();
        }

        @Override
        public void tick() {
            switch (phase) {
                case 1 -> scan();
                case 2 -> travel(a, 3);
                case 3 -> travel(b, 4);
                case 4 -> travel(mid, 5);
                default -> { }
            }
            if (phase >= 2 && phase <= 4 && ++legTicks > LEG_TIMEOUT) phase = 0;
        }

        private void scan() {
            int w = RADIUS * 2 + 1, h = VRANGE * 2 + 1, total = w * w * h;
            BlockPos o = blockPosition();
            int n = 0;
            while (n++ < PER_TICK && cursor < total) {
                int i = cursor++;
                int dy = i % h - VRANGE;
                int dx = (i / h) % w - RADIUS;
                int dz = i / (h * w) - RADIUS;
                BlockPos p = o.offset(dx, dy, dz);
                if (!level().hasChunkAt(p)) continue;
                if (isShroom(level().getBlockState(p)) && found.size() < 48) found.add(p);
            }
            if (cursor < total) return;
            if (found.size() < 2) { phase = 0; return; }
            BlockPos first = found.get(0);
            double best = first.distSqr(o);
            for (BlockPos p : found) { double d = p.distSqr(o); if (d < best) { best = d; first = p; } }
            List<BlockPos> cand = new ArrayList<>();
            for (BlockPos p : found) {
                double d = Math.sqrt(p.distSqr(first));
                if (d >= 3 && d <= 64) cand.add(p);
            }
            if (cand.isEmpty()) { phase = 0; return; }
            a = first;
            b = cand.get(getRandom().nextInt(cand.size()));
            mid = findSporeSpot(new BlockPos((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2, (a.getZ() + b.getZ()) / 2));
            if (mid == null) { phase = 0; return; }
            phase = 2; legTicks = 0;
            go(a);
        }

        private BlockPos findSporeSpot(BlockPos c) {
            for (int r = 0; r <= 5; r++) {
                for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    for (int dy = 6; dy >= -6; dy--) {
                        BlockPos p = c.offset(dx, dy, dz);
                        if (!level().hasChunkAt(p)) continue;
                        if (level().isEmptyBlock(p)
                                && ModBlocks.LARDSHROOM.get().defaultBlockState().canSurvive(level(), p)) return p;
                    }
                }
            }
            return null;
        }

        private void go(BlockPos p) {
            var path = getNavigation().createPath(p, 1);
            if (path == null) { phase = 0; return; }
            getNavigation().moveTo(path, 1.0);
        }

        private void travel(BlockPos target, int nextPhase) {
            if (target == null) { phase = 0; return; }
            Vec3 c = Vec3.atCenterOf(target);
            if (distanceToSqr(c) <= 6.25) {
                legTicks = 0;
                if (nextPhase == 5) { releaseSpores(); phase = 0; return; }
                phase = nextPhase;
                go(nextPhase == 3 ? b : mid);
            } else if (getNavigation().isDone()) {
                go(target);
            }
        }

        private void releaseSpores() {
            if (!(level() instanceof ServerLevel sl)) return;
            BlockState st = ModBlocks.LARDSHROOM.get().defaultBlockState();
            if (mid != null && sl.isEmptyBlock(mid) && st.canSurvive(sl, mid)) {
                sl.setBlock(mid, st, Block.UPDATE_ALL);
            }
            sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 0.6, getZ(), 40, 0.5, 0.4, 0.5, 0.02);
            sl.sendParticles(ParticleTypes.MYCELIUM, getX(), getY() + 0.6, getZ(), 20, 0.4, 0.3, 0.4, 0.02);
            playSound(SoundEvents.FUNGUS_BREAK, 1.0f, 0.8f);
            playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, 0.6f, 1.2f);
        }
    }
}
