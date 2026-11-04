package com.eisiadev.oversaturatedgrowth.worldgen.castle;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.eisiadev.oversaturatedgrowth.worldgen.castle.CastleCanvas.*;

public final class CastleBuild {
    private CastleBuild() {}

    public static final int F = 8;

    private static final int[][] RING = {{-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};

    public static void spiral(CastleCanvas c, int cx, int cz, int yFrom, int yTo, int phaseY) {
        c.fill(cx, yFrom, cz, cx, yTo, cz, encased());
        for (int y = yFrom; y <= yTo; y++) {
            int i = Math.floorMod(y - phaseY, 8);
            c.set(cx + RING[i][0], y, cz + RING[i][1], plain());
        }
    }

    private static final String[] CASTLE_MOBS = {
            "oversaturatedgrowth:butler", "oversaturatedgrowth:butler", "oversaturatedgrowth:butler",
            "oversaturatedgrowth:chef", "oversaturatedgrowth:chef", "oversaturatedgrowth:chef",
            "oversaturatedgrowth:gourmand_knight", "oversaturatedgrowth:gourmand_knight"};

    private static void pillarSpawners(CastleCanvas c, int cx, int cz, int yFloor0, int yTop, int pct) {
        for (int y = yFloor0; y + 3 < yTop; y += F) {
            if (Math.floorMod(c.hash(cx, y, cz) >>> 5, 100L) < pct) c.mobSpawner(cx, y + 2, cz, CASTLE_MOBS);
        }
    }

    private static void floors(CastleCanvas c, int x0, int z0, int x1, int z1, int cx, int cz, int y0, int yTop, int t, int holeR, int phaseY) {
        for (int y = y0 + F; y < yTop; y += F) {
            c.fillWood(x0 + t, y, z0 + t, x1 - t, y, z1 - t);
            c.air(cx - holeR, y, cz - holeR, cx + holeR, y, cz + holeR);
        }
    }

    private static void windows(CastleCanvas c, int x0, int z0, int x1, int z1, int y0, int yTop, int skipHalf, int t) {
        int cx = (x0 + x1) / 2, cz = (z0 + z1) / 2, half = (x1 - x0) / 2;
        int start = Math.max(skipHalf + 1, half >= 6 ? 3 : 2);
        for (int y = y0 + F; y <= yTop; y += F) {
            int wy = y - F + 3;

            for (int o = start; o <= half - 2; o += 4) for (int sg : new int[]{-1, 1}) {
                slit(c, true, cx + sg * o, wy, z0, 1, t, 3, 1);
                slit(c, true, cx + sg * o, wy, z1, -1, t, 3, 1);
                slit(c, false, cz + sg * o, wy, x0, 1, t, 3, 1);
                slit(c, false, cz + sg * o, wy, x1, -1, t, 3, 1);
            }
        }
    }

    public static void slit(CastleCanvas c, boolean faceZ, int a, int wy, int plane, int inward, int t, int h, int w) { slit(c, faceZ, a, wy, plane, inward, t, h, w, false); }
    public static void slit(CastleCanvas c, boolean faceZ, int a, int wy, int plane, int inward, int t, int h, int w, boolean frame) {
        int r = w / 2;
        for (int d = 1; d < t; d++) {
            int p = plane + inward * d;
            if (faceZ) c.air(a - r, wy, p, a + r, wy + h - 1, p); else c.air(p, wy, a - r, p, wy + h - 1, a + r);
        }
        BlockState e = encased();
        if (faceZ) {
            c.fill(a - r, wy, plane, a + r, wy + h - 1, plane, bars(true));
            if (frame) {
            c.fill(a - r - 1, wy - 1, plane, a - r - 1, wy + h, plane, e); c.fill(a + r + 1, wy - 1, plane, a + r + 1, wy + h, plane, e);
            c.fill(a - r, wy - 1, plane, a + r, wy - 1, plane, e); c.fill(a - r, wy + h, plane, a + r, wy + h, plane, e);
            }
        } else {
            c.fill(plane, wy, a - r, plane, wy + h - 1, a + r, bars(false));
            if (frame) {
            c.fill(plane, wy - 1, a - r - 1, plane, wy + h, a - r - 1, e); c.fill(plane, wy - 1, a + r + 1, plane, wy + h, a + r + 1, e);
            c.fill(plane, wy - 1, a - r, plane, wy - 1, a + r, e); c.fill(plane, wy + h, a - r, plane, wy + h, a + r, e);
            }
        }
    }

    public static void doorFrame(CastleCanvas c, boolean axisX, int plane, int perp, int y) {
        BlockState e = encased();
        if (axisX) {
            c.fill(plane, y + 1, perp - 2, plane, y + 6, perp + 2, reappearing());
            c.fill(plane, y + 1, perp - 3, plane, y + 7, perp - 3, e); c.fill(plane, y + 1, perp + 3, plane, y + 7, perp + 3, e);
            c.fill(plane, y + 7, perp - 3, plane, y + 7, perp + 3, e); c.fill(plane, y, perp - 3, plane, y, perp + 3, e);
        } else {
            c.fill(perp - 2, y + 1, plane, perp + 2, y + 6, plane, reappearing());
            c.fill(perp - 3, y + 1, plane, perp - 3, y + 7, plane, e); c.fill(perp + 3, y + 1, plane, perp + 3, y + 7, plane, e);
            c.fill(perp - 3, y + 7, plane, perp + 3, y + 7, plane, e); c.fill(perp - 3, y, plane, perp + 3, y, plane, e);
        }
    }

    private static void cornerPosts(CastleCanvas c, int x0, int z0, int x1, int z1, int y0, int y1) {
        for (int x : new int[]{x0, x1}) for (int z : new int[]{z0, z1}) c.fill(x, y0, z, x, y1, z, encased());
    }

    private static void lanterns(CastleCanvas c, int cx, int cz, int half, int y0, int yTop) {
        for (int y = y0 + F; y <= yTop; y += F) {
            for (int dx : new int[]{-half + 3, half - 3}) for (int dz : new int[]{-half + 3, half - 3}) {
                if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) continue;
                c.set(cx + dx, y - 1, cz + dz, lantern());
            }
        }
    }

    public static void crenellate(CastleCanvas c, int x0, int z0, int x1, int z1, int y) {
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
            if (x != x0 && x != x1 && z != z0 && z != z1) continue;
            long h = c.hash(x, y, z);
            if (Math.floorMod(h, 23L) == 0) continue;
            c.set(x, y + 1, z, c.wood(x, y + 1, z));
            if (((x + z) & 1) == 0 && Math.floorMod(h >>> 8, 7L) != 0) c.set(x, y + 2, z, c.wood(x, y + 2, z));
        }
    }

    public static void brazier(CastleCanvas c, int x, int y, int z) {
        c.set(x, y, z, encased());
        c.set(x, y + 1, z, fire());
    }

    public static final int ROOF_CRENEL = 0, ROOF_PEAKED = 1, ROOF_CONICAL = 2;
    public static final int FLAG_DAMAGED = 16, FLAG_WRECKED = 32, FLAG_FLOATING = 64;

    public static void tower(CastleCanvas c, int cx, int y0, int cz, int size, int floors, int roof, int flags, int phaseY) {
        int half = size / 2, x0 = cx - half, x1 = cx + half, z0 = cz - half, z1 = cz + half;
        int top = y0 + floors * F;
        boolean damaged = (flags & FLAG_DAMAGED) != 0, wrecked = (flags & FLAG_WRECKED) != 0, floating = (flags & FLAG_FLOATING) != 0;
        if (floating) beard(c, cx, y0, cz, half);
        else c.foundation(x0, z0, x1, z1, y0 - 1, 3);
        c.shell(x0, y0, z0, x1, top, z1, 1);
        floors(c, x0, z0, x1, z1, cx, cz, y0, top, 1, 1, phaseY);
        c.edges(x0, y0, z0, x1, top, z1);
        if (!wrecked && size >= 11) for (int k = 0; k < floors; k++)
            furnishRoom(c, x0 + 1, z0 + 1, x1 - 1, z1 - 1, y0 + k * F + 1, cx, cz, (int) Math.floorMod(c.hash(cx, y0 + k, cz), 7L));

        for (int y = y0 + F; y < top; y += F) {
            c.fill(x0, y, z0, x1, y, z0, encased()); c.fill(x0, y, z1, x1, y, z1, encased());
            c.fill(x0, y, z0, x0, y, z1, encased()); c.fill(x1, y, z0, x1, y, z1, encased());
        }
        windows(c, x0, z0, x1, z1, y0, top, 1, 1);
        spiral(c, cx, cz, y0 + 1, top - 1, phaseY);
        if (!wrecked) pillarSpawners(c, cx, cz, y0, top, 80);

        if (!wrecked && size >= 9) for (int k = 0; k < floors; k++)
            if (Math.floorMod(c.hash(cx, y0 + k, cz) >>> 9, 100L) < 55) c.mobSpawner(x0 + 2, y0 + k * F + 1, z0 + 2, CASTLE_MOBS);
        if (!wrecked) lanterns(c, cx, cz, half - 2, y0, top);
        door(c, cx, y0, z1, true, flags, 1);
        door(c, cx, y0, z0, true, flags, 2);
        door(c, x1, y0, cz, false, flags, 4);
        door(c, x0, y0, cz, false, flags, 8);
        int roofTop = top;
        if (!wrecked) {
            switch (roof) {
                case ROOF_PEAKED -> { peakedRoof(c, x0, z0, x1, z1, top + 1); roofTop = top + half + 3; }
                case ROOF_CONICAL -> { roofTop = conicalRoof(c, cx, cz, half, top); }
                default -> {
                    crenellate(c, x0, z0, x1, z1, top);
                    for (int x : new int[]{x0, x1}) for (int z : new int[]{z0, z1}) brazier(c, x, top + 3, z);
                    roofTop = top + 4;
                }
            }
        }
        if (damaged) c.erode(x0 - 3, y0, z0 - 3, x1 + 3, roofTop + 2, z1 + 3, y0 + (top - y0) / 3, roofTop, 1.25);
        if (wrecked) {
            c.erode(x0 - 1, y0, z0 - 1, x1 + 1, top + 1, z1 + 1, y0 + 2, y0 + 12, 1.0);

            for (int i = 0; i < size * 2; i++) {
                long h = c.hash(cx + i, y0, cz - i);
                int rx = cx + (int) Math.floorMod(h, (long) (size + 8)) - half - 4;
                int rz = cz + (int) Math.floorMod(h >>> 16, (long) (size + 8)) - half - 4;
                if (rx >= x0 && rx <= x1 && rz >= z0 && rz <= z1) continue;
                c.set(rx, y0, rz, c.wood(rx, y0, rz));
                if ((h & 3) == 0) c.set(rx, y0 + 1, rz, c.wood(rx, y0 + 1, rz));
            }
        }
    }

    public static int conicalRoof(CastleCanvas c, int cx, int cz, int half, int top) {
        double R0 = half * 1.42 + 1.2;
        int H = (int) Math.round(R0 * 2.3);
        c.fillWood(cx - half, top, cz - half, cx + half, top, cz + half);
        for (int l = 0; l <= H; l++) {
            double r = R0 * Math.pow(1.0 - (double) l / (H + 1), 1.35);
            if (r < 0.6) break;
            int ri = (int) Math.ceil(r);
            int y = top + 1 + l;
            for (int dx = -ri; dx <= ri; dx++) for (int dz = -ri; dz <= ri; dz++) {
                double d2 = dx * dx + dz * dz;
                if (d2 > r * r + 0.5) continue;
                boolean rim = d2 > (r - 1.1) * (r - 1.1);
                boolean rib = rim && Math.abs(Math.abs(dx) - Math.abs(dz)) <= 1;
                if ((l == 0 && rim) || rib) c.set(cx + dx, y, cz + dz, encased());
                else if (rim || l == 0) c.set(cx + dx, y, cz + dz, c.wood(cx + dx, y, cz + dz));
            }

            if (l == 0) for (int dx = -ri; dx <= ri; dx++) for (int dz = -ri; dz <= ri; dz++) {
                double d2 = dx * dx + dz * dz;
                if (d2 <= r * r + 0.5 && d2 > (r - 1.1) * (r - 1.1) && (Math.abs(dx) > half || Math.abs(dz) > half)) c.set(cx + dx, top, cz + dz, encased());
            }
        }
        int tip = top + 1 + H;
        c.fill(cx, tip - 2, cz, cx, tip + 2, cz, encased());
        brazier(c, cx, tip + 3, cz);
        return tip + 5;
    }

    public static void beard(CastleCanvas c, int cx, int y0, int cz, int half) {
        for (int l = 1; l <= half; l++) {
            int r = half - l;
            c.fillWood(cx - r, y0 - l, cz - r, cx + r, y0 - l, cz + r);
            for (int[] q : new int[][]{{-r, -r}, {r, -r}, {-r, r}, {r, r}}) c.set(cx + q[0], y0 - l, cz + q[1], encased());
        }
        for (int d = 1; d <= 3; d++) c.set(cx, y0 - half - d, cz, chain());
        c.set(cx, y0 - half - 4, cz, lantern());
    }

    public static void plaza(CastleCanvas c, int cx, int y0, int cz, int half) {
        int x0 = cx - half, x1 = cx + half, z0 = cz - half, z1 = cz + half;
        c.foundation(x0, z0, x1, z1, y0 - 1, 4);
        c.fillFn(x0, y0, z0, x1, y0, z1, (x, y, z) ->
                (x == x0 || x == x1 || z == z0 || z == z1) ? encased() : (((x + z) & 1) == 0 ? floorStone() : c.wood(x, y, z)));
        crenellate(c, x0, z0, x1, z1, y0);
        c.air(cx - 7, y0 + 1, z1, cx + 7, y0 + 2, z1);
        for (int x : new int[]{x0, x1}) for (int z : new int[]{z0, z1}) brazier(c, x, y0 + 3, z);

        for (int t = -half + 6; t <= half - 6; t += 8) {
            c.foundation(cx + t, z0 - 1, cx + t, z0 - 1, y0 - 1, 1);
            c.foundation(cx + t, z1 + 1, cx + t, z1 + 1, y0 - 1, 1);
            c.foundation(x0 - 1, cz + t, x0 - 1, cz + t, y0 - 1, 1);
            c.foundation(x1 + 1, cz + t, x1 + 1, cz + t, y0 - 1, 1);
        }
    }

    public static void shaft(CastleCanvas c, int cx, int cz, int yBottom, int yTop, int phaseY) {
        c.fillWood(cx - 3, yBottom + 1, cz - 3, cx + 3, yTop, cz + 3);
        c.air(cx - 2, yBottom + 1, cz - 2, cx + 2, yTop - 1, cz + 2);
        c.air(cx - 1, yTop, cz - 1, cx + 1, yTop, cz + 1);
        c.air(cx - 1, yBottom + 1, cz - 3, cx + 1, yBottom + 3, cz + 3);
        c.air(cx - 3, yBottom + 1, cz - 1, cx + 3, yBottom + 3, cz + 1);
        spiral(c, cx, cz, yBottom + 1, yTop, phaseY);
    }

    private static final net.minecraft.resources.ResourceLocation LOOT_TREASURE =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("oversaturatedgrowth", "chests/castle_treasure");
    private static final net.minecraft.resources.ResourceLocation LOOT_BARREL =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("oversaturatedgrowth", "chests/castle_barrel");
    private static final net.minecraft.resources.ResourceLocation LOOT_COMMON =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("oversaturatedgrowth", "chests/lard_vault");

    public static void maze(CastleCanvas c, int cx, int yTop, int cz, int n, int levels) {
        int W = n * 4 + 1, x0 = cx - W / 2, z0 = cz - W / 2;
        int yBottom = yTop - (levels - 1) * 8;
        c.fillFn(x0, yBottom, z0, x0 + W - 1, yTop + 6, z0 + W - 1, c::brick);
        for (int L = 0; L < levels; L++) {
            int fy = yTop - L * 8;
            net.minecraft.util.RandomSource r = net.minecraft.util.RandomSource.create(c.seed * 31 + L * 7919L);
            boolean[][] east = new boolean[n][n], south = new boolean[n][n];
            boolean[][] seen = new boolean[n][n];
            java.util.ArrayDeque<int[]> st = new java.util.ArrayDeque<>();
            st.push(new int[]{r.nextInt(n), r.nextInt(n)});
            seen[st.peek()[0]][st.peek()[1]] = true;
            int[][] D = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            while (!st.isEmpty()) {
                int[] cur = st.peek();
                int[] order = {0, 1, 2, 3};
                for (int i = 3; i > 0; i--) { int j = r.nextInt(i + 1); int t = order[i]; order[i] = order[j]; order[j] = t; }
                boolean moved = false;
                for (int k : order) {
                    int ni = cur[0] + D[k][0], nj = cur[1] + D[k][1];
                    if (ni < 0 || nj < 0 || ni >= n || nj >= n || seen[ni][nj]) continue;
                    open(east, south, cur[0], cur[1], ni, nj);
                    seen[ni][nj] = true; st.push(new int[]{ni, nj}); moved = true; break;
                }
                if (!moved) st.pop();
            }

            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
                if (r.nextInt(10) != 0) continue;
                if (r.nextBoolean() && i + 1 < n) east[i][j] = true; else if (j + 1 < n) south[i][j] = true;
            }

            java.util.List<int[]> rooms = new java.util.ArrayList<>();
            if (L == 0) rooms.add(new int[]{n / 2 - 1, n / 2 - 1, -1});
            int want = 4 + r.nextInt(3) + (L > 0 ? 2 : 0);
            for (int tries = 0; tries < 60 && rooms.size() < want + (L == 0 ? 1 : 0); tries++) {
                int ri = r.nextInt(n - 3), rj = r.nextInt(n - 3);
                boolean ok = true;
                for (int[] o : rooms) if (Math.abs(o[0] - ri) < 5 && Math.abs(o[1] - rj) < 5) { ok = false; break; }
                if (ok) rooms.add(new int[]{ri, rj, r.nextInt(4)});
            }

            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
                int bx = x0 + 1 + i * 4, bz = z0 + 1 + j * 4;
                c.air(bx, fy + 1, bz, bx + 2, fy + 5, bz + 2);
                if (east[i][j]) c.air(bx + 3, fy + 1, bz, bx + 3, fy + 5, bz + 2);
                if (south[i][j]) c.air(bx, fy + 1, bz + 3, bx + 2, fy + 5, bz + 3);
                c.set(bx + 1, fy, bz + 1, floorStone());
            }

            for (int[] rm : rooms) {
                int ax = x0 + 1 + rm[0] * 4, az = z0 + 1 + rm[1] * 4, bx = ax + 10, bz = az + 10;
                c.air(ax, fy + 1, az, bx, fy + 5, bz);
                c.fill(ax, fy, az, bx, fy, bz, floorStone());
                for (int[] q : new int[][]{{ax, az}, {bx, az}, {ax, bz}, {bx, bz}}) c.set(q[0], fy + 5, q[1], lantern());
                int mx = ax + 5, mz = az + 5;
                switch (rm[2]) {
                    case 0 -> {
                        c.chest(ax + 1, fy + 1, mz, net.minecraft.core.Direction.EAST, LOOT_TREASURE);
                        c.chest(bx - 1, fy + 1, mz, net.minecraft.core.Direction.WEST, LOOT_TREASURE);
                        c.spawner(mx, fy + 1, mz, com.eisiadev.oversaturatedgrowth.init.ModEntities.LARD_CRAWLER.get());
                    }
                    case 1 -> {
                        c.spawner(mx - 2, fy + 1, mz, com.eisiadev.oversaturatedgrowth.init.ModEntities.LARDSTONE_ARCHER.get());
                        c.spawner(mx + 2, fy + 1, mz, com.eisiadev.oversaturatedgrowth.init.ModEntities.LARDSTONE_ARCHER.get());
                        c.chest(mx, fy + 1, az + 1, net.minecraft.core.Direction.SOUTH, LOOT_COMMON);
                    }
                    case 2 -> {
                        c.fill(mx - 2, fy, mz - 2, mx + 2, fy, mz + 2, ModBlocks.LARD_BLOCK.get().defaultBlockState());
                        c.fill(mx, fy + 1, mz, mx, fy + 4, mz, ModBlocks.LARD_SLUDGE_BLOCK.get().defaultBlockState());
                        brazier(c, ax + 1, fy + 1, az + 1); brazier(c, bx - 1, fy + 1, bz - 1);
                    }
                    case 3 -> {
                        c.chest(mx, fy + 1, mz, net.minecraft.core.Direction.NORTH, LOOT_TREASURE);
                        c.mob(ax + 2, fy + 1, az + 2, com.eisiadev.oversaturatedgrowth.init.ModEntities.MAW_CHEST.get());
                        c.mob(bx - 2, fy + 1, az + 2, com.eisiadev.oversaturatedgrowth.init.ModEntities.MAW_CHEST.get());
                        c.mob(ax + 2, fy + 1, bz - 2, com.eisiadev.oversaturatedgrowth.init.ModEntities.MAW_CHEST.get());
                    }
                    default -> { }
                }
            }

            if (L > 0) {
                for (int k = 0; k < 6; k++) {
                    int i = r.nextInt(n), j = r.nextInt(n);
                    int bx = x0 + 1 + i * 4, bz = z0 + 1 + j * 4;
                    c.air(bx, fy + 6, bz, bx, fy + 8, bz);
                    for (int y = fy + 1; y <= fy + 8; y++)
                        c.set(bx, y, bz, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, net.minecraft.core.Direction.EAST));
                }
            }
        }
    }

    private static void open(boolean[][] east, boolean[][] south, int i, int j, int ni, int nj) {
        if (ni == i + 1) east[i][j] = true;
        else if (ni == i - 1) east[ni][nj] = true;
        else if (nj == j + 1) south[i][j] = true;
        else south[ni][nj] = true;
    }

    private static void door(CastleCanvas c, int fx, int y0, int fz, boolean alongX, int mask, int bit) {
        if ((mask & bit) == 0) return;
        c.air(fx - 2, y0 + 1, fz - 2, fx + 2, y0 + 6, fz + 2);
        if (alongX) c.fill(fx - 2, y0 + 1, fz, fx + 2, y0 + 6, fz, reappearing());
        else c.fill(fx, y0 + 1, fz - 2, fx, y0 + 6, fz + 2, reappearing());
    }

    public static void peakedRoof(CastleCanvas c, int x0, int z0, int x1, int z1, int yBase) {
        int layers = (x1 - x0) / 2 + 2;
        for (int l = 0; l < layers; l++) {
            int ax = x0 - 1 + l, bx = x1 + 1 - l, az = z0 - 1 + l, bz = z1 + 1 - l;
            if (ax > bx || az > bz) break;
            c.fillWood(ax, yBase + l, az, bx, yBase + l, bz);
            for (int[] q : new int[][]{{ax, az}, {bx, az}, {ax, bz}, {bx, bz}}) c.set(q[0], yBase + l, q[1], encased());
            if (l == layers - 1 || ax == bx) { brazier(c, (x0 + x1) / 2, yBase + l + 1, (z0 + z1) / 2); }
        }
    }

    public static void keep(CastleCanvas c, int cx, int y0, int cz, int size, int phaseY) {
        int half = size / 2, x0 = cx - half, x1 = cx + half, z0 = cz - half, z1 = cz + half, H = 36, top = y0 + H - 1;
        c.foundation(x0, z0, x1, z1, y0 - 1, 4);
        c.shell(x0, y0, z0, x1, top, z1, 3);
        cornerPosts(c, x0, z0, x1, z1, y0, top);
        c.edges(x0, y0, z0, x1, top, z1);

        for (int x = x0 + 3; x <= x1 - 3; x++) for (int z = z0 + 3; z <= z1 - 3; z++) {
            boolean dark = ((x + z) & 1) == 0;
            c.set(x, y0, z, dark ? floorStone() : encased());
        }
        c.air(cx - 1, y0, cz - 1, cx + 1, y0, cz + 1);

        c.fillWood(x0 + 3, y0 + 24, z0 + 3, x1 - 3, y0 + 24, z1 - 3);
        c.air(cx - 1, y0 + 24, cz - 1, cx + 1, y0 + 24, cz + 1);

        int[][] pil = {{-14, -14}, {14, -14}, {-14, 14}, {14, 14}, {0, -14}, {0, 14}, {-14, 0}, {14, 0}};
        for (int[] p : pil) {
            c.fill(cx + p[0] - 1, y0 + 1, cz + p[1] - 1, cx + p[0] + 1, y0 + 23, cz + p[1] + 1, encased());
            brazier(c, cx + p[0] + 2, y0 + 1, cz + p[1] + 2);
        }

        for (int tx : new int[]{-7, 7}) {
            for (int z = cz - 17; z <= cz + 17; z++) {
                if (Math.abs(z - cz) < 7 && Math.abs(tx) < 8) continue;
                c.set(cx + tx, y0 + 1, z, planks());
                c.set(cx + tx - 2, y0 + 1, z, slab());
                c.set(cx + tx + 2, y0 + 1, z, slab());
                if (Math.floorMod(z, 4) == 0) c.set(cx + tx, y0 + 2, z, Blocks.CAKE.defaultBlockState());
            }
        }

        for (int gx = -10; gx <= 10; gx += 10) for (int gz = -10; gz <= 10; gz += 10) {
            if (gx == 0 && gz == 0) continue;
            for (int dy = 0; dy < 5; dy++) c.set(cx + gx, y0 + 23 - dy, cz + gz, chain());
            c.set(cx + gx, y0 + 18, cz + gz, lantern());
        }

        int xi = cx - 7, xj = cx + 7, zi = cz - 7, zj = cz + 7;
        int fy = y0 + 25, fh = 10;
        rectRooms(c, x0 + 3, z0 + 3, x1 - 3, zi - 1, fy, fh, 0, cx, true);
        rectRooms(c, x0 + 3, zj + 1, x1 - 3, z1 - 3, fy, fh, 1, cx, true);
        rectRooms(c, x0 + 3, zi, xi - 1, zj, fy, fh, 2, cz, false);
        rectRooms(c, xj + 1, zi, x1 - 3, zj, fy, fh, 3, cz, false);

        for (int o : new int[]{6, 12, 18}) for (int sg : new int[]{-1, 1}) {
            slit(c, true, cx + sg * o, y0 + 6, z0, 1, 3, 13, 3, true);
            if (o > 6) slit(c, true, cx + sg * o, y0 + 6, z1, -1, 3, 13, 3, true);
            slit(c, false, cz + sg * o, y0 + 6, x0, 1, 3, 13, 3, true);
            slit(c, false, cz + sg * o, y0 + 6, x1, -1, 3, 13, 3, true);
        }

        c.air(cx - 4, y0 + 1, z1 - 3, cx + 4, y0 + 12, z1);
        c.fill(cx - 5, y0 + 1, z1, cx - 5, y0 + 13, z1, encased()); c.fill(cx + 5, y0 + 1, z1, cx + 5, y0 + 13, z1, encased());
        c.fill(cx - 5, y0 + 13, z1, cx + 5, y0 + 13, z1, encased());

        spiral(c, cx, cz, y0 + 1, top - 1, phaseY);

        pillarSpawners(c, cx, cz, y0 + 24, top, 100);

        for (int[] sp : new int[][]{{12, 8}, {12, -8}, {-12, 8}, {-12, -8}, {3, 19}, {-3, 19}, {3, -19}, {-3, -19}, {20, 0}, {-20, 0}, {9, 0}, {-9, 0}, {20, 14}, {-20, -14}})
            c.mobSpawner(cx + sp[0], y0 + 1, cz + sp[1], CASTLE_MOBS);

        c.fillWood(x0, top, z0, x1, top, z1);
        c.air(cx - 1, top, cz - 1, cx + 1, top, cz + 1);
        crenellate(c, x0, z0, x1, z1, top);
        for (int x : new int[]{x0, x1}) for (int z : new int[]{z0, z1}) brazier(c, x, top + 3, z);
    }

    private static void rectRooms(CastleCanvas c, int x0, int z0, int x1, int z1, int y, int h, int inner, int mid, boolean alongX) {

        switch (inner) {
            case 0 -> { c.fillWood(x0, y, z1, x1, y + h - 1, z1); c.air(mid - 1, y, z1, mid + 1, y + 3, z1); }
            case 1 -> { c.fillWood(x0, y, z0, x1, y + h - 1, z0); c.air(mid - 1, y, z0, mid + 1, y + 3, z0); }
            case 2 -> { c.fillWood(x1, y, z0, x1, y + h - 1, z1); c.air(x1, y, mid - 1, x1, y + 3, mid + 1); }
            default -> { c.fillWood(x0, y, z0, x0, y + h - 1, z1); c.air(x0, y, mid - 1, x0, y + 3, mid + 1); }
        }
        bsp(c, x0, z0, x1, z1, y, h, 0, c.hash(x0, y, z0));
    }

    private static void bsp(CastleCanvas c, int x0, int z0, int x1, int z1, int y, int h, int depth, long salt) {
        int w = x1 - x0 + 1, d = z1 - z0 + 1;
        if (depth > 5 || (w <= 13 && d <= 13)) { furnish(c, x0, z0, x1, z1, y, salt); return; }
        boolean splitX = w >= d;
        long r = salt * 6364136223846793005L + 1442695040888963407L;
        if (splitX) {
            int span = w - 10; if (span < 1) { furnish(c, x0, z0, x1, z1, y, salt); return; }
            int m = x0 + 5 + (int) Math.floorMod(r >>> 8, (long) span);
            c.fillWood(m, y, z0, m, y + h - 1, z1);
            int dz = z0 + 2 + (int) Math.floorMod(r >>> 20, (long) Math.max(1, d - 4));
            c.air(m, y, dz - 1, m, y + 3, dz + 1); bspDoor(c, true, m, dz, y);
            bsp(c, x0, z0, m - 1, z1, y, h, depth + 1, r ^ 0x1111);
            bsp(c, m + 1, z0, x1, z1, y, h, depth + 1, r ^ 0x2222);
        } else {
            int span = d - 10; if (span < 1) { furnish(c, x0, z0, x1, z1, y, salt); return; }
            int m = z0 + 5 + (int) Math.floorMod(r >>> 8, (long) span);
            c.fillWood(x0, y, m, x1, y + h - 1, m);
            int dx = x0 + 2 + (int) Math.floorMod(r >>> 20, (long) Math.max(1, w - 4));
            c.air(dx - 1, y, m, dx + 1, y + 3, m); bspDoor(c, false, m, dx, y);
            bsp(c, x0, z0, x1, m - 1, y, h, depth + 1, r ^ 0x3333);
            bsp(c, x0, m + 1, x1, z1, y, h, depth + 1, r ^ 0x4444);
        }
    }

    private static void bspDoor(CastleCanvas c, boolean axisX, int m, int a, int y) {
        BlockState e = encased();
        if (axisX) {
            c.fill(m, y, a - 1, m, y + 3, a + 1, reappearing());
            c.fill(m, y, a - 2, m, y + 4, a - 2, e); c.fill(m, y, a + 2, m, y + 4, a + 2, e); c.fill(m, y + 4, a - 1, m, y + 4, a + 1, e);
        } else {
            c.fill(a - 1, y, m, a + 1, y + 3, m, reappearing());
            c.fill(a - 2, y, m, a - 2, y + 4, m, e); c.fill(a + 2, y, m, a + 2, y + 4, m, e); c.fill(a - 1, y + 4, m, a + 1, y + 4, m, e);
        }
    }

    private static void furnish(CastleCanvas c, int x0, int z0, int x1, int z1, int y, long salt) {
        c.set((x0 + x1) / 2, y + 9, (z0 + z1) / 2, lantern());
        c.mobSpawner((x0 + x1) / 2, y, (z0 + z1) / 2, CASTLE_MOBS);
        if (x1 - x0 >= 9 && z1 - z0 >= 9) c.mobSpawner(x0 + 2, y, z1 - 2, CASTLE_MOBS);
        if (x1 - x0 >= 4 && z1 - z0 >= 4)
            furnishRoom(c, x0 + 1, z0 + 1, x1 - 1, z1 - 1, y, Integer.MIN_VALUE, Integer.MIN_VALUE, (int) Math.floorMod(salt >>> 3, 7L));
    }

    public static void furnishRoom(CastleCanvas c, int ix0, int iz0, int ix1, int iz1, int y, int cx, int cz, int theme) {
        net.minecraft.core.Direction S = net.minecraft.core.Direction.SOUTH, N = net.minecraft.core.Direction.NORTH,
                E = net.minecraft.core.Direction.EAST, Wd = net.minecraft.core.Direction.WEST;
        java.util.List<int[]> ring = new java.util.ArrayList<>();
        for (int x = ix0; x <= ix1; x++) { ring.add(new int[]{x, iz0, 0}); ring.add(new int[]{x, iz1, 1}); }
        for (int z = iz0 + 1; z < iz1; z++) { ring.add(new int[]{ix0, z, 2}); ring.add(new int[]{ix1, z, 3}); }
        net.minecraft.core.Direction[] fd = {S, N, E, Wd};
        int i = 0;
        for (int[] q : ring) {
            i++;
            int x = q[0], z = q[1];
            if (cx != Integer.MIN_VALUE && Math.abs(x - cx) <= 2 && Math.abs(z - cz) <= 2) continue;
            int mx = (ix0 + ix1) / 2, mz = (iz0 + iz1) / 2;
            if (Math.abs(x - mx) <= 2 || Math.abs(z - mz) <= 2) continue;
            long h = c.hash(x, y, z);
            int r = (int) Math.floorMod(h, 100L);
            BlockState st = null, st2 = null;
            switch (theme) {
                case 0 -> { st = Blocks.BOOKSHELF.defaultBlockState(); st2 = r < 60 ? Blocks.BOOKSHELF.defaultBlockState() : null; }
                case 1 -> { st = r < 45 ? Blocks.BARREL.defaultBlockState() : r < 70 ? ModBlocks.LARD_BLOCK.get().defaultBlockState() : r < 85 ? planks() : null;
                            if (st == planks()) st2 = Blocks.CAKE.defaultBlockState(); }
                case 2 -> { st = r < 30 ? Blocks.SMOKER.defaultBlockState().setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, fd[q[2]])
                                : r < 50 ? Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, fd[q[2]])
                                : r < 62 ? Blocks.CAULDRON.defaultBlockState() : r < 72 ? Blocks.CRAFTING_TABLE.defaultBlockState() : r < 88 ? Blocks.BARREL.defaultBlockState() : null; }
                case 3 -> { if (r < 35) { st = planks(); st2 = r < 15 ? Blocks.CAKE.defaultBlockState() : null; } }
                case 4 -> { if (r < 10) { c.chest(x, y, z, fd[q[2]], LOOT_COMMON); continue; }
                            st = r < 55 ? Blocks.BARREL.defaultBlockState() : r < 70 ? c.wood(x, y, z) : null; }
                case 5 -> { st = r < 20 ? c.wood(x, y, z) : null; if (r > 90) c.set(x, y + 5, z, Blocks.COBWEB.defaultBlockState()); }
                default -> { st = r < 40 ? ModBlocks.LARDY_WOOL.get().defaultBlockState() : r < 60 ? Blocks.BARREL.defaultBlockState() : null; }
            }
            if (st != null) { if (st.is(Blocks.BARREL)) c.barrel(x, y, z, LOOT_BARREL); else c.set(x, y, z, st); }
            if (st2 != null) c.set(x, y + 1, z, st2);
        }

        if (theme == 3 && ix1 - ix0 >= 8) {
            for (int z : new int[]{iz0 + 3, iz1 - 3}) for (int x = ix0 + 2; x <= ix1 - 2; x++) {
                if (cx != Integer.MIN_VALUE && Math.abs(x - cx) <= 2 && Math.abs(z - cz) <= 2) continue;
                c.set(x, y, z, planks());
                if (Math.floorMod(x, 3) == 0) c.set(x, y + 1, z, Blocks.CAKE.defaultBlockState());
            }
        }
    }

    public static void spire(CastleCanvas c, int cx, int yBase, int cz, int size, int height, long sealMask, int phaseY) {
        int half = size / 2, x0 = cx - half, x1 = cx + half, z0 = cz - half, z1 = cz + half, top = yBase + height - 1;
        int arenaFloor = top - 3 * F;
        c.shell(x0, yBase, z0, x1, top, z1, 3);
        c.air(x0 + 3, yBase, z0 + 3, x1 - 3, yBase, z1 - 3);

        for (int i = 1; yBase + i * F < arenaFloor; i++) {
            int y = yBase + i * F;
            c.fillWood(x0 + 3, y, z0 + 3, x1 - 3, y, z1 - 3);
            if (i >= 64 || ((sealMask >>> i) & 1L) == 0) c.air(cx - 1, y, cz - 1, cx + 1, y, cz + 1);
        }
        c.edges(x0, yBase, z0, x1, top, z1);

        for (int t = -1; t <= 1; t++) {
            c.fill(cx + t, yBase, z0 - 1, cx + t, top, z0 - 1, encased()); c.fill(cx + t, yBase, z1 + 1, cx + t, top, z1 + 1, encased());
            c.fill(x0 - 1, yBase, cz + t, x0 - 1, top, cz + t, encased()); c.fill(x1 + 1, yBase, cz + t, x1 + 1, top, cz + t, encased());
        }
        for (int y = yBase + 3 * F; y < arenaFloor; y += 3 * F) {
            c.fill(x0, y, z0, x1, y, z0, encased()); c.fill(x0, y, z1, x1, y, z1, encased());
            c.fill(x0, y, z0, x0, y, z1, encased()); c.fill(x1, y, z0, x1, y, z1, encased());
        }
        windows(c, x0, z0, x1, z1, yBase, arenaFloor, 2, 3);
        lanterns(c, cx, cz, half - 3, yBase, arenaFloor);
        for (int k = 0; (yBase + k * F) < arenaFloor; k++)
            furnishRoom(c, x0 + 3, z0 + 3, x1 - 3, z1 - 3, yBase + k * F + 1, cx, cz, (int) Math.floorMod(c.hash(cx, yBase + k, cz), 7L));

        c.fill(x0 + 3, arenaFloor, z0 + 3, x1 - 3, arenaFloor, z1 - 3, floorStone());
        c.air(x0 + 3, arenaFloor + 1, z0 + 3, x1 - 3, top - 1, z1 - 3);
        c.fill(cx - 6, arenaFloor, cz - 6, cx + 6, arenaFloor, cz + 6, ModBlocks.POLISHED_LARDSTONE.get().defaultBlockState());

        c.mob(cx + 4, arenaFloor + 1, cz + 3, com.eisiadev.oversaturatedgrowth.init.ModEntities.OVERLARD.get());
        int ah = 3 * F - 4, aw = half - 5;
        for (int t = -aw; t <= aw; t += aw) {
            c.air(cx + t - 2, arenaFloor + 2, z0, cx + t + 2, arenaFloor + ah, z0 + 2); c.fill(cx + t - 2, arenaFloor + 1, z0, cx + t + 2, arenaFloor + 1, z0, bars(true));
            c.air(cx + t - 2, arenaFloor + 2, z1 - 2, cx + t + 2, arenaFloor + ah, z1); c.fill(cx + t - 2, arenaFloor + 1, z1, cx + t + 2, arenaFloor + 1, z1, bars(true));
            c.air(x0, arenaFloor + 2, cz + t - 2, x0 + 2, arenaFloor + ah, cz + t + 2); c.fill(x0, arenaFloor + 1, cz + t - 2, x0, arenaFloor + 1, cz + t + 2, bars(false));
            c.air(x1 - 2, arenaFloor + 2, cz + t - 2, x1, arenaFloor + ah, cz + t + 2); c.fill(x1, arenaFloor + 1, cz + t - 2, x1, arenaFloor + 1, cz + t + 2, bars(false));
        }
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            int px = cx + sx * (half - 6), pz = cz + sz * (half - 6);
            c.fill(px - 1, arenaFloor + 1, pz - 1, px + 1, top - 1, pz + 1, encased());
            brazier(c, px + sx * 2, arenaFloor + 1, pz + sz * 2);
        }
        for (int gx = -6; gx <= 6; gx += 6) for (int gz = -6; gz <= 6; gz += 6) {
            if (gx == 0 && gz == 0) continue;
            for (int dy = 1; dy <= 6; dy++) c.set(cx + gx, top - dy, cz + gz, chain());
            c.set(cx + gx, top - 7, cz + gz, lantern());
        }

        for (int i = 0; yBase + i * F < arenaFloor; i++) {
            int yA = yBase + i * F + 1, yB = Math.min(yBase + (i + 1) * F, arenaFloor);
            boolean sealedAbove = (i + 1) < 64 && ((sealMask >>> (i + 1)) & 1L) != 0;
            if (!sealedAbove) spiral(c, cx, cz, yA, yB, phaseY);
            else c.fill(cx, yA, cz, cx, yB - 1, cz, encased());
            if (Math.floorMod(c.hash(cx, yA, cz) >>> 5, 100L) < 80) c.mobSpawner(cx, yA + 1, cz, CASTLE_MOBS);
        }
        c.air(cx - 1, arenaFloor, cz - 1, cx + 1, arenaFloor, cz + 1);
        c.set(cx + RING[Math.floorMod(arenaFloor - phaseY, 8)][0], arenaFloor, cz + RING[Math.floorMod(arenaFloor - phaseY, 8)][1], plain());
        c.fill(cx, yBase + 1, cz, cx, arenaFloor, cz, encased());

        conicalRoof(c, cx, cz, half + 1, top);
        for (int x : new int[]{x0 - 1, x1 + 1}) for (int z : new int[]{z0 - 1, z1 + 1}) brazier(c, x, top + 1, z);
    }

    public static int spireHeight(int size, int yBase, int maxY) {
        int cone = (int) Math.round((size / 2 + 3.5) * 2.0) + 6;
        int h = maxY - 6 - cone - yBase;
        h = Math.max(64, Math.min(420, h));
        return (h / F) * F + 1;
    }

    public static void bridge(CastleCanvas c, boolean axisX, int a0, int a1, int perp, int y, int phaseY) { bridge(c, axisX, a0, a1, perp, y, phaseY, 0); }

    public static void bridge(CastleCanvas c, boolean axisX, int a0, int a1, int perp, int y, int phaseY, int variant) {
        int lo = Math.min(a0, a1), hi = Math.max(a0, a1);
        int pierMask = variant >>> 4; variant &= 15;
        if (variant == 1) {
            if (axisX) {
                c.fillWood(lo, y, perp - 3, hi, y, perp + 3);
                c.fill(lo, y, perp - 3, hi, y, perp - 3, encased()); c.fill(lo, y, perp + 3, hi, y, perp + 3, encased());
                c.air(lo, y + 1, perp - 2, hi, y + 6, perp + 2);
                crenellateLine(c, true, lo, hi, perp - 3, y); crenellateLine(c, true, lo, hi, perp + 3, y);
                for (int x = lo + 5, k = 0; x < hi - 4; x += 10, k++) if (((pierMask >>> k) & 1) != 0) c.foundation(x - 1, perp - 1, x + 1, perp + 1, y - 1, 1);
                c.air(lo - 4, y + 1, perp - 2, lo + 1, y + 6, perp + 2); c.air(hi - 1, y + 1, perp - 2, hi + 4, y + 6, perp + 2);
            doorFrame(c, true, lo - 1, perp, y); doorFrame(c, true, hi + 1, perp, y);
            } else {
                c.fillWood(perp - 3, y, lo, perp + 3, y, hi);
                c.fill(perp - 3, y, lo, perp - 3, y, hi, encased()); c.fill(perp + 3, y, lo, perp + 3, y, hi, encased());
                c.air(perp - 2, y + 1, lo, perp + 2, y + 6, hi);
                crenellateLine(c, false, lo, hi, perp - 3, y); crenellateLine(c, false, lo, hi, perp + 3, y);
                for (int z = lo + 5, k = 0; z < hi - 4; z += 10, k++) if (((pierMask >>> k) & 1) != 0) c.foundation(perp - 1, z - 1, perp + 1, z + 1, y - 1, 1);
                c.air(perp - 2, y + 1, lo - 4, perp + 2, y + 6, lo + 1); c.air(perp - 2, y + 1, hi - 1, perp + 2, y + 6, hi + 4);
            doorFrame(c, false, lo - 1, perp, y); doorFrame(c, false, hi + 1, perp, y);
            }
            return;
        }
        if (axisX) {
            c.shell(lo, y, perp - 3, hi, y + 7, perp + 3, 1);
            c.edges(lo, y, perp - 3, hi, y + 7, perp + 3);
            for (int x = lo + 2; x <= hi - 2; x += 3) for (int dy = 3; dy <= 4; dy++) { c.set(x, y + dy, perp - 3, bars(true)); c.set(x, y + dy, perp + 3, bars(true)); }
            for (int x = lo + 5, k = 0; x < hi - 4; x += 10, k++) if (((pierMask >>> k) & 1) != 0) c.foundation(x - 1, perp - 1, x + 1, perp + 1, y - 1, 1);
            for (int x = lo; x <= hi; x += 6) c.set(x, y + 6, perp, lantern());
            c.air(lo - 4, y + 1, perp - 2, lo + 1, y + 6, perp + 2); c.air(hi - 1, y + 1, perp - 2, hi + 4, y + 6, perp + 2);
            doorFrame(c, true, lo - 1, perp, y); doorFrame(c, true, hi + 1, perp, y);
        } else {
            c.shell(perp - 3, y, lo, perp + 3, y + 7, hi, 1);
            c.edges(perp - 3, y, lo, perp + 3, y + 7, hi);
            for (int z = lo + 2; z <= hi - 2; z += 3) for (int dy = 3; dy <= 4; dy++) { c.set(perp - 3, y + dy, z, bars(false)); c.set(perp + 3, y + dy, z, bars(false)); }
            for (int z = lo + 5, k = 0; z < hi - 4; z += 10, k++) if (((pierMask >>> k) & 1) != 0) c.foundation(perp - 1, z - 1, perp + 1, z + 1, y - 1, 1);
            for (int z = lo; z <= hi; z += 6) c.set(perp, y + 6, z, lantern());
            c.air(perp - 2, y + 1, lo - 4, perp + 2, y + 6, lo + 1); c.air(perp - 2, y + 1, hi - 1, perp + 2, y + 6, hi + 4);
            doorFrame(c, false, lo - 1, perp, y); doorFrame(c, false, hi + 1, perp, y);
        }
    }

    private static void crenellateLine(CastleCanvas c, boolean axisX, int lo, int hi, int perp, int y) {
        for (int a = lo; a <= hi; a++) {
            int x = axisX ? a : perp, z = axisX ? perp : a;
            c.set(x, y + 1, z, c.wood(x, y + 1, z));
            if ((a & 1) == 0 && Math.floorMod(c.hash(x, y, z), 9L) != 0) c.set(x, y + 2, z, c.wood(x, y + 2, z));
        }
    }

    public static void gateStairs(CastleCanvas c, int cx, int zStart, int y0, int width, int steps) {
        int hw = width / 2;
        for (int s = 0; s < steps; s++) {
            int y = y0 - s;
            int z0 = zStart + s * 2, z1 = z0 + 1;
            c.fill(cx - hw, y, z0, cx + hw, y, z1, encased());
            c.foundation(cx - hw, z0, cx + hw, z1, y - 1, 3);
            c.air(cx - hw, y + 1, z0, cx + hw, y + 12, z1);
        }
        for (int s = 0; s < steps; s += 2) { brazier(c, cx - hw - 1, y0 - s + 1, zStart + s * 2); brazier(c, cx + hw + 1, y0 - s + 1, zStart + s * 2); }
    }
}
