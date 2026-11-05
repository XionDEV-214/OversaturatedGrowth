package com.eisiadev.oversaturatedgrowth.accessory;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessoryBagProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<AccessoryBag> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OversaturatedGrowth.MODID, "accessory_bag");

    private final AccessoryBag bag = new AccessoryBag();
    private LazyOptional<AccessoryBag> optional = LazyOptional.of(() -> bag);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? optional.cast() : LazyOptional.empty();
    }

    /**
     * Called when the player entity's caps are invalidated (death, dimension change). Old holders are told the cap is
     * gone, but a fresh optional is made right away: PlayerEvent.Clone revives the old player's caps to copy the bag
     * over, and an already-invalidated optional would make that copy silently find nothing (the bag "evaporated").
     */
    public void invalidate() {
        optional.invalidate();
        optional = LazyOptional.of(() -> bag);
    }

    @Override
    public CompoundTag serializeNBT() {
        return bag.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        bag.deserializeNBT(nbt);
    }
}
