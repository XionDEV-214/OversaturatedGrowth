package com.eisiadev.oversaturatedgrowth.client;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.init.ModKeybinds;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BlockConsumeClient {

    private BlockConsumeClient() {}

    private static final Set<Block> BLOCKS = new HashSet<>();
    private static final List<TagKey<Block>> TAGS = new ArrayList<>();

    public static void setData(List<ResourceLocation> blocks, List<ResourceLocation> tags) {
        BLOCKS.clear();
        TAGS.clear();
        for (ResourceLocation id : blocks) {
            Block b = ForgeRegistries.BLOCKS.getValue(id);
            if (b != null) BLOCKS.add(b);
        }
        for (ResourceLocation id : tags) TAGS.add(TagKey.create(Registries.BLOCK, id));
    }

    public static boolean isConsumable(BlockState state) {
        if (BLOCKS.contains(state.getBlock())) return true;
        for (TagKey<Block> t : TAGS) if (state.is(t)) return true;
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) return;
        if (!ModKeybinds.CONSUME_ENTITY.isDown()) return;
        if (!isConsumable(event.getLevel().getBlockState(event.getPos()))) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    private static final double SCATTER_TICKS = 8.0;
    private static final double FLY_TICKS = 12.0;
    private static final double TOTAL_TICKS = SCATTER_TICKS + FLY_TICKS;

    private static final class Visual {
        final BlockPos pos;
        final BlockState state;
        final int playerId;
        final int pieces;
        final long startTick;
        final long seed;
        final EnumMap<Direction, BakedQuad> faces = new EnumMap<>(Direction.class);
        TextureAtlasSprite fallback;

        Visual(BlockPos pos, BlockState state, int playerId, int pieces, long startTick) {
            this.pos = pos;
            this.state = state;
            this.playerId = playerId;
            this.pieces = pieces;
            this.startTick = startTick;
            this.seed = pos.asLong() ^ (startTick * 31L);
        }
    }

    private static final List<Visual> VISUALS = new ArrayList<>();

    public static void addVisual(BlockPos pos, int stateId, int playerId, int pieces) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        BlockState state = Block.stateById(stateId);
        Visual v = new Visual(pos, state, playerId, pieces, mc.level.getGameTime());
        BakedModel model = mc.getBlockRenderer().getBlockModel(state);
        RandomSource rs = RandomSource.create(v.seed);
        v.fallback = model.getParticleIcon(ModelData.EMPTY);
        List<Direction> sides = new ArrayList<>(List.of(Direction.values()));
        sides.add(null);
        for (Direction side : sides) {
            for (BakedQuad q : model.getQuads(state, side, rs, ModelData.EMPTY, null)) {
                v.faces.putIfAbsent(q.getDirection(), q);
            }
        }
        VISUALS.add(v);
    }

    private static final float[][][] CORNERS = new float[6][][];
    private static final Direction[] DIRS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
    static {
        CORNERS[0] = new float[][]{{.5f, -.5f, -.5f}, {-.5f, -.5f, -.5f}, {-.5f, .5f, -.5f}, {.5f, .5f, -.5f}};
        CORNERS[1] = new float[][]{{-.5f, -.5f, .5f}, {.5f, -.5f, .5f}, {.5f, .5f, .5f}, {-.5f, .5f, .5f}};
        CORNERS[2] = new float[][]{{.5f, -.5f, .5f}, {.5f, -.5f, -.5f}, {.5f, .5f, -.5f}, {.5f, .5f, .5f}};
        CORNERS[3] = new float[][]{{-.5f, -.5f, -.5f}, {-.5f, -.5f, .5f}, {-.5f, .5f, .5f}, {-.5f, .5f, -.5f}};
        CORNERS[4] = new float[][]{{-.5f, .5f, .5f}, {.5f, .5f, .5f}, {.5f, .5f, -.5f}, {-.5f, .5f, -.5f}};
        CORNERS[5] = new float[][]{{-.5f, -.5f, -.5f}, {.5f, -.5f, -.5f}, {.5f, -.5f, .5f}, {-.5f, -.5f, .5f}};
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || VISUALS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) { VISUALS.clear(); return; }

        PoseStack ps = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        float pt = event.getPartialTick();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType type = RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer vc = buffers.getBuffer(type);

        Iterator<Visual> it = VISUALS.iterator();
        while (it.hasNext()) {
            Visual v = it.next();
            double t = (mc.level.getGameTime() - v.startTick) + pt;
            Entity eater = mc.level.getEntity(v.playerId);
            if (t >= TOTAL_TICKS) {
                Vec3 at = eater != null ? eater.position() : Vec3.atCenterOf(v.pos);
                mc.level.playLocalSound(at.x, at.y, at.z, (eater instanceof net.minecraft.world.entity.player.Player ? com.eisiadev.oversaturatedgrowth.init.ModSounds.SWALLOW.get() : SoundEvents.STONE_PLACE), SoundSource.PLAYERS, 0.9f, 0.9f + mc.level.random.nextFloat() * 0.2f, false);
                it.remove();
                continue;
            }
            renderVisual(mc, ps, vc, cam, v, t, pt, eater);
        }
        buffers.endBatch(type);
    }

    private static void renderVisual(Minecraft mc, PoseStack ps, VertexConsumer vc, Vec3 cam,
                                     Visual v, double t, float pt, Entity eater) {
        Vec3 center = Vec3.atCenterOf(v.pos);
        Vec3 mouth = center.add(0, 1.5, 0);
        if (eater != null) {
            mouth = eater.getEyePosition(pt);
        }
        Random rnd = new Random(v.seed);
        for (int n = 0; n < v.pieces; n++) {
            int idx = v.pieces >= 8 ? n : (n == 0 ? 0 : 7);
            int ox = idx & 1, oy = (idx >> 1) & 1, oz = (idx >> 2) & 1;
            Vec3 dir = new Vec3(ox == 0 ? -1 : 1, oy == 0 ? -1 : 1, oz == 0 ? -1 : 1).normalize();
            Vec3 jitter = new Vec3(rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian()).scale(0.12);
            Vec3 start = center.add(dir.scale(0.25 / 0.577));
            Vec3 scattered = center.add(dir.scale(0.25 / 0.577 + 0.55)).add(jitter).add(0, 0.12, 0);

            Vec3 pos;
            double scale;
            if (t < SCATTER_TICKS) {
                double x = t / SCATTER_TICKS;
                double e = 1 - Math.pow(1 - x, 3);
                pos = start.add(scattered.subtract(start).scale(e));
                scale = 0.5;
            } else {
                double q = (t - SCATTER_TICKS) / FLY_TICKS;
                double s = q * q * (3 - 2 * q);
                Vec3 ctrl = scattered.add(mouth).scale(0.5).add(0, 0.7, 0).add(jitter);
                double a = (1 - s) * (1 - s), b = 2 * (1 - s) * s, c = s * s;
                pos = scattered.scale(a).add(ctrl.scale(b)).add(mouth.scale(c));
                scale = 0.5 * (1 - 0.75 * s);
            }
            drawCube(mc, ps, vc, cam, v, pos, (float) scale, ox, oy, oz);
        }
    }

    private static void drawCube(Minecraft mc, PoseStack ps, VertexConsumer vc, Vec3 cam, Visual v,
                                 Vec3 pos, float scale, int ox, int oy, int oz) {
        int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(pos));
        ps.pushPose();
        ps.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        ps.scale(scale, scale, scale);
        Matrix4f m = ps.last().pose();
        Matrix3f nm = ps.last().normal();

        for (int f = 0; f < 6; f++) {
            Direction d = DIRS[f];
            BakedQuad q = v.faces.get(d);
            TextureAtlasSprite sprite = q != null ? q.getSprite() : v.fallback;
            float r = 1f, g = 1f, b = 1f;
            if (q != null && q.getTintIndex() >= 0) {
                int c = mc.getBlockColors().getColor(v.state, mc.level, v.pos, q.getTintIndex());
                r = (c >> 16 & 255) / 255f; g = (c >> 8 & 255) / 255f; b = (c & 255) / 255f;
            }
            for (float[] p : CORNERS[f]) {

                float bx = (ox + (p[0] + 0.5f)) * 0.5f, by = (oy + (p[1] + 0.5f)) * 0.5f, bz = (oz + (p[2] + 0.5f)) * 0.5f;
                float u, w;
                switch (d) {
                    case NORTH -> { u = 1 - bx; w = 1 - by; }
                    case SOUTH -> { u = bx; w = 1 - by; }
                    case EAST -> { u = 1 - bz; w = 1 - by; }
                    case WEST -> { u = bz; w = 1 - by; }
                    case UP -> { u = bx; w = bz; }
                    default -> { u = bx; w = 1 - bz; }
                }
                vc.vertex(m, p[0], p[1], p[2]).color(r, g, b, 1f)
                        .uv(sprite.getU(u * 16f), sprite.getV(w * 16f))
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                        .normal(nm, d.getStepX(), d.getStepY(), d.getStepZ()).endVertex();
            }
        }
        ps.popPose();
    }
}
