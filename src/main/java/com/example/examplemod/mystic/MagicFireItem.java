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

public class MagicFireItem extends Item {
    private final MagicFireType fireType;

    public MagicFireItem(MagicFireType fireType, Properties properties) {
        super(properties);
        this.fireType = fireType;
    }

    public MagicFireType getFireType() {
        return fireType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack fireStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack hotbarStack = player.getInventory().getItem(slot);

                if (hotbarStack.getItem() instanceof SkullItem skullItem && skullItem.addFire(hotbarStack, fireType)) {
                    if (!player.getAbilities().instabuild) {
                        fireStack.shrink(1);
                    }

                    player.sendSystemMessage(Component.literal("Đã ép " + fireType.getDisplayName() + " vào " + hotbarStack.getHoverName().getString()).withStyle(ChatFormatting.LIGHT_PURPLE));
                    return InteractionResultHolder.success(fireStack);
                }
            }

            player.sendSystemMessage(Component.literal("Cần đặt Sọ còn slot trống trên hotbar!").withStyle(ChatFormatting.RED));
        }

        return InteractionResultHolder.sidedSuccess(fireStack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (fireType.getHealthBonus() > 0) {
            tooltip.add(Component.literal("+" + (int) (fireType.getHealthBonus() / 2.0D) + " tim khi Sọ ở hotbar").withStyle(ChatFormatting.RED));
        }
        if (fireType.getDamageBonus() > 0) {
            tooltip.add(Component.literal("+" + (int) fireType.getDamageBonus() + " sát thương khi Sọ ở hotbar").withStyle(ChatFormatting.DARK_RED));
        }
        tooltip.add(Component.literal("Chuột phải để ép vào Sọ trên hotbar").withStyle(ChatFormatting.GRAY));
    }
}
