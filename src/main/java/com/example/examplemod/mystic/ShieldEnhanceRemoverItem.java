package com.example.examplemod.mystic;

import com.example.examplemod.ExampleMod;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

public class ShieldEnhanceRemoverItem extends Item {
    public ShieldEnhanceRemoverItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack remover = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(remover, true);

        for (int slot = 0; slot < 9; slot++) {
            ItemStack hotbarStack = player.getInventory().getItem(slot);
            if (!(hotbarStack.getItem() instanceof net.minecraft.world.item.ShieldItem)) continue;
            if (!hotbarStack.hasTag()) continue;

            ListTag list = ShieldHelper.getEnhanceList(hotbarStack);
            if (list.isEmpty()) continue;

            CompoundTag removed = list.getCompound(0).copy();
            list.remove(0);
            hotbarStack.getOrCreateTag().put(ShieldHelper.SHIELD_ENHANCE_LIST, list);
            if (list.isEmpty()) hotbarStack.getTag().remove(ShieldHelper.SHIELD_ENHANCE_LIST);

            String id = removed.getString("ENHANCE_ID");
            ShieldEnhanceType type;
            try {
                type = ShieldEnhanceType.valueOf(id);
            } catch (IllegalArgumentException e) {
                player.sendSystemMessage(Component.literal("Không thể đọc Mảnh, đã bị lỗi").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(remover);
            }

            ItemStack out;
            switch (type) {
                case SHIELD_I -> out = new ItemStack(ExampleMod.SHIELD_ENHANCE_I.get());
                case SHIELD_II -> out = new ItemStack(ExampleMod.SHIELD_ENHANCE_II.get());
                case SHIELD_III -> out = new ItemStack(ExampleMod.SHIELD_ENHANCE_III.get());
                default -> out = ItemStack.EMPTY;
            }

            giveOrDrop(player, out);
            consume(player, remover);
            player.sendSystemMessage(Component.literal("☣ Đã gỡ Mảnh Cường Hóa và trả về túi đồ!").withStyle(ChatFormatting.GREEN));
            return InteractionResultHolder.success(remover);
        }

        player.sendSystemMessage(Component.literal("Cần đặt Khiên có Mảnh Cường Hóa trên hotbar!").withStyle(ChatFormatting.RED));
        return InteractionResultHolder.fail(remover);
    }

    private static void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Gỡ Mảnh Cường Hóa trên Khiên và trả về túi đồ").withStyle(ChatFormatting.GRAY));
    }
}
