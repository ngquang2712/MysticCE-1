package com.example.examplemod.mystic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ShieldEnhanceItem extends Item {
    private final ShieldEnhanceType enhanceType;

    public ShieldEnhanceItem(ShieldEnhanceType enhanceType, Properties properties) {
        super(properties);
        this.enhanceType = enhanceType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack hotbarStack = player.getInventory().getItem(slot);
                if (ShieldHelper.addEnhance(hotbarStack, enhanceType)) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    player.sendSystemMessage(Component.literal("Đã ép " + enhanceType.getDisplayName() + " vào " + hotbarStack.getHoverName().getString()).withStyle(ChatFormatting.LIGHT_PURPLE));
                    return InteractionResultHolder.success(stack);
                }
            }

            player.sendSystemMessage(Component.literal("Cần đặt Khiên còn slot trống (chưa có Mảnh) trên hotbar!").withStyle(ChatFormatting.RED));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("+" + (int) enhanceType.getArmorBonus() + " giáp khi khiên được trang bị").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("+" + (int) (enhanceType.getHealthBonus() / 2.0D) + " tim khi khiên được trang bị").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Chuột phải để ép vào Khiên trên hotbar").withStyle(ChatFormatting.GRAY));
    }
}
