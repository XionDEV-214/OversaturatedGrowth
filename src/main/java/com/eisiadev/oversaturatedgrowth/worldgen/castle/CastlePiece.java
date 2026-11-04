package com.eisiadev.oversaturatedgrowth.worldgen.castle;

import com.eisiadev.oversaturatedgrowth.init.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class CastlePiece extends StructurePiece {
    public enum Kind { KEEP, TOWER, SPIRE, BRIDGE, GATE, PLAZA, MAZE, SHAFT }

    private final Kind kind;
    private final int x, y, z;
    private final int p0, p1, p2, p3;
    private final int phaseY;
    private final long seed;

    public CastlePiece(Kind kind, int x, int y, int z, int p0, int p1, int p2, int p3, int phaseY, long seed) {
        super(ModStructures.CASTLE_PIECE.get(), 0, boxFor(kind, x, y, z, p0, p1, p2, p3));
        this.kind = kind; this.x = x; this.y = y; this.z = z; this.p0 = p0; this.p1 = p1; this.p2 = p2; this.p3 = p3;
        this.phaseY = phaseY; this.seed = seed;
        setOrientation(null);
    }

    public CastlePiece(StructurePieceSerializationContext ctx, CompoundTag t) {
        super(ModStructures.CASTLE_PIECE.get(), t);
        this.kind = Kind.values()[t.getInt("Kind")];
        this.x = t.getInt("X"); this.y = t.getInt("Y"); this.z = t.getInt("Z");
        this.p0 = t.getInt("P0"); this.p1 = t.getInt("P1"); this.p2 = t.getInt("P2"); this.p3 = t.getInt("P3");
        this.phaseY = t.getInt("Phase"); this.seed = t.getLong("Seed");
        setOrientation(null);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag t) {
        t.putInt("Kind", kind.ordinal()); t.putInt("X", x); t.putInt("Y", y); t.putInt("Z", z);
        t.putInt("P0", p0); t.putInt("P1", p1); t.putInt("P2", p2); t.putInt("P3", p3);
        t.putInt("Phase", phaseY); t.putLong("Seed", seed);
    }

    private static BoundingBox boxFor(Kind k, int x, int y, int z, int p0, int p1, int p2, int p3) {
        int dn = 40;
        switch (k) {
            case KEEP: { int h = p0 / 2 + 4; return new BoundingBox(x - h, y - dn, z - h, x + h, y + 40, z + h); }
            case TOWER: {
                int h = p0 / 2 + 5;
                int down = (p3 & CastleBuild.FLAG_FLOATING) != 0 ? p0 / 2 + 6 : Math.max(6, ((p3 >>> 8) & 0xFF) + 4);
                return new BoundingBox(x - h, y - down, z - h, x + h, y + p1 * 8 + p0 * 2 + 16, z + h);
            }
            case SPIRE: { int h = p0 / 2 + 4; return new BoundingBox(x - h, y - 2, z - h, x + h, y + p1 + p0 * 2 + 12, z + h); }
            case BRIDGE: {
                int lo = Math.min(p1, p2) - 5, hi = Math.max(p1, p2) + 5;
                int bd = (p3 >>> 4) != 0 ? 110 : 2;
                return p0 == 1 ? new BoundingBox(lo, y - bd, z - 5, hi, y + 9, z + 5) : new BoundingBox(x - 5, y - bd, lo, x + 5, y + 9, hi);
            }
            case PLAZA: { int h = p0 + 2; return new BoundingBox(x - h, y - dn, z - h, x + h, y + 5, z + h); }
            case MAZE: { int h = (p0 * 4 + 1) / 2 + 1; return new BoundingBox(x - h, y - (p1 - 1) * 8 - 1, z - h, x + h, y + 7, z + h); }
            case SHAFT: return new BoundingBox(x - 4, y, z - 4, x + 4, p0 + 1, z + 4);
            default: { int hw = p0 / 2 + 3; return new BoundingBox(x - hw, y - dn, z, x + hw, y + 14, z + p1 * 2 + 4); }
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen, RandomSource rnd,
                            BoundingBox box, ChunkPos cp, BlockPos pivot) {
        int minX = Math.max(box.minX(), boundingBox.minX()), maxX = Math.min(box.maxX(), boundingBox.maxX());
        int minZ = Math.max(box.minZ(), boundingBox.minZ()), maxZ = Math.min(box.maxZ(), boundingBox.maxZ());
        int minY = Math.max(box.minY(), boundingBox.minY()), maxY = Math.min(box.maxY(), boundingBox.maxY());
        if (minX > maxX || minZ > maxZ || minY > maxY) return;
        CastleCanvas c = new CastleCanvas(level, new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ), seed);
        switch (kind) {
            case KEEP -> CastleBuild.keep(c, x, y, z, p0, phaseY);
            case TOWER -> CastleBuild.tower(c, x, y, z, p0, p1, p2, p3, phaseY);
            case SPIRE -> CastleBuild.spire(c, x, y, z, p0, p1, ((long) p3 << 32) | (p2 & 0xFFFFFFFFL), phaseY);
            case BRIDGE -> CastleBuild.bridge(c, p0 == 1, p1, p2, p0 == 1 ? z : x, y, phaseY, p3);
            case GATE -> CastleBuild.gateStairs(c, x, z, y, p0, p1);
            case PLAZA -> CastleBuild.plaza(c, x, y, z, p0);
            case MAZE -> CastleBuild.maze(c, x, y, z, p0, p1);
            case SHAFT -> CastleBuild.shaft(c, x, z, y, p0, phaseY);
        }
    }
}
