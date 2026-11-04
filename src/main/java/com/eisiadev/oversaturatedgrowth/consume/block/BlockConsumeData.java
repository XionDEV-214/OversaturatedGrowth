package com.eisiadev.oversaturatedgrowth.consume.block;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockConsumeData extends SimpleJsonResourceReloadListener {

    public static final int DEFAULT_HOLD_TICKS = 30;
    private static final Gson GSON = new GsonBuilder().create();
    public static final BlockConsumeData INSTANCE = new BlockConsumeData();

    private final Map<Block, BlockConsumeEntry> blocks = new HashMap<>();
    private final Map<TagKey<Block>, BlockConsumeEntry> tags = new HashMap<>();

    private BlockConsumeData() {
        super(GSON, "og_consumables");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        blocks.clear();
        tags.clear();
        for (Map.Entry<ResourceLocation, JsonElement> file : files.entrySet()) {
            try {
                JsonObject root = GsonHelper.convertToJsonObject(file.getValue(), "og_consumables");
                if (!conditionsMet(root)) continue;
                for (JsonElement e : GsonHelper.getAsJsonArray(root, "entries", new JsonArray())) {
                    parseEntry(GsonHelper.convertToJsonObject(e, "entry"));
                }
            } catch (Exception ex) {
                OversaturatedGrowth.LOGGER.warn("og_consumables {} 파싱 실패: {}", file.getKey(), ex.toString());
            }
        }
        OversaturatedGrowth.LOGGER.info("Block Consume: 블록 {}종, 태그 {}종 로드", blocks.size(), tags.size());
    }

    private static boolean conditionsMet(JsonObject root) {
        if (!root.has("conditions")) return true;
        for (JsonElement c : GsonHelper.getAsJsonArray(root, "conditions")) {
            if (!conditionMet(c.getAsJsonObject())) return false;
        }
        return true;
    }

    private static boolean conditionMet(JsonObject c) {
        String type = GsonHelper.getAsString(c, "type", "");
        return switch (type) {
            case "forge:mod_loaded" -> ModList.get().isLoaded(GsonHelper.getAsString(c, "modid"));
            case "forge:not" -> !conditionMet(GsonHelper.getAsJsonObject(c, "value"));
            default -> true;
        };
    }

    private void parseEntry(JsonObject o) {
        double cal = GsonHelper.getAsDouble(o, "calories_percent", 0.0);
        double weight = GsonHelper.getAsDouble(o, "weight_percent", 0.0);
        int hold = Math.max(1, GsonHelper.getAsInt(o, "hold_ticks", DEFAULT_HOLD_TICKS));
        boolean bite = "bite".equals(GsonHelper.getAsString(o, "mode", "whole"));
        List<MobEffectInstance> effects = new ArrayList<>();
        for (JsonElement ef : GsonHelper.getAsJsonArray(o, "effects", new JsonArray())) {
            JsonObject eo = ef.getAsJsonObject();
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.parse(GsonHelper.getAsString(eo, "id")));
            if (effect == null) continue;
            effects.add(new MobEffectInstance(effect, GsonHelper.getAsInt(eo, "duration", 100), GsonHelper.getAsInt(eo, "amplifier", 0)));
        }
        BlockConsumeEntry entry = new BlockConsumeEntry(cal, weight, hold, bite, List.copyOf(effects));
        if (o.has("block")) {
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(GsonHelper.getAsString(o, "block")));
            if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) blocks.put(block, entry);
        } else if (o.has("tag")) {
            tags.put(TagKey.create(Registries.BLOCK, ResourceLocation.parse(GsonHelper.getAsString(o, "tag"))), entry);
        }
    }

    @Nullable
    public BlockConsumeEntry find(BlockState state) {
        BlockConsumeEntry e = blocks.get(state.getBlock());
        if (e != null) return e;
        for (Map.Entry<TagKey<Block>, BlockConsumeEntry> t : tags.entrySet()) {
            if (state.is(t.getKey())) return t.getValue();
        }
        return null;
    }

    public List<ResourceLocation> blockIds() {
        List<ResourceLocation> out = new ArrayList<>();
        for (Block b : blocks.keySet()) out.add(ForgeRegistries.BLOCKS.getKey(b));
        return out;
    }

    public List<ResourceLocation> tagIds() {
        List<ResourceLocation> out = new ArrayList<>();
        for (TagKey<Block> t : tags.keySet()) out.add(t.location());
        return out;
    }
}
