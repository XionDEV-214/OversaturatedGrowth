package com.eisiadev.oversaturatedgrowth.accessory.gui;

import brachy.modularui.api.MCHelper;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widgets.slot.ItemSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

/** Same slot tooltip workaround the Vending Machine uses (vanilla item tooltip, only while the slot is synced). */
final class AccessoryTooltips {

    private AccessoryTooltips() {}

    static ItemSlot fix(ItemSlot slot) {
        return fix(slot, null);
    }

    /** @param extra extra lines (click hints) appended below the item tooltip; may return an empty list. */
    static ItemSlot fix(ItemSlot slot, Function<ItemStack, List<Component>> extra) {
        RichTooltip tooltip = slot.itemTooltip();
        tooltip.tooltipBuilder(null);
        tooltip.tooltipBuilder(t -> {
            if (!slot.isSynced()) return;
            ItemStack stack = slot.getSlot().getItem();
            if (stack.isEmpty()) return;
            List<Component> lines = MCHelper.getItemToolTip(stack);
            for (int i = 0; i < lines.size(); i++) {
                t.addLine(lines.get(i));
                if (i == 0 && lines.size() > 1) t.spaceLine();
            }
            if (extra != null) {
                List<Component> more = extra.apply(stack);
                if (!more.isEmpty()) {
                    t.spaceLine();
                    for (Component c : more) t.addLine(c);
                }
            }
        });
        return slot;
    }
}
