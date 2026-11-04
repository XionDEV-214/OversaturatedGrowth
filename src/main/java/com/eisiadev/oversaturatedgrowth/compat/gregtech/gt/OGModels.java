package com.eisiadev.oversaturatedgrowth.compat.gregtech.gt;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.client.util.ExtendedBlockModelRotation;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

public final class OGModels {
    private OGModels() {}

    private static final String TEMPLATE = "gtceu:block/machine/template/cube_all/sided";

    public static void registerListener(IEventBus modBus) {
        modBus.addListener(OGModels::onRegisterDynamicResources);
    }

    private static void onRegisterDynamicResources(RegisterDynamicResourcesEvent event) {
        register("lard_extractor_multiblock", OGMachines.BASE_CASING, OGMachines.OVERLAY);
    }

    private static void register(String name, ResourceLocation casing, ResourceLocation overlay) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, name);
        ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "machine/" + name);
        String modelPath = OversaturatedGrowth.MODID + ":block/machine/" + name;

        GTDynamicResourcePack.addBlockState(id, blockState(modelPath));
        GTDynamicResourcePack.addBlockModel(modelId, blockModel(id, casing.toString(), overlay.toString()));

        JsonObject item = new JsonObject();
        item.addProperty("parent", modelPath);
        GTDynamicResourcePack.addItemModel(id, item);
    }

    private static JsonObject blockState(String modelPath) {
        JsonObject variants = new JsonObject();
        for (Direction front : Direction.Plane.HORIZONTAL) {
            for (Direction up : Direction.Plane.HORIZONTAL) {
                ExtendedBlockModelRotation rot = ExtendedBlockModelRotation.get(front, up);
                JsonObject v = new JsonObject();
                v.addProperty("model", modelPath);
                if (rot.getAngleX() != 0) v.addProperty("x", rot.getAngleX());
                if (rot.getAngleY() != 0) v.addProperty("y", rot.getAngleY());
                if (rot.getAngleZ() != 0) v.addProperty("gtceu:z", rot.getAngleZ());
                variants.add("facing=" + front.getName() + ",upwards_facing=" + up.getName(), v);
            }
        }
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private static JsonObject blockModel(ResourceLocation machineId, String casing, String overlay) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/block");
        model.addProperty("loader", "gtceu:machine");
        model.addProperty("machine", machineId.toString());

        JsonObject variants = new JsonObject();
        for (boolean formed : new boolean[] { false, true }) {
            addState(variants, formed, "idle", casing, overlay + "/overlay_front", overlay + "/overlay_front_emissive");
            addState(variants, formed, "working", casing, overlay + "/overlay_front_active", overlay + "/overlay_front_active_emissive");
            addState(variants, formed, "waiting", casing, overlay + "/overlay_front_paused", overlay + "/overlay_front_paused_emissive");
            addState(variants, formed, "suspend", casing, overlay + "/overlay_front_paused", overlay + "/overlay_front_paused_emissive");
        }
        model.add("variants", variants);

        JsonObject overrides = new JsonObject();
        overrides.addProperty("all", casing);
        model.add("texture_overrides", overrides);
        return model;
    }

    private static void addState(JsonObject variants, boolean formed, String status, String casing,
                                 String overlayFront, String overlayFrontEmissive) {
        JsonObject textures = new JsonObject();
        textures.addProperty("all", casing);
        textures.addProperty("overlay_front", overlayFront);
        textures.addProperty("overlay_front_emissive", overlayFrontEmissive);

        JsonObject nested = new JsonObject();
        nested.addProperty("parent", TEMPLATE);
        nested.add("textures", textures);

        JsonObject entry = new JsonObject();
        entry.add("model", nested);
        variants.add("is_formed=" + formed + ",recipe_logic_status=" + status, entry);
    }
}
