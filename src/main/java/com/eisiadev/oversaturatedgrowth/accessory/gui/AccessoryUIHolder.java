package com.eisiadev.oversaturatedgrowth.accessory.gui;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class AccessoryUIHolder implements IUIHolder<PosGuiData> {

    public static final AccessoryUIHolder INSTANCE = new AccessoryUIHolder();

    private AccessoryUIHolder() {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel<?> mainPanel) {
        return new ModularScreen(OversaturatedGrowth.MODID, mainPanel);
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new AccessoryGui(data, syncManager).build(settings);
    }
}
