package com.eisiadev.oversaturatedgrowth.accessory;

import com.eisiadev.oversaturatedgrowth.init.ModLootFunctions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function {@code oversaturatedgrowth:random_accessory} with {@code min_tier} / {@code max_tier}.
 * Put it on an {@code accessory_amulet / belt / ring} entry to roll the stats when the chest is generated.
 */
public class RandomAccessoryFunction extends LootItemConditionalFunction {

    private final AccessoryTier min;
    private final AccessoryTier max;

    protected RandomAccessoryFunction(LootItemCondition[] conditions, AccessoryTier min, AccessoryTier max) {
        super(conditions);
        this.min = min;
        this.max = max;
    }

    @Override
    public LootItemFunctionType getType() {
        return ModLootFunctions.RANDOM_ACCESSORY.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!(stack.getItem() instanceof AccessoryItem)) return stack;
        AccessoryTier tier = AccessoryData.rollTier(context.getRandom(), min, max);
        AccessoryData.generate(tier, context.getRandom()).write(stack);
        return stack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomAccessoryFunction> {
        @Override
        public void serialize(JsonObject json, RandomAccessoryFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
            json.addProperty("min_tier", function.min.id);
            json.addProperty("max_tier", function.max.id);
        }

        @Override
        public RandomAccessoryFunction deserialize(JsonObject json, JsonDeserializationContext context,
                                                   LootItemCondition[] conditions) {
            AccessoryTier min = AccessoryTier.byId(GsonHelper.getAsString(json, "min_tier", "common"), AccessoryTier.COMMON);
            AccessoryTier max = AccessoryTier.byId(GsonHelper.getAsString(json, "max_tier", "epic"), AccessoryTier.EPIC);
            return new RandomAccessoryFunction(conditions, min, max);
        }
    }
}
