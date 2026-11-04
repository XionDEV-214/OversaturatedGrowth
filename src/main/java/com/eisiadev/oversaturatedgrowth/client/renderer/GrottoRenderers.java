package com.eisiadev.oversaturatedgrowth.client.renderer;

import com.eisiadev.oversaturatedgrowth.client.LardRecolor;
import com.eisiadev.oversaturatedgrowth.entity.GluttonToadEntity;
import com.eisiadev.oversaturatedgrowth.entity.IPlump;
import com.eisiadev.oversaturatedgrowth.entity.LardfinEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class GrottoRenderers {
    private GrottoRenderers() {}

    private static ResourceLocation vanilla(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    private static void lardyScale(LivingEntity e, PoseStack ps) {
        float p = (e instanceof IPlump pl) ? IPlump.scaleFor(pl.getPlumpLevel()) : 1.0f;
        ps.scale(IPlump.WIDE * p, p, IPlump.WIDE * p);
    }

    public static class Pig extends net.minecraft.client.renderer.entity.PigRenderer {
        public Pig(EntityRendererProvider.Context c) { super(c); }
        @Override protected void scale(net.minecraft.world.entity.animal.Pig e, PoseStack ps, float pt) { lardyScale(e, ps); }
        @Override public ResourceLocation getTextureLocation(net.minecraft.world.entity.animal.Pig e) {
            return LardRecolor.get(vanilla("textures/entity/pig/pig.png"), "lardy_pig");
        }
    }

    public static class Cow extends net.minecraft.client.renderer.entity.CowRenderer {
        public Cow(EntityRendererProvider.Context c) { super(c); }
        @Override protected void scale(net.minecraft.world.entity.animal.Cow e, PoseStack ps, float pt) { lardyScale(e, ps); }
        @Override public ResourceLocation getTextureLocation(net.minecraft.world.entity.animal.Cow e) {
            return LardRecolor.get(vanilla("textures/entity/cow/cow.png"), "lardy_cow");
        }
    }

    public static class Sheep extends net.minecraft.client.renderer.entity.SheepRenderer {
        public Sheep(EntityRendererProvider.Context c) {
            super(c);
            this.layers.removeIf(l -> l instanceof net.minecraft.client.renderer.entity.layers.SheepFurLayer);
            this.addLayer(new LardyFurLayer(this, c.getModelSet()));
        }
        @Override protected void scale(net.minecraft.world.entity.animal.Sheep e, PoseStack ps, float pt) { lardyScale(e, ps); }
        @Override public ResourceLocation getTextureLocation(net.minecraft.world.entity.animal.Sheep e) {
            return LardRecolor.get(vanilla("textures/entity/sheep/sheep.png"), "lardy_sheep");
        }
    }

    public static class Chicken extends net.minecraft.client.renderer.entity.ChickenRenderer {
        public Chicken(EntityRendererProvider.Context c) { super(c); }
        @Override protected void scale(net.minecraft.world.entity.animal.Chicken e, PoseStack ps, float pt) { lardyScale(e, ps); }
        @Override public ResourceLocation getTextureLocation(net.minecraft.world.entity.animal.Chicken e) {
            return LardRecolor.get(vanilla("textures/entity/chicken.png"), "lardy_chicken");
        }
    }

    public static class Lardfin extends MobRenderer<LardfinEntity, CodModel<LardfinEntity>> {
        public Lardfin(EntityRendererProvider.Context c) {
            super(c, new CodModel<>(c.bakeLayer(ModelLayers.COD)), 0.2f);
        }
        @Override public ResourceLocation getTextureLocation(LardfinEntity e) {
            return LardRecolor.get(vanilla("textures/entity/fish/cod.png"), "lardfin");
        }
    }

    public static class Toad extends net.minecraft.client.renderer.entity.FrogRenderer {
        public Toad(EntityRendererProvider.Context c) { super(c); }
        @Override protected void scale(net.minecraft.world.entity.animal.frog.Frog e, PoseStack ps, float pt) {
            float s = e instanceof GluttonToadEntity t ? t.getToadSize() : 1.0f;
            ps.scale(s, s, s);
            this.shadowRadius = 0.3f * s;
        }
        @Override public ResourceLocation getTextureLocation(net.minecraft.world.entity.animal.frog.Frog e) {
            return LardRecolor.get(vanilla("textures/entity/frog/temperate_frog.png"), "glutton_toad");
        }
    }

    public static class LardyFurLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<net.minecraft.world.entity.animal.Sheep, net.minecraft.client.model.SheepModel<net.minecraft.world.entity.animal.Sheep>> {
        private static final ResourceLocation FUR = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/sheep/sheep_fur.png");
        private final net.minecraft.client.model.SheepFurModel<net.minecraft.world.entity.animal.Sheep> model;

        public LardyFurLayer(net.minecraft.client.renderer.entity.RenderLayerParent<net.minecraft.world.entity.animal.Sheep, net.minecraft.client.model.SheepModel<net.minecraft.world.entity.animal.Sheep>> parent,
                             net.minecraft.client.model.geom.EntityModelSet models) {
            super(parent);
            this.model = new net.minecraft.client.model.SheepFurModel<>(models.bakeLayer(ModelLayers.SHEEP_FUR));
        }

        @Override
        public void render(PoseStack ps, net.minecraft.client.renderer.MultiBufferSource buf, int light,
                           net.minecraft.world.entity.animal.Sheep sheep, float limbSwing, float limbSwingAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (sheep.isSheared() || sheep.isInvisible()) return;
            coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, FUR, ps, buf, light, sheep,
                    limbSwing, limbSwingAmount, age, headYaw, headPitch, partial, 1.0F, 0.80F, 0.30F);
        }
    }
}
