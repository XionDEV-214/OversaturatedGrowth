package com.eisiadev.oversaturatedgrowth.accessory;

/**
 * The three accessory shapes. Every shape is one registered item; the individual stats / tier live in the stack's NBT.
 * <p>
 * Textures (assets/oversaturatedgrowth/textures/item/accessories):
 * amulet = amulet_string + amulet_gem, belt = belt_leather + belt_core, ring = ring_metal + ring_gem.
 */
public enum AccessoryType {
    AMULET("amulet"),
    BELT("belt"),
    RING("ring");

    public static final AccessoryType[] VALUES = values();

    public final String id;

    AccessoryType(String id) {
        this.id = id;
    }

    /** Registry path of the item, e.g. {@code accessory_ring}. */
    public String itemName() {
        return "accessory_" + id;
    }

    public String translationKey() {
        return "item.oversaturatedgrowth." + itemName();
    }
}
