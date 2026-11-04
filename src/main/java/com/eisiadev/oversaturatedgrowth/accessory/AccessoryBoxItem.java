package com.eisiadev.oversaturatedgrowth.accessory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/** Crafted box: right-click for a random Common..Epic accessory of a random shape. */
public class AccessoryBoxItem extends Item {

    public AccessoryBoxItem() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide) {
            AccessoryType type = AccessoryType.VALUES[level.random.nextInt(AccessoryType.VALUES.length)];
            AccessoryTier tier = AccessoryData.rollTier(level.random, AccessoryTier.COMMON, AccessoryTier.EPIC);
            ItemStack reward = AccessoryData.newStack(type, AccessoryData.generate(tier, level.random));
            held.shrink(1);
            if (!player.getInventory().add(reward)) player.drop(reward, false);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tip, @NotNull TooltipFlag flag) {
        tip.add(Component.translatable("tooltip.oversaturatedgrowth.accessory_box").withStyle(ChatFormatting.GRAY));
    }
}
