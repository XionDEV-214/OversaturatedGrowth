package com.eisiadev.oversaturatedgrowth.client;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.util.LardColors;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class LardRecolor {
    private LardRecolor() {}

    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    private static final float TARGET_MEAN = 0.78f;
    private static final float MAX_BOOST = 4.0f;

    public static ResourceLocation get(ResourceLocation source, String key) {
        ResourceLocation cached = CACHE.get(key);
        if (cached != null) return cached;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "dynamic/" + key);
        try {
            var res = Minecraft.getInstance().getResourceManager().getResource(source);
            if (res.isEmpty()) return source;
            try (InputStream in = res.get().open(); NativeImage img = NativeImage.read(in)) {
                recolor(img);
                Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(copy(img)));
            }
            CACHE.put(key, id);
            return id;
        } catch (Exception e) {
            OversaturatedGrowth.LOGGER.error("[LardRecolor] failed for {}", source, e);
            return source;
        }
    }

    private static NativeImage copy(NativeImage src) {
        NativeImage out = new NativeImage(src.getWidth(), src.getHeight(), false);
        out.copyFrom(src);
        return out;
    }

    private static void recolor(NativeImage img) {
        int w = img.getWidth(), h = img.getHeight();
        double sum = 0; int n = 0;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int p = img.getPixelRGBA(x, y);
            if ((p >>> 24) < 16) continue;
            sum += lum(p); n++;
        }
        float mean = n == 0 ? 0.5f : (float) (sum / n);
        float boost = Math.max(1.0f, Math.min(MAX_BOOST, TARGET_MEAN / Math.max(mean, 0.05f)));
        float lr = ((LardColors.LARD >> 16) & 255) / 255f;
        float lg = ((LardColors.LARD >> 8) & 255) / 255f;
        float lb = (LardColors.LARD & 255) / 255f;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int p = img.getPixelRGBA(x, y);
            int a = p >>> 24;
            if (a == 0) continue;
            float l = Math.min(1f, lum(p) * boost);
            int r = Math.min(255, Math.round(l * lr * 255f));
            int g = Math.min(255, Math.round(l * lg * 255f));
            int b = Math.min(255, Math.round(l * lb * 255f));
            img.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
        }
    }

    private static float lum(int p) {
        float r = (p & 255) / 255f, g = ((p >> 8) & 255) / 255f, b = ((p >> 16) & 255) / 255f;
        return 0.299f * r + 0.587f * g + 0.114f * b;
    }
}
