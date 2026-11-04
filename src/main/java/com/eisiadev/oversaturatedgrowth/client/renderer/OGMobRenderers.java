package com.eisiadev.oversaturatedgrowth.client.renderer;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.client.model.*;
import com.eisiadev.oversaturatedgrowth.entity.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import com.eisiadev.oversaturatedgrowth.client.LardRecolor;
import net.minecraft.resources.ResourceLocation;

public class OGMobRenderers {

    public static final float BLOAT_SCALE = 1.0f;
    public static final float DRIPPER_SCALE = 1.0f;
    public static final float BEETLE_SCALE = 1.0f;
    public static final float SHROOMLING_SCALE = 1.0f;
    public static final float MAW_CHEST_SCALE = 1.0f;
    public static final float CRAWLER_SCALE = 0.9f;

    private static ResourceLocation tex(String n) {
        return ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "textures/mobs/" + n + ".png");
    }

    public static class Bloat extends MobRenderer<LardBloatEntity, LardBloatModel> {
        private static final ResourceLocation T = tex("lard_bloat");
        public Bloat(EntityRendererProvider.Context c) {
            super(c, new LardBloatModel(c.bakeLayer(OGMobModels.BLOAT)), 0.3f);
        }
        @Override protected void scale(LardBloatEntity e, PoseStack ps, float pt) {
            float s = BLOAT_SCALE * e.getBloatScale();
            ps.scale(s, s, s);
        }
        @Override public ResourceLocation getTextureLocation(LardBloatEntity e) { return T; }
    }

    public static class Dripper extends MobRenderer<LardDripperEntity, LardDripperModel> {
        private static final ResourceLocation T = tex("lard_dripper");
        public Dripper(EntityRendererProvider.Context c) {
            super(c, new LardDripperModel(c.bakeLayer(OGMobModels.DRIPPER)), 0.0f);
        }
        @Override protected void scale(LardDripperEntity e, PoseStack ps, float pt) {
            ps.scale(DRIPPER_SCALE, DRIPPER_SCALE, DRIPPER_SCALE);
        }
        @Override public ResourceLocation getTextureLocation(LardDripperEntity e) { return T; }
    }

    public static class Beetle extends MobRenderer<FattadiumBeetleEntity, FattadiumBeetleModel> {
        private static final ResourceLocation T = tex("fattadium_beetle");
        public Beetle(EntityRendererProvider.Context c) {
            super(c, new FattadiumBeetleModel(c.bakeLayer(OGMobModels.BEETLE)), 0.5f);
        }
        @Override protected void scale(FattadiumBeetleEntity e, PoseStack ps, float pt) {
            ps.scale(BEETLE_SCALE, BEETLE_SCALE, BEETLE_SCALE);
        }
        @Override public ResourceLocation getTextureLocation(FattadiumBeetleEntity e) { return T; }
    }

    public static class Shroomling extends MobRenderer<ShroomlingEntity, ShroomlingModel> {
        private static final ResourceLocation T = tex("shroomling");
        public Shroomling(EntityRendererProvider.Context c) {
            super(c, new ShroomlingModel(c.bakeLayer(OGMobModels.SHROOMLING)), 0.3f);
        }
        @Override protected void scale(ShroomlingEntity e, PoseStack ps, float pt) {
            ps.scale(SHROOMLING_SCALE, SHROOMLING_SCALE, SHROOMLING_SCALE);
        }
        @Override public ResourceLocation getTextureLocation(ShroomlingEntity e) { return T; }
    }

    public static class MawChest extends MobRenderer<MawChestEntity, MawChestModel> {
        private static final ResourceLocation T = tex("maw_chest");
        public MawChest(EntityRendererProvider.Context c) {
            super(c, new MawChestModel(c.bakeLayer(OGMobModels.MAW_CHEST)), 0.5f);
        }
        @Override protected void scale(MawChestEntity e, PoseStack ps, float pt) {
            ps.scale(MAW_CHEST_SCALE, MAW_CHEST_SCALE, MAW_CHEST_SCALE);
        }
        @Override public ResourceLocation getTextureLocation(MawChestEntity e) { return T; }
    }

    public static class Archer extends net.minecraft.client.renderer.entity.HumanoidMobRenderer<LardstoneArcherEntity, LardstoneArcherModel> {
        private static final ResourceLocation SRC =
                ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/skeleton/skeleton.png");
        public Archer(EntityRendererProvider.Context c) {
            super(c, new LardstoneArcherModel(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SKELETON)), 0.5f);
            this.addLayer(new net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer<>(this,
                    new LardstoneArcherModel(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SKELETON_INNER_ARMOR)),
                    new LardstoneArcherModel(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SKELETON_OUTER_ARMOR)),
                    c.getModelManager()));
            this.addLayer(new HeldBlockLayer(this, c.getBlockRenderDispatcher()));
        }
        @Override public ResourceLocation getTextureLocation(LardstoneArcherEntity e) { return LardRecolor.get(SRC, "lardstone_archer"); }
    }

    private static class HeldBlockLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<LardstoneArcherEntity, LardstoneArcherModel> {
        private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;
        HeldBlockLayer(net.minecraft.client.renderer.entity.RenderLayerParent<LardstoneArcherEntity, LardstoneArcherModel> parent,
                       net.minecraft.client.renderer.block.BlockRenderDispatcher blocks) {
            super(parent);
            this.blocks = blocks;
        }
        @Override
        public void render(PoseStack ps, net.minecraft.client.renderer.MultiBufferSource buf, int light, LardstoneArcherEntity e,
                           float limbSwing, float limbSwingAmount, float pt, float age, float netHeadYaw, float headPitch) {
            var st = e.getHeldState();
            if (st == null) return;
            ps.pushPose();
            ps.translate(0.0, 0.5, -0.5);
            ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180f));
            ps.scale(0.6f, 0.6f, 0.6f);
            ps.translate(-0.5, -0.5, -0.5);
            blocks.renderSingleBlock(st, ps, buf, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            ps.popPose();
        }
    }

    public static class Crawler extends MobRenderer<LardCrawlerEntity, LardCrawlerModel> {
        private static final float ATTACH_PITCH = -90f;
        private static final ResourceLocation SRC =
                ResourceLocation.fromNamespaceAndPath(com.eisiadev.oversaturatedgrowth.OversaturatedGrowth.MODID,
                        "textures/mobs/rock_crawler_src.png");
        public Crawler(EntityRendererProvider.Context c) {
            super(c, new LardCrawlerModel(c.bakeLayer(OGMobModels.CRAWLER)), 0.6f);
        }
        @Override protected void setupRotations(LardCrawlerEntity e, PoseStack ps, float bob, float yBodyRot, float pt) {
            super.setupRotations(e, ps, bob, yBodyRot, pt);
            if (e.isAttached()) ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(ATTACH_PITCH));
        }
        @Override protected void scale(LardCrawlerEntity e, PoseStack ps, float pt) {
            ps.scale(CRAWLER_SCALE, CRAWLER_SCALE, CRAWLER_SCALE);
        }
        @Override public ResourceLocation getTextureLocation(LardCrawlerEntity e) { return LardRecolor.get(SRC, "lard_crawler"); }
    }

    public static class Butler extends net.minecraft.client.renderer.entity.HumanoidMobRenderer<ButlerEntity, ButlerModel> {
        private static final ResourceLocation SRC =
                ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/skeleton/skeleton.png");
        public Butler(EntityRendererProvider.Context c) {
            super(c, new ButlerModel(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SKELETON)), 0.5f);
            this.addLayer(new net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer<>(this,
                    new ButlerModel(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SKELETON_INNER_ARMOR)),
                    new ButlerModel(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SKELETON_OUTER_ARMOR)),
                    c.getModelManager()));
        }
        @Override public ResourceLocation getTextureLocation(ButlerEntity e) { return LardRecolor.get(SRC, "butler"); }
    }

    public static class Chef extends MobRenderer<ChefEntity, ChefModel> {
        private static final ResourceLocation SRC = tex("chef_src");
        public Chef(EntityRendererProvider.Context c) {
            super(c, new ChefModel(c.bakeLayer(OGMobModels.CHEF)), 0.5f);
        }
        @Override public ResourceLocation getTextureLocation(ChefEntity e) { return LardRecolor.get(SRC, "chef"); }
    }

    public static class Knight extends MobRenderer<GourmandKnightEntity, KnightModel> {
        private static final ResourceLocation SRC = tex("knight_src");
        public Knight(EntityRendererProvider.Context c) {
            super(c, new KnightModel(c.bakeLayer(OGMobModels.KNIGHT)), 0.7f);
        }
        @Override protected void scale(GourmandKnightEntity e, PoseStack ps, float pt) { ps.scale(1.2f, 1.2f, 1.2f); }
        @Override public ResourceLocation getTextureLocation(GourmandKnightEntity e) { return LardRecolor.get(SRC, "gourmand_knight"); }
    }

    public static class Overlard extends MobRenderer<OverlardEntity, OverlardModel> {
        private static final ResourceLocation T = tex("overlard");
        public Overlard(EntityRendererProvider.Context c) {
            super(c, new OverlardModel(c.bakeLayer(OGMobModels.OVERLARD)), 1.0f);
        }
        @Override protected void scale(OverlardEntity e, PoseStack ps, float pt) {
            float f = e.factor();
            float t = e.tickCount + pt;

            float sy = 1.0f + 0.04f * (float) Math.sin(t * 0.12f), sx = 1.0f - 0.02f * (float) Math.sin(t * 0.12f);
            switch (e.state()) {
                case OverlardEntity.ST_SLAM -> { sy = 0.72f; sx = 1.18f; }
                case OverlardEntity.ST_PICKUP -> { sy = 0.88f + 0.05f * (float) Math.sin(t * 0.8f); sx = 1.06f; }
                case OverlardEntity.ST_SWALLOW -> { sy = 0.9f; sx = 1.15f; }
                case OverlardEntity.ST_THROW -> { sy = 1.15f; sx = 0.93f; }
                case OverlardEntity.ST_ROAR -> { sy = 1.0f + 0.1f * (float) Math.sin(t * 1.3f); sx = 1.0f - 0.05f * (float) Math.sin(t * 1.3f); }
                default -> { }
            }
            this.shadowRadius = 1.0f * f;
            ps.scale(f * sx, f * sy, f * sx);
        }
        @Override public ResourceLocation getTextureLocation(OverlardEntity e) { return T; }
    }
}
