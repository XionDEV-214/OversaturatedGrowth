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
    private final LazyOptional<AccessoryBag> optional = LazyOptional.of(() -> bag);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? optional.cast() : LazyOptional.empty();
    }

    public void invalidate() {
        optional.invalidate();
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
