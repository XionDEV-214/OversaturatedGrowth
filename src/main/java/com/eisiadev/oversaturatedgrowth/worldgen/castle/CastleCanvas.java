package com.eisiadev.oversaturatedgrowth.worldgen.castle;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class CastleCanvas {
    public static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public final WorldGenLevel level;
    public final BoundingBox clip;
    public final long seed;
    private final BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

    public CastleCanvas(WorldGenLevel level, BoundingBox clip, long seed) {
        this.level = level; this.clip = clip; this.seed = seed;
    }

    public BlockState wood(int x, int y, int z) {
        double n = noise(x * 0.21, y * 0.21, z * 0.21);
        double m = noise(x * 0.17 + 900, y * 0.17, z * 0.17);
        double u = Math.floorMod(hash(x, y, z), 10000L) / 10000.0;
        double pc = 0.05 + 0.40 * smooth((n - 0.55) / 0.35);
        double pi = 0.18 * smooth((0.35 - m) / 0.25);
        if (u < pc) return ModBlocks.CRACKED_CASTLE_WOOD.get().defaultBlockState();
        if (u < pc + pi) return ModBlocks.INFESTED_CASTLE_WOOD.get().defaultBlockState();
        return ModBlocks.CASTLE_WOOD.get().defaultBlockState();
    }
    private static double smooth(double t) { t = Math.max(0, Math.min(1, t)); return t * t * (3 - 2 * t); }

    public BlockState brick(int x, int y, int z) {
        double n = noise(x * 0.13 + 50, y * 0.13, z * 0.13);
        int r = (int) Math.floorMod(hash(x, y, z), 100L);
        if (n > 0.66) return ModBlocks.WEATHERED_LARDSTONE.get().defaultBlockState();
        if (n < 0.18 && r < 40) return ModBlocks.GREASY_LARDSTONE.get().defaultBlockState();
        if (r < 4) return ModBlocks.LARDSTONE.get().defaultBlockState();
        return ModBlocks.LARDSTONE_BRICKS.get().defaultBlockState();
    }
    public static BlockState plain()   { return ModBlocks.CASTLE_WOOD.get().defaultBlockState(); }
    public static BlockState encased() { return ModBlocks.ENCASED_CASTLE_WOOD.get().defaultBlockState(); }
    public static BlockState reappearing() { return ModBlocks.REAPPEARING_CASTLE_DOOR.get().defaultBlockState(); }
    public static BlockState infested(){ return ModBlocks.INFESTED_CASTLE_WOOD.get().defaultBlockState(); }
    public static BlockState floorStone() { return ModBlocks.POLISHED_LARDSTONE.get().defaultBlockState(); }

    public static BlockState bars(boolean alongX) {
        BlockState b = Blocks.IRON_BARS.defaultBlockState();
        return alongX ? b.setValue(net.minecraft.world.level.block.IronBarsBlock.EAST, true).setValue(net.minecraft.world.level.block.IronBarsBlock.WEST, true)
                : b.setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH, true).setValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH, true);
    }
    public static BlockState planks() { return ModBlocks.LARDLOG_PLANKS.get().defaultBlockState(); }
    public static BlockState fire()    { return Blocks.FIRE.defaultBlockState(); }
    public static BlockState lantern() { return Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true); }
    public static BlockState chain()   { return Blocks.CHAIN.defaultBlockState(); }
    public static BlockState slab()    { return ModBlocks.LARDLOG_SLAB.get().defaultBlockState(); }

    public long hash(int x, int y, int z) {
        long h = seed ^ (x * 341873128712L) ^ (y * 132897987541L) ^ (z * 42317861L);
        h ^= h >>> 33; h *= 0xff51afd7ed558ccdL; h ^= h >>> 33; h *= 0xc4ceb9fe1a85ec53L; h ^= h >>> 33;
        return h;
    }

    public double noise(double x, double y, double z) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y), zi = (int) Math.floor(z);
        double fx = x - xi, fy = y - yi, fz = z - zi;
        fx = fx * fx * (3 - 2 * fx); fy = fy * fy * (3 - 2 * fy); fz = fz * fz * (3 - 2 * fz);
        double v000 = lat(xi, yi, zi), v100 = lat(xi + 1, yi, zi), v010 = lat(xi, yi + 1, zi), v110 = lat(xi + 1, yi + 1, zi);
        double v001 = lat(xi, yi, zi + 1), v101 = lat(xi + 1, yi, zi + 1), v011 = lat(xi, yi + 1, zi + 1), v111 = lat(xi + 1, yi + 1, zi + 1);
        double a = v000 + (v100 - v000) * fx, b = v010 + (v110 - v010) * fx;
        double d = v001 + (v101 - v001) * fx, e = v011 + (v111 - v011) * fx;
        double ab = a + (b - a) * fy, de = d + (e - d) * fy;
        return ab + (de - ab) * fz;
    }
    private double lat(int x, int y, int z) { return (hash(x * 7919, y * 104729, z * 1299709) >>> 11) * 0x1.0p-53; }

    public interface BlockFn { BlockState at(int x, int y, int z); }
    public void fillFn(int x0, int y0, int z0, int x1, int y1, int z1, BlockFn f) {
        int ax = Math.max(Math.min(x0, x1), clip.minX()), bx = Math.min(Math.max(x0, x1), clip.maxX());
        int ay = Math.max(Math.min(y0, y1), clip.minY()), by = Math.min(Math.max(y0, y1), clip.maxY());
        int az = Math.max(Math.min(z0, z1), clip.minZ()), bz = Math.min(Math.max(z0, z1), clip.maxZ());
        for (int x = ax; x <= bx; x++) for (int z = az; z <= bz; z++) for (int y = ay; y <= by; y++)
            level.setBlock(mp.set(x, y, z), f.at(x, y, z), 2);
    }

    public void erode(int x0, int y0, int z0, int x1, int y1, int z1, int yStart, int yEnd, double strength) {
        int ax = Math.max(x0, clip.minX()), bx = Math.min(x1, clip.maxX());
        int ay = Math.max(Math.max(y0, yStart), clip.minY()), by = Math.min(y1, clip.maxY());
        int az = Math.max(z0, clip.minZ()), bz = Math.min(z1, clip.maxZ());
        double span = Math.max(1, yEnd - yStart);
        for (int x = ax; x <= bx; x++) for (int z = az; z <= bz; z++) for (int y = ay; y <= by; y++) {
            double t = (y - yStart) / span;
            double n = noise(x * 0.17 + 300, y * 0.17, z * 0.17);
            if (n * 0.9 + t * strength > 1.0) {
                if (!level.getBlockState(mp.set(x, y, z)).isAir()) level.setBlock(mp, AIR, 2);
            }
        }
    }

    public void chest(int x, int y, int z, net.minecraft.core.Direction facing, net.minecraft.resources.ResourceLocation loot) {
        if (!clip.isInside(x, y, z)) return;
        level.setBlock(mp.set(x, y, z), Blocks.CHEST.defaultBlockState().setValue(net.minecraft.world.level.block.ChestBlock.FACING, facing), 2);
        if (level.getBlockEntity(mp) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity be) be.setLootTable(loot, hash(x, y, z));
    }
    public void barrel(int x, int y, int z, net.minecraft.resources.ResourceLocation loot) {
        if (!clip.isInside(x, y, z)) return;
        level.setBlock(mp.set(x, y, z), Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, net.minecraft.core.Direction.UP), 2);
        if (level.getBlockEntity(mp) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity be) be.setLootTable(loot, hash(x, y, z));
    }
    public void spawner(int x, int y, int z, net.minecraft.world.entity.EntityType<?> type) {
        if (!clip.isInside(x, y, z)) return;
        level.setBlock(mp.set(x, y, z), Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(mp) instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity sp) sp.setEntityId(type, level.getRandom());
    }

    public void mobSpawner(int x, int y, int z, String... ids) {
        if (!clip.isInside(x, y, z)) return;
        level.setBlock(mp.set(x, y, z), Blocks.SPAWNER.defaultBlockState(), 2);
        if (!(level.getBlockEntity(mp) instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity sp)) return;
        net.minecraft.nbt.CompoundTag tag = sp.saveWithoutMetadata();
        net.minecraft.nbt.ListTag pot = new net.minecraft.nbt.ListTag();
        for (String id : ids) {
            net.minecraft.nbt.CompoundTag ent = new net.minecraft.nbt.CompoundTag(); ent.putString("id", id);
            net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag(); data.put("entity", ent);
            net.minecraft.nbt.CompoundTag e = new net.minecraft.nbt.CompoundTag(); e.putInt("weight", 1); e.put("data", data);
            pot.add(e);
        }
        net.minecraft.nbt.CompoundTag first = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.CompoundTag fe = new net.minecraft.nbt.CompoundTag(); fe.putString("id", ids[0]);
        first.put("entity", fe);
        tag.put("SpawnData", first);
        tag.put("SpawnPotentials", pot);
        tag.putShort("MaxNearbyEntities", (short) 6);
        tag.putShort("RequiredPlayerRange", (short) 24);
        tag.putShort("MinSpawnDelay", (short) 100);
        tag.putShort("MaxSpawnDelay", (short) 350);
        tag.putShort("SpawnCount", (short) 3);
        sp.load(tag);
    }
    public void mob(int x, int y, int z, net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> type) {
        if (!clip.isInside(x, y, z)) return;
        net.minecraft.world.entity.Mob m = type.create(level.getLevel());
        if (m == null) return;
        m.moveTo(x + 0.5, y, z + 0.5, (hash(x, y, z) & 255) * 1.4f, 0f);
        m.setPersistenceRequired();
        m.finalizeSpawn(level, level.getCurrentDifficultyAt(mp.set(x, y, z)), net.minecraft.world.entity.MobSpawnType.STRUCTURE, null, null);
        level.addFreshEntityWithPassengers(m);
    }

    public boolean in(int x, int y, int z) { return clip.isInside(x, y, z); }
    public void set(int x, int y, int z, BlockState s) {
        if (clip.isInside(x, y, z)) level.setBlock(mp.set(x, y, z), s, 2);
    }
    public BlockState get(int x, int y, int z) {
        return clip.isInside(x, y, z) ? level.getBlockState(mp.set(x, y, z)) : AIR;
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        int ax = Math.max(Math.min(x0, x1), clip.minX()), bx = Math.min(Math.max(x0, x1), clip.maxX());
        int ay = Math.max(Math.min(y0, y1), clip.minY()), by = Math.min(Math.max(y0, y1), clip.maxY());
        int az = Math.max(Math.min(z0, z1), clip.minZ()), bz = Math.min(Math.max(z0, z1), clip.maxZ());
        for (int x = ax; x <= bx; x++) for (int z = az; z <= bz; z++) for (int y = ay; y <= by; y++)
            level.setBlock(mp.set(x, y, z), s, 2);
    }

    public void fillWood(int x0, int y0, int z0, int x1, int y1, int z1) {
        int ax = Math.max(Math.min(x0, x1), clip.minX()), bx = Math.min(Math.max(x0, x1), clip.maxX());
        int ay = Math.max(Math.min(y0, y1), clip.minY()), by = Math.min(Math.max(y0, y1), clip.maxY());
        int az = Math.max(Math.min(z0, z1), clip.minZ()), bz = Math.min(Math.max(z0, z1), clip.maxZ());
        for (int x = ax; x <= bx; x++) for (int z = az; z <= bz; z++) for (int y = ay; y <= by; y++)
            level.setBlock(mp.set(x, y, z), wood(x, y, z), 2);
    }
    public void air(int x0, int y0, int z0, int x1, int y1, int z1) { fill(x0, y0, z0, x1, y1, z1, AIR); }

    public void shell(int x0, int y0, int z0, int x1, int y1, int z1, int t) {
        fillWood(x0, y0, z0, x1, y1, z1);
        air(x0 + t, y0 + 1, z0 + t, x1 - t, y1 - 1, z1 - t);
    }

    public void foundation(int x0, int z0, int x1, int z1, int yTop, int spacing) {
        int ax = Math.max(x0, clip.minX()), bx = Math.min(x1, clip.maxX());
        int az = Math.max(z0, clip.minZ()), bz = Math.min(z1, clip.maxZ());
        for (int x = ax; x <= bx; x++) for (int z = az; z <= bz; z++) {
            boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
            if (!edge && (Math.floorMod(x - x0, spacing) != 0 || Math.floorMod(z - z0, spacing) != 0)) continue;
            for (int y = yTop; y >= clip.minY(); y--) {
                BlockState st = level.getBlockState(mp.set(x, y, z));
                if (!(st.isAir() || !st.getFluidState().isEmpty() || st.canBeReplaced())) break;
                boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                level.setBlock(mp, corner ? encased() : wood(x, y, z), 2);
            }
        }
    }

    public void edges(int x0, int y0, int z0, int x1, int y1, int z1) {
        BlockState e = encased();
        fill(x0, y0, z0, x1, y0, z0, e); fill(x0, y0, z1, x1, y0, z1, e); fill(x0, y1, z0, x1, y1, z0, e); fill(x0, y1, z1, x1, y1, z1, e);
        fill(x0, y0, z0, x0, y0, z1, e); fill(x1, y0, z0, x1, y0, z1, e); fill(x0, y1, z0, x0, y1, z1, e); fill(x1, y1, z0, x1, y1, z1, e);
        fill(x0, y0, z0, x0, y1, z0, e); fill(x1, y0, z0, x1, y1, z0, e); fill(x0, y0, z1, x0, y1, z1, e); fill(x1, y0, z1, x1, y1, z1, e);
    }
}
