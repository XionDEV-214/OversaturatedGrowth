package com.eisiadev.oversaturatedgrowth.worldgen.castle;

import com.eisiadev.oversaturatedgrowth.worldgen.castle.CastlePiece.Kind;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

import java.util.ArrayList;
import java.util.List;

public final class CastleLayout {
    private CastleLayout() {}

    public interface HeightFn { int at(int x, int z); }

    public static final int PLINTH = 8;
    public static final int KEEP = 49, PLAZA = 33;
    public static final int SPIRE = 31;
    public static final int MAZE_CELLS = 23, MAZE_LEVELS = 8, MAZE_DEPTH = 26;
    public static final int RADIUS = 118;
    public static final int PIER_MAX = 100;
    public static final int UPPER = 48;
    public static final int RELAY_EVERY = 6;

    private record Box(int x0, int y0, int z0, int x1, int y1, int z1) {
        boolean hits(Box o) { return x0 <= o.x1 && x1 >= o.x0 && y0 <= o.y1 && y1 >= o.y0 && z0 <= o.z1 && z1 >= o.z0; }
    }

    private static final class Node {
        final int cx, cz, half, base, floors; final Box box;
        boolean keep, spire, floating;
        Direction from;
        Node(int cx, int cz, int half, int base, int floors, Box box) { this.cx = cx; this.cz = cz; this.half = half; this.base = base; this.floors = floors; this.box = box; }
        int floorY(int i) { return base + 8 * i; }
        int topExit() { return base + 8 * (floors - 1); }
    }

    private static final class Ctx {
        final int cx, cz, y0, maxY; final long seed; final RandomSource r; final HeightFn h;
        final List<Box> occ = new ArrayList<>();
        final List<Node> nodes = new ArrayList<>();
        final List<StructurePiece> towers = new ArrayList<>(), bridges = new ArrayList<>();
        int maxFloors, count, hTop, hCyl;
        Ctx(int cx, int cz, int y0, int maxY, long seed, HeightFn h) { this.cx = cx; this.cz = cz; this.y0 = y0; this.maxY = maxY; this.seed = seed; this.r = RandomSource.create(seed); this.h = h; }
        boolean inside(Box q) { return q.x0 >= cx - RADIUS && q.x1 <= cx + RADIUS && q.z0 >= cz - RADIUS && q.z1 <= cz + RADIUS && q.y1 < maxY - 2; }
        boolean free(Box q, Box... ignore) {
            outer:
            for (Box o : occ) { for (Box ig : ignore) if (ig == o) continue outer; if (o.hits(q)) return false; }
            return true;
        }
        long nextSeed() { return seed ^ (++count * 0x9E3779B97F4A7C15L); }
    }

    public static List<StructurePiece> generate(int cx, int groundY, int cz, long seed, HeightFn hf, int minY, int maxY) {
        int y0 = groundY + PLINTH;
        Ctx c = new Ctx(cx, cz, y0, maxY, seed, hf);
        c.maxFloors = 48;
        List<StructurePiece> core = new ArrayList<>();
        core.add(new CastlePiece(Kind.PLAZA, cx, y0, cz, PLAZA, 0, 0, 0, y0, seed));
        core.add(new CastlePiece(Kind.KEEP, cx, y0, cz, KEEP, 0, 0, 0, y0, seed));
        Box plaza = new Box(cx - PLAZA - 2, groundY - 20, cz - PLAZA - 2, cx + PLAZA + 2, y0 + 40, cz + PLAZA + 2);
        c.occ.add(plaza);
        c.occ.add(new Box(cx - 10, groundY - 20, cz + PLAZA, cx + 10, y0 + 14, cz + PLAZA + PLINTH * 2 + 6));

        int sBase = y0 + 36, sHeight = CastleBuild.spireHeight(SPIRE, sBase, maxY);
        int sFloors = (sHeight - 1) / 8, arenaIdx = sFloors - 3;
        Box sBox = new Box(cx - SPIRE / 2 - 3, sBase, cz - SPIRE / 2 - 3, cx + SPIRE / 2 + 3, sBase + sHeight + SPIRE * 2 + 12, cz + SPIRE / 2 + 3);
        c.occ.add(sBox);
        Node spire = new Node(cx, cz, SPIRE / 2, sBase, arenaIdx, sBox); spire.spire = true;
        c.hTop = sBase + sHeight;
        c.hCyl = y0 + (int) ((c.hTop - y0) * 0.62);
        long seal = relays(c, spire, arenaIdx);
        core.add(new CastlePiece(Kind.SPIRE, cx, sBase, cz, SPIRE, sHeight, (int) seal, (int) (seal >>> 32), y0, seed ^ 0x5151));

        Node keepNode = new Node(cx, cz, KEEP / 2, y0, 4, plaza); keepNode.keep = true;
        int D = 10 + c.r.nextInt(4);
        double a0 = c.r.nextDouble() * Math.PI * 2;
        for (int d = 0; d < D; d++) {
            double ang = a0 + d * Math.PI * 2 / D + (c.r.nextDouble() - 0.5) * 0.4;
            int dist = 85 + c.r.nextInt(25);
            chain(c, keepNode, cx + (int) Math.round(Math.cos(ang) * dist), cz + (int) Math.round(Math.sin(ang) * dist));
        }

        for (int i = 0; i < 80; i++) {
            List<Node> ground = c.nodes.stream().filter(n -> !n.floating && !n.spire).toList();
            if (ground.isEmpty()) break;
            Node n = ground.get(c.r.nextInt(ground.size()));
            placeGround(c, n, Direction.Plane.HORIZONTAL.getRandomDirection(c.r), 6 + c.r.nextInt(14), randomSize(c), -1, randomRoof(c), 0, c.r.nextInt(3) == 0 ? 1 : 0);
        }

        for (int i = 0; i < 10; i++) {
            List<Node> ground = c.nodes.stream().filter(n -> !n.floating && !n.spire).toList();
            if (ground.isEmpty()) break;
            wreck(c, ground.get(c.r.nextInt(ground.size())), Direction.Plane.HORIZONTAL.getRandomDirection(c.r));
        }

        List<Node> anchors = new ArrayList<>(c.nodes);
        anchors.add(spire);
        int lowY = c.y0 + UPPER, highY = c.hTop - 40;
        for (int t = 0; t < 900 && highY > lowY; t++) {
            int ty = lowY + c.r.nextInt(highY - lowY);
            Node pick = null; int pickFloor = -1, seen = 0;
            for (Node n : anchors) {
                int i = Math.round((ty - n.base) / 8.0f);
                if (i < 0 || i >= n.floors || Math.abs(n.floorY(i) - ty) > 12 || n.floorY(i) < lowY) continue;
                if (c.r.nextInt(++seen) == 0) { pick = n; pickFloor = i; }
            }
            if (pick == null) continue;
            Node w = wing(c, pick, pickFloor);
            if (w != null) anchors.add(w);
        }

        interconnect(c, spire);

        List<StructurePiece> out = new ArrayList<>(core);
        out.addAll(c.towers);
        int mazeTop = Math.min(groundY, y0 - PLINTH) - MAZE_DEPTH;
        int levels = Math.max(1, Math.min(MAZE_LEVELS, (mazeTop - (minY + 6)) / 8 + 1));
        out.add(new CastlePiece(Kind.MAZE, cx, mazeTop, cz, MAZE_CELLS, levels, 0, 0, y0, seed ^ 0x3A3A));
        out.add(new CastlePiece(Kind.SHAFT, cx, mazeTop, cz, y0, 0, 0, 0, y0, seed));
        out.add(new CastlePiece(Kind.GATE, cx, y0, cz + PLAZA + 1, 15, PLINTH, 0, 0, y0, seed ^ 5));
        out.addAll(c.bridges);
        return out;
    }

    private static long relays(Ctx c, Node s, int arenaIdx) {
        long mask = 0;
        Direction[] dirs = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
        int k = c.r.nextInt(4);
        for (int b = 5; b + 4 <= arenaIdx - 1 && b + 1 < 64; b += RELAY_EVERY) {
            boolean ok = false;
            for (int t = 0; t < 4 && !ok; t++, k++) ok = relay(c, s, dirs[k % 4], b);
            if (ok) mask |= 1L << (b + 1);
        }
        return mask;
    }

    private static boolean relay(Ctx c, Node s, Direction d, int b) {
        int exitY = s.floorY(b);
        Direction l = d.getClockWise();
        int size = 11, h = size / 2, far1 = 4 + c.r.nextInt(6), far2 = 4 + c.r.nextInt(2);
        int lat = 8;
        int ax = s.cx + d.getStepX() * (s.half + far1 + h + 1) + l.getStepX() * lat;
        int az = s.cz + d.getStepZ() * (s.half + far1 + h + 1) + l.getStepZ() * lat;
        int step = h + far2 + h + 1;
        int bx = ax - l.getStepX() * step, bz = az - l.getStepZ() * step;
        Node A = floatingNode(ax, az, h, exitY, 3, size);
        Node B = floatingNode(bx, bz, h, exitY + 16, 3, size);
        if (!c.inside(A.box) || !c.inside(B.box) || !c.free(A.box) || !c.free(B.box) || A.box.hits(B.box)) return false;
        if (groundHit(c, A) || groundHit(c, B)) return false;
        Bridge b1 = between(s, A, exitY), b2 = between(A, B, exitY + 16), b3 = between(B, s, exitY + 32);
        if (b1 == null || b2 == null || b3 == null) return false;
        if (!c.free(b1.box, s.box) || !c.free(b2.box) || !c.free(b3.box, s.box)) return false;
        if (b1.box.hits(B.box) || b3.box.hits(A.box)) return false;
        commitFloating(c, A, size, 0); commitFloating(c, B, size, 0);
        commitBridge(c, b1, 0); commitBridge(c, b2, 0); commitBridge(c, b3, 0);
        return true;
    }

    private static void chain(Ctx c, Node start, int dx, int dz) {
        Node cur = start;
        for (int depth = 0; depth < 16; depth++) {
            boolean near = !cur.keep && Math.abs(cur.cx - dx) + Math.abs(cur.cz - dz) < 30;
            Direction[] dirs = ranked(cur, dx, dz);
            Node next = null;
            if (near) {
                for (Direction d : dirs) {
                    next = placeGround(c, cur, d, 8 + c.r.nextInt(10), 17 + 4 * c.r.nextInt(2), -1, CastleBuild.ROOF_CONICAL, 0, 0);
                    if (next != null) break;
                }
                return;
            }
            for (Direction d : dirs) {
                if (d == cur.from) continue;
                next = placeGround(c, cur, d, 8 + c.r.nextInt(22), randomSize(c), -1, randomRoof(c), c.r.nextInt(10) == 0 ? CastleBuild.FLAG_DAMAGED : 0, c.r.nextInt(4) == 0 ? 1 : 0);
                if (next != null) break;
            }
            if (next == null && cur.from != null) next = placeGround(c, cur, cur.from, 8 + c.r.nextInt(22), randomSize(c), -1, randomRoof(c), 0, 0);
            if (next == null) return;
            next.from = dirOf(cur, next);
            cur = next;
        }
    }

    private static int randomSize(Ctx c) { int[] s = {9, 11, 13, 13, 13, 15, 17}; return s[c.r.nextInt(s.length)]; }
    private static int randomRoof(Ctx c) { int r = c.r.nextInt(10); return r < 4 ? CastleBuild.ROOF_CONICAL : r < 7 ? CastleBuild.ROOF_CRENEL : CastleBuild.ROOF_PEAKED; }

    private static int envelope(Ctx c, int x, int z) {
        double d = Math.sqrt((double) (x - c.cx) * (x - c.cx) + (double) (z - c.cz) * (z - c.cz));
        double f = Math.max(0, 1.0 - d / (RADIUS + 6));
        return c.hCyl + (int) ((c.hTop - 20 - c.hCyl) * f);
    }

    private static int envFloors(Ctx c, int x, int z, int g) {
        int room = envelope(c, x, z) - g - 30;
        int f = (int) (room * (0.45 + 0.55 * c.r.nextDouble())) / 8;
        return Math.max(2, Math.min(c.maxFloors, f));
    }

    private static Node placeGround(Ctx c, Node cur, Direction d, int howFar, int size, int floors, int roof, int flags, int variant) {
        int h2 = size / 2;
        if (cur.keep) howFar += PLAZA - KEEP / 2 + 4;
        int exitY = cur.keep ? c.y0 + 24 : cur.floorY(c.r.nextInt(cur.floors));
        if (!cur.keep && exitY > c.y0 + UPPER + 40) exitY = cur.floorY(c.r.nextInt(Math.min(cur.floors, 4)));
        int J = cur.keep ? KEEP / 2 - 6 : Math.max(0, Math.min(cur.half, h2) - 4);
        int jit = J == 0 ? 0 : c.r.nextInt(J * 2 + 1) - J;
        int ncx = cur.cx + d.getStepX() * (cur.half + howFar + h2 + 1) + (d.getAxis() == Direction.Axis.Z ? jit : 0);
        int ncz = cur.cz + d.getStepZ() * (cur.half + howFar + h2 + 1) + (d.getAxis() == Direction.Axis.X ? jit : 0);
        if (floors < 0) floors = envFloors(c, ncx, ncz, c.h.at(ncx, ncz));

        int g = c.h.at(ncx, ncz), gMin = g;
        for (int[] q : new int[][]{{-h2, -h2}, {h2, -h2}, {-h2, h2}, {h2, h2}}) gMin = Math.min(gMin, c.h.at(ncx + q[0], ncz + q[1]));
        int base = Integer.MIN_VALUE, startE = c.r.nextInt(floors);
        for (int k = 0; k < floors; k++) {
            int b = exitY - 8 * ((startE + k) % floors);
            if (b >= g - 1 && b <= g + 34) { base = b; break; }
        }
        if (base == Integer.MIN_VALUE) return null;
        int roofH = roof == CastleBuild.ROOF_CRENEL ? 5 : size * 2 + 8;
        Box box = new Box(ncx - h2 - 2, gMin - 2, ncz - h2 - 2, ncx + h2 + 2, base + floors * 8 + roofH, ncz + h2 + 2);
        if (!c.inside(box) || !c.free(box)) return null;
        Node n = new Node(ncx, ncz, h2, base, floors, box);
        Bridge br = between(cur, n, exitY);
        if (br == null || !c.free(br.box, cur.box)) return null;
        int depth = Math.min(250, base - gMin + 3);
        int doors = base - g <= 2 ? 1 << c.r.nextInt(4) : 0;
        c.occ.add(box);
        c.nodes.add(n);
        c.towers.add(new CastlePiece(Kind.TOWER, ncx, base, ncz, size, floors, roof, flags | doors | (depth << 8), c.y0, c.nextSeed()));
        commitBridge(c, br, variant);
        return n;
    }

    private static Node wing(Ctx c, Node from, int ei) {
        int exitY = from.floorY(ei);
        int[] sizes = {7, 9, 9, 11, 11, 13, 13};
        int size = sizes[c.r.nextInt(sizes.length)], h2 = size / 2;
        Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(c.r);
        int howFar = 3 + c.r.nextInt(12);
        int J = Math.max(0, Math.min(from.half, h2) - 4);
        int jit = J == 0 ? 0 : c.r.nextInt(J * 2 + 1) - J;
        int ncx = from.cx + d.getStepX() * (from.half + howFar + h2 + 1) + (d.getAxis() == Direction.Axis.Z ? jit : 0);
        int ncz = from.cz + d.getStepZ() * (from.half + howFar + h2 + 1) + (d.getAxis() == Direction.Axis.X ? jit : 0);
        int env = envelope(c, ncx, ncz);
        int floors = 1 + c.r.nextInt(5), entrance = c.r.nextInt(floors);
        int base = exitY - 8 * entrance;
        while (floors > 1 && base + floors * 8 + size + 6 > env) { floors--; if (entrance >= floors) { entrance = floors - 1; base = exitY - 8 * entrance; } }
        if (base + floors * 8 > env) return null;
        Node n = floatingNode(ncx, ncz, h2, base, floors, size);
        if (!c.inside(n.box) || !c.free(n.box) || groundHit(c, n)) return null;
        Bridge br = between(from, n, exitY);
        if (br == null || !c.free(br.box, from.box)) return null;
        commitFloating(c, n, size, 0);
        commitBridge(c, br, c.r.nextInt(3) == 0 ? 1 : 0);
        return n;
    }

    private static Node floatingNode(int x, int z, int h, int base, int floors, int size) {
        Box box = new Box(x - h - 2, base - h - 6, z - h - 2, x + h + 2, base + floors * 8 + size * 2 + 8, z + h + 2);
        Node n = new Node(x, z, h, base, floors, box);
        n.floating = true;
        return n;
    }

    private static boolean groundHit(Ctx c, Node n) { return n.box.y0 <= c.h.at(n.cx, n.cz) + 4; }

    private static void commitFloating(Ctx c, Node n, int size, int flags) {
        c.occ.add(n.box);
        c.nodes.add(n);
        c.towers.add(new CastlePiece(Kind.TOWER, n.cx, n.base, n.cz, size, n.floors, randomRoof(c), flags | CastleBuild.FLAG_FLOATING, c.y0, c.nextSeed()));
    }

    private static void interconnect(Ctx c, Node spire) {
        List<Node> all = new ArrayList<>(c.nodes);
        all.add(spire);
        int made = 0;
        for (int i = 0; i < all.size() && made < 80; i++) for (int j = i + 1; j < all.size() && made < 80; j++) {
            Node a = all.get(i), b = all.get(j);
            if (c.r.nextInt(100) >= 60) continue;
            int lo = Math.max(a.base, b.base), hi = Math.min(a.topExit(), b.topExit());
            if (lo > hi || Math.floorMod(a.base - b.base, 8) != 0) continue;
            if (a.spire && lo >= a.floorY(a.floors)) continue;
            List<Integer> ys = new ArrayList<>();
            for (int y = lo; y <= hi; y += 8) ys.add(y);
            int y = ys.get(c.r.nextInt(ys.size()));
            Bridge br = between(a, b, y);
            if (br == null || br.length < 3 || br.length > 40) continue;
            if (!c.free(br.box, a.box, b.box)) continue;
            commitBridge(c, br, c.r.nextInt(3) == 0 ? 1 : 0);
            made++;
        }
    }

    private record Bridge(boolean axisX, int a0, int a1, int perp, int y, Box box, int length) {}

    private static Bridge between(Node a, Node b, int y) {
        int dx = b.cx - a.cx, dz = b.cz - a.cz;
        boolean xGap = Math.abs(dx) > a.half + b.half + 2, zGap = Math.abs(dz) > a.half + b.half + 2;
        int ma = a.spire ? 4 : 3, mb = b.spire ? 4 : 3;
        if (xGap && !zGap) {
            int lo = Math.max(a.cz - a.half + ma, b.cz - b.half + mb), hi = Math.min(a.cz + a.half - ma, b.cz + b.half - mb);
            if (lo > hi) return null;
            int perp = (lo + hi) / 2;
            if (a.spire || b.spire) { int sc = a.spire ? a.cz : b.cz; if (Math.abs(perp - sc) <= 2) perp = perp < sc ? sc - 3 : sc + 3; if (perp < lo || perp > hi) return null; }
            int s = dx > 0 ? a.cx + a.half + 1 : b.cx + b.half + 1, e = dx > 0 ? b.cx - b.half - 1 : a.cx - a.half - 1;
            if (e < s) return null;
            return new Bridge(true, s, e, perp, y, new Box(s, y - 1, perp - 4, e, y + 8, perp + 4), e - s + 1);
        }
        if (zGap && !xGap) {
            int lo = Math.max(a.cx - a.half + ma, b.cx - b.half + mb), hi = Math.min(a.cx + a.half - ma, b.cx + b.half - mb);
            if (lo > hi) return null;
            int perp = (lo + hi) / 2;
            if (a.spire || b.spire) { int sc = a.spire ? a.cx : b.cx; if (Math.abs(perp - sc) <= 2) perp = perp < sc ? sc - 3 : sc + 3; if (perp < lo || perp > hi) return null; }
            int s = dz > 0 ? a.cz + a.half + 1 : b.cz + b.half + 1, e = dz > 0 ? b.cz - b.half - 1 : a.cz - a.half - 1;
            if (e < s) return null;
            return new Bridge(false, s, e, perp, y, new Box(perp - 4, y - 1, s, perp + 4, y + 8, e), e - s + 1);
        }
        return null;
    }

    private static void commitBridge(Ctx c, Bridge b, int variant) {
        int mask = 0;
        for (int a = b.a0 + 5, k = 0; a < b.a1 - 4 && k < 20; a += 10, k++) {
            boolean ok = true;
            for (int[] q : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
                int x = b.axisX ? a + q[0] : b.perp + q[0], z = b.axisX ? b.perp + q[1] : a + q[1];
                if (b.y - c.h.at(x, z) > PIER_MAX) { ok = false; break; }
            }
            if (ok) mask |= 1 << k;
        }
        c.occ.add(b.box);
        c.bridges.add(new CastlePiece(Kind.BRIDGE, b.axisX ? 0 : b.perp, b.y, b.axisX ? b.perp : 0, b.axisX ? 1 : 0,
                b.a0, b.a1, variant | (mask << 4), c.y0, c.nextSeed()));
    }

    private static void wreck(Ctx c, Node cur, Direction d) {
        int size = c.r.nextBoolean() ? 9 : 13, h2 = size / 2;
        int far = 6 + c.r.nextInt(10);
        int ncx = cur.cx + d.getStepX() * (cur.half + far + h2 + 1), ncz = cur.cz + d.getStepZ() * (cur.half + far + h2 + 1);
        int g = c.h.at(ncx, ncz);
        Box box = new Box(ncx - h2 - 2, g - 4, ncz - h2 - 2, ncx + h2 + 2, g + 24, ncz + h2 + 2);
        if (!c.inside(box) || !c.free(box)) return;
        c.occ.add(box);
        c.towers.add(new CastlePiece(Kind.TOWER, ncx, g, ncz, size, 1 + c.r.nextInt(2), 0, CastleBuild.FLAG_WRECKED | 15 | (4 << 8), c.y0, c.nextSeed()));
    }

    private static Direction dirOf(Node a, Node b) {
        int dx = b.cx - a.cx, dz = b.cz - a.cz;
        if (Math.abs(dx) > Math.abs(dz)) return dx > 0 ? Direction.EAST : Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static Direction[] ranked(Node n, int dx, int dz) {
        int ex = dx - n.cx, ez = dz - n.cz;
        Direction px = ex >= 0 ? Direction.EAST : Direction.WEST, pz = ez >= 0 ? Direction.SOUTH : Direction.NORTH;
        Direction a = Math.abs(ex) > Math.abs(ez) ? px : pz, b = a == px ? pz : px;
        return new Direction[]{a, b, b.getOpposite(), a.getOpposite()};
    }

    public static List<StructurePiece> sample(String name, int x, int y, int z, long seed, HeightFn hf, int minY, int maxY) {
        List<StructurePiece> l = new ArrayList<>();
        switch (name) {
            case "keep" -> {
                l.add(new CastlePiece(Kind.PLAZA, x, y, z, PLAZA, 0, 0, 0, y, seed));
                l.add(new CastlePiece(Kind.KEEP, x, y, z, KEEP, 0, 0, 0, y, seed));
            }
            case "tower" -> {
                int[][] v = {{13, 4, CastleBuild.ROOF_CONICAL, 0}, {13, 3, CastleBuild.ROOF_CRENEL, 0}, {15, 4, CastleBuild.ROOF_PEAKED, 0},
                        {13, 4, CastleBuild.ROOF_CONICAL, CastleBuild.FLAG_DAMAGED}, {13, 2, 0, CastleBuild.FLAG_WRECKED | 15}, {21, 6, CastleBuild.ROOF_CONICAL, 0},
                        {11, 2, CastleBuild.ROOF_CONICAL, CastleBuild.FLAG_FLOATING}};
                for (int i = 0; i < v.length; i++) {
                    int yy = (v[i][3] & CastleBuild.FLAG_FLOATING) != 0 ? y + 20 : y;
                    l.add(new CastlePiece(Kind.TOWER, x + i * 28, yy, z, v[i][0], v[i][1], v[i][2], v[i][3] | (20 << 8), y, seed + i));
                }
            }
            case "maze" -> {
                int top = y - MAZE_DEPTH;
                l.add(new CastlePiece(Kind.MAZE, x, top, z, MAZE_CELLS, Math.max(1, Math.min(MAZE_LEVELS, (top - (minY + 6)) / 8 + 1)), 0, 0, y, seed ^ 0x3A3A));
                l.add(new CastlePiece(Kind.SHAFT, x, top, z, y, 0, 0, 0, y, seed));
            }
            default -> l.addAll(generate(x, y - PLINTH, z, seed, hf, minY, maxY));
        }
        return l;
    }
}
