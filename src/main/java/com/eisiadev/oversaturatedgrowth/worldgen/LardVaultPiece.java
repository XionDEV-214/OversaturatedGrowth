package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModEntities;
import com.eisiadev.oversaturatedgrowth.init.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.ArrayList;
import java.util.List;

public class LardVaultPiece extends StructurePiece {
    private static final int H_MAIN = 6;
    private static final int H_ANNEX = 5;
    private static final int H_CORR = 4;
    private static final ResourceLocation LOOT = ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "chests/lard_vault");

    private final long seed;
    private final int cx, cy, cz;

    private record Room(int x0, int x1, int y0, int y1, int z0, int z1, int kind) {
        boolean inside(int x, int y, int z) { return x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1; }
        boolean shell(int x, int y, int z) { return x >= x0 - 1 && x <= x1 + 1 && y >= y0 - 1 && y <= y1 + 1 && z >= z0 - 1 && z <= z1 + 1; }
    }

    public LardVaultPiece(BlockPos center, long seed) {
        super(ModStructures.LARD_VAULT_PIECE.get(), 0, computeBox(center.getX(), center.getY(), center.getZ()));
        this.seed = seed; this.cx = center.getX(); this.cy = center.getY(); this.cz = center.getZ();
        setOrientation(null);
    }

    public LardVaultPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.LARD_VAULT_PIECE.get(), tag);
        this.seed = tag.getLong("Seed"); this.cx = tag.getInt("CX"); this.cy = tag.getInt("CY"); this.cz = tag.getInt("CZ");
        setOrientation(null);
    }

    private static BoundingBox computeBox(int x, int y, int z) {

        return new BoundingBox(x - 19, y - 24, z - 19, x + 19, y + H_MAIN + 1, z + 19);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putLong("Seed", seed); tag.putInt("CX", cx); tag.putInt("CY", cy); tag.putInt("CZ", cz);
    }

    private List<Room> layout(RandomSource r) {
        List<Room> rooms = new ArrayList<>();
        int hw = 4 + r.nextInt(3), hd = 4 + r.nextInt(3);
        Room main = new Room(cx - hw, cx + hw, cy, cy + H_MAIN - 1, cz - hd, cz + hd, 0);
        rooms.add(main);
        List<Direction> dirs = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        int annexes = 1 + r.nextInt(3);
        for (int i = 0; i < annexes; i++) {
            Direction d = dirs.remove(r.nextInt(dirs.size()));
            int half = 2 + r.nextInt(2);
            int corrLen = 4;
            int kind = (i == annexes - 1) ? 2 : 1;
            int s = d.getAxis() == Direction.Axis.X ? d.getStepX() : d.getStepZ();
            if (d.getAxis() == Direction.Axis.X) {
                int start = s > 0 ? main.x1 + 1 : main.x0 - 1;
                int end = start + s * (corrLen - 1);
                rooms.add(new Room(Math.min(start, end), Math.max(start, end), cy, cy + H_CORR - 1, cz - 1, cz + 1, 3));
                int c = (s > 0 ? main.x1 : main.x0) + s * (corrLen + 1 + half);
                rooms.add(new Room(c - half, c + half, cy, cy + H_ANNEX - 1, cz - half, cz + half, kind));
            } else {
                int start = s > 0 ? main.z1 + 1 : main.z0 - 1;
                int end = start + s * (corrLen - 1);
                rooms.add(new Room(cx - 1, cx + 1, cy, cy + H_CORR - 1, Math.min(start, end), Math.max(start, end), 3));
                int c = (s > 0 ? main.z1 : main.z0) + s * (corrLen + 1 + half);
                rooms.add(new Room(cx - half, cx + half, cy, cy + H_ANNEX - 1, c - half, c + half, kind));
            }
        }
        return rooms;
    }

    private long hash(int x, int y, int z) {
        long h = seed ^ (x * 341873128712L) ^ (y * 132897987541L) ^ (z * 42317861L);
        h ^= h >>> 33; h *= 0xff51afd7ed558ccdL; h ^= h >>> 33; h *= 0xc4ceb9fe1a85ec53L; h ^= h >>> 33;
        return h;
    }

    private BlockState wallBlock(int x, int y, int z, boolean floor) {
        int r = (int) Math.floorMod(hash(x, y, z), 100L);
        if (floor) {
            if (r < 55) return ModBlocks.POLISHED_LARDSTONE.get().defaultBlockState();
            if (r < 75) return ModBlocks.LARDSTONE_BRICKS.get().defaultBlockState();
            if (r < 85) return ModBlocks.GREASY_LARDSTONE.get().defaultBlockState();
            if (r < 93) return ModBlocks.LARD_BLOCK.get().defaultBlockState();
            return ModBlocks.WEATHERED_LARDSTONE.get().defaultBlockState();
        }
        if (r < 55) return ModBlocks.LARDSTONE_BRICKS.get().defaultBlockState();
        if (r < 72) return ModBlocks.WEATHERED_LARDSTONE.get().defaultBlockState();
        if (r < 84) return ModBlocks.LARDSTONE.get().defaultBlockState();
        if (r < 92) return ModBlocks.GREASY_LARDSTONE.get().defaultBlockState();
        if (r < 96) return ModBlocks.WEATHERED_GREASY_LARDSTONE.get().defaultBlockState();
        return ModBlocks.LARDSTONE_SLATES.get().defaultBlockState();
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen, RandomSource rndIn,
                            BoundingBox box, ChunkPos cp, BlockPos pivot) {
        RandomSource r = RandomSource.create(seed);
        List<Room> rooms = layout(r);
        Room main = rooms.get(0);
        BlockState air = Blocks.AIR.defaultBlockState();

        int minX = Math.max(box.minX(), this.boundingBox.minX()), maxX = Math.min(box.maxX(), this.boundingBox.maxX());
        int minZ = Math.max(box.minZ(), this.boundingBox.minZ()), maxZ = Math.min(box.maxZ(), this.boundingBox.maxZ());

        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            for (int y = cy - 1; y <= cy + H_MAIN; y++) {
                boolean inShell = false, inInside = false;
                for (Room rm : rooms) {
                    if (rm.inside(x, y, z)) { inInside = true; break; }
                    if (rm.shell(x, y, z)) inShell = true;
                }
                if (inInside) { placeBlock(level, air, x, y, z, box); continue; }
                if (inShell) placeBlock(level, wallBlock(x, y, z, y == cy - 1), x, y, z, box);
            }

            if (Math.floorMod(x, 4) == 0 && Math.floorMod(z, 4) == 0) {
                boolean under = false;
                for (Room rm : rooms) if (x >= rm.x0 - 1 && x <= rm.x1 + 1 && z >= rm.z0 - 1 && z <= rm.z1 + 1) { under = true; break; }
                if (under) {
                    for (int y = cy - 2; y >= cy - 24; y--) {
                        BlockState s = level.getBlockState(new BlockPos(x, y, z));
                        if (!s.isAir() && s.getFluidState().isEmpty()) break;
                        placeBlock(level, ModBlocks.LARDSTONE_BRICKS.get().defaultBlockState(), x, y, z, box);
                    }
                }
            }
        }

        BlockState carved = ModBlocks.CARVED_LARDSTONE.get().defaultBlockState();
        int[][] corners = {{main.x0, main.z0}, {main.x0, main.z1}, {main.x1, main.z0}, {main.x1, main.z1}};
        for (int[] c : corners) for (int y = main.y0; y <= main.y1; y++) placeBlock(level, carved, c[0], y, c[1], box);
        BlockState bricks = ModBlocks.LARDSTONE_BRICKS.get().defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) placeBlock(level, bricks, cx + dx, cy, cz + dz, box);
        placeSpawner(level, new BlockPos(cx, cy + 1, cz), ModEntities.LARDSTONE_ARCHER.get(), box, r);

        breach(level, main, rooms, r, box);

        for (int i = 1; i < rooms.size(); i++) {
            Room rm = rooms.get(i);
            if (rm.kind == 3) continue;
            int mx = (rm.x0 + rm.x1) / 2, mz = (rm.z0 + rm.z1) / 2;
            if (rm.kind == 1) {
                placeChest(level, new BlockPos(rm.x0, cy, rm.z0), Direction.SOUTH, box, r);
                placeMaw(level, new BlockPos(mx, cy, mz), box, r);
                if (rm.x1 - rm.x0 >= 6) placeMaw(level, new BlockPos(rm.x1, cy, rm.z1), box, r);

                placeChest(level, new BlockPos(rm.x1, cy, rm.z0), Direction.WEST, box, r);
            } else {
                placeChest(level, new BlockPos(rm.x0, cy, mz), Direction.EAST, box, r);
                placeChest(level, new BlockPos(rm.x1, cy, mz), Direction.WEST, box, r);
                placeChest(level, new BlockPos(mx, cy, rm.z0), Direction.SOUTH, box, r);
                placeSpawner(level, new BlockPos(mx, cy + 1, mz), ModEntities.LARD_CRAWLER.get(), box, r);
                placeBlock(level, bricks, mx, cy, mz, box);
            }
        }
    }

    private void breach(WorldGenLevel level, Room main, List<Room> rooms, RandomSource r, BoundingBox box) {

        List<Direction> free = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        for (Room rm : rooms) if (rm.kind == 3) {
            if (rm.x0 > main.x1) free.remove(Direction.EAST);
            else if (rm.x1 < main.x0) free.remove(Direction.WEST);
            else if (rm.z0 > main.z1) free.remove(Direction.SOUTH);
            else free.remove(Direction.NORTH);
        }
        if (free.isEmpty()) return;
        Direction d = free.get(r.nextInt(free.size()));
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState rubble = ModBlocks.WEATHERED_LARDSTONE.get().defaultBlockState();
        for (int a = -1; a <= 1; a++) for (int yy = 0; yy < 3; yy++) {
            int x = d.getAxis() == Direction.Axis.X ? (d == Direction.EAST ? main.x1 + 1 : main.x0 - 1) : cx + a;
            int z = d.getAxis() == Direction.Axis.Z ? (d == Direction.SOUTH ? main.z1 + 1 : main.z0 - 1) : cz + a;
            placeBlock(level, air, x, cy + yy, z, box);
        }

        int rx = cx + d.getStepX() * (d.getAxis() == Direction.Axis.X ? (d == Direction.EAST ? main.x1 - cx : cx - main.x0) : 0);
        int rz = cz + d.getStepZ() * (d.getAxis() == Direction.Axis.Z ? (d == Direction.SOUTH ? main.z1 - cz : cz - main.z0) : 0);
        placeBlock(level, rubble, rx - d.getStepX(), cy, rz - d.getStepZ(), box);
    }

    private void placeChest(WorldGenLevel level, BlockPos p, Direction facing, BoundingBox box, RandomSource r) {
        if (!box.isInside(p)) return;
        BlockState st = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
        placeBlock(level, st, p.getX(), p.getY(), p.getZ(), box);
        BlockEntity be = level.getBlockEntity(p);
        if (be instanceof RandomizableContainerBlockEntity c) c.setLootTable(LOOT, hash(p.getX(), p.getY(), p.getZ()));
    }

    private void placeSpawner(WorldGenLevel level, BlockPos p, EntityType<?> type, BoundingBox box, RandomSource r) {
        if (!box.isInside(p)) return;
        placeBlock(level, Blocks.SPAWNER.defaultBlockState(), p.getX(), p.getY(), p.getZ(), box);
        BlockEntity be = level.getBlockEntity(p);
        if (be instanceof SpawnerBlockEntity sp) sp.setEntityId(type, level.getRandom());
    }

    private void placeMaw(WorldGenLevel level, BlockPos p, BoundingBox box, RandomSource r) {
        if (!box.isInside(p)) return;
        ServerLevel sl = level.getLevel();
        Mob m = ModEntities.MAW_CHEST.get().create(sl);
        if (m == null) return;
        m.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, r.nextFloat() * 360F, 0F);
        m.setPersistenceRequired();
        m.finalizeSpawn(level, level.getCurrentDifficultyAt(p), MobSpawnType.STRUCTURE, null, null);
        level.addFreshEntityWithPassengers(m);
    }
}
