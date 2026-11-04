package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * One item class for all three shapes. It does nothing on its own; it only has an effect while it sits in the
 * player's Accessory Bag (default key: K).
 */
public class AccessoryItem extends Item {

    private final AccessoryType type;

    public AccessoryItem(AccessoryType type) {
        super(new Item.Properties().stacksTo(1));
        this.type = type;
    }

    public AccessoryType type() {
        return type;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        AccessoryData data = AccessoryData.read(stack);
        if (data == null) return super.getName(stack);
        return Component.translatable("tooltip.oversaturatedgrowth.accessory.name",
                        Component.translatable("accessory.oversaturatedgrowth.tier." + data.tier().id),
                        Component.translatable(type.translationKey()))
                .withStyle(Style.EMPTY.withColor(data.tier().color));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tip, @NotNull TooltipFlag flag) {
        AccessoryData data = AccessoryData.read(stack);
        if (data == null) {
            tip.add(Component.translatable("tooltip.oversaturatedgrowth.accessory.unrolled").withStyle(ChatFormatting.GRAY));
            return;
        }
        for (AccessoryData.Entry e : data.entries()) {
            int color = e.stat().color(data.tier(), e.value());
            tip.add(Component.literal(e.stat().format(e.value()) + " ")
                    .append(e.stat().displayName())
                    .withStyle(Style.EMPTY.withColor(color))
                    .append(rollPercent(data.tier(), e)));
        }
        tip.add(Component.empty());
        tip.add(Component.translatable("tooltip.oversaturatedgrowth.accessory.bag_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** " [ 72% ]": where the roll sits between the tier's minimum (0%) and maximum (100%). */
    public static Component rollPercent(AccessoryTier tier, AccessoryData.Entry e) {
        return Component.literal(" [ ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(e.stat().percentOf(tier, e.value()) + "%").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" ]").withStyle(ChatFormatting.YELLOW));
    }

    /** An accessory that arrives without data (/give, old worlds, other mods) is rolled as Common the first time it is carried. */
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (level.isClientSide || AccessoryData.read(stack) != null) return;
        AccessoryData.generate(AccessoryTier.COMMON, level.random).write(stack);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        AccessoryData data = AccessoryData.read(stack);
        return data != null && data.tier().ordinal() >= AccessoryTier.MYTHIC.ordinal();
    }
}
