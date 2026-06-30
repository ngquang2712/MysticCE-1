package com.example.examplemod.rename;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RenameScrollItem extends Item {

    public RenameScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Dùng để đổi tên vật phẩm.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Cầm vật phẩm cần đổi tên trên tay chính.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Lệnh: /renameitem <tên>").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Hỗ trợ: &c &6 &l &o &n &m &k &r").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Ví dụ: /renameitem &c&l⚔ Huyết Kiếm ⚔").withStyle(ChatFormatting.RED));
    }
}