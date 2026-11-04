package com.eisiadev.oversaturatedgrowth.client;

import net.willsbr.gluttonousgrowth.client.ClientCPMData;
import net.willsbr.gluttonousgrowth.config.GluttonousClientConfig;

public class PredatorConsumeOverlay {

    private static boolean active = false;
    private static String savedStuffedLayer = null;

    public static void setActive(boolean state) {
        if (state == active) return;
        active = state;

        if (active) {

            String current = GluttonousClientConfig.stuffedLayerConfigEntry.get();
            if (current != null && !current.isEmpty()) {
                savedStuffedLayer = current;

                GluttonousClientConfig.stuffedLayerConfigEntry.set("");
            }
        } else {

            if (savedStuffedLayer != null && !savedStuffedLayer.isEmpty()) {
                GluttonousClientConfig.stuffedLayerConfigEntry.set(savedStuffedLayer);
            }
            savedStuffedLayer = null;
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static void tick() {
        if (!active) return;
        if (savedStuffedLayer == null || savedStuffedLayer.isEmpty()) return;

        var api = ClientCPMData.getPlayersAPI();
        if (api == null) return;

        int maxValue = api.getAnimationMaxValue(savedStuffedLayer);
        if (maxValue <= 0) return;

        api.playAnimation(savedStuffedLayer, maxValue);

        String last = GluttonousClientConfig.lastStuffedLayer;
        if (last != null && !last.isEmpty() && !last.equals(savedStuffedLayer)) {
            api.playAnimation(last, 0);
        }
    }
}
