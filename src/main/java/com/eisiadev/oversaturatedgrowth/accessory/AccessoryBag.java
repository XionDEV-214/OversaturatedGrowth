package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/** The 45-slot Accessory Bag that lives on the player (survives death and dimension changes). */
public class AccessoryBag implements INBTSerializable<CompoundTag> {

    public static final int SIZE = 45;

    private boolean dirty = true;
    private AccessoryTotals totals;

    public final ItemStackHandler items = new ItemStackHandler(SIZE) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof AccessoryItem && AccessoryData.read(stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    public void markDirty() {
        dirty = true;
        totals = null;
    }

    /** True once after every change; the server re-applies the attribute modifiers then. */
    public boolean consumeDirty() {
        boolean d = dirty;
        dirty = false;
        return d;
    }

    public AccessoryTotals totals() {
        if (totals == null) totals = AccessoryTotals.of(items);
        return totals;
    }

    @Override
    public CompoundTag serializeNBT() {
        return items.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        items.deserializeNBT(tag);
        markDirty();
    }
}
