package com.example.examplemod.expansion;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ColoredMaterialItem extends Item {
    public enum Kind {
        ENHANCE(ChatFormatting.YELLOW, "✦", "Mảnh Cường Hóa", "Dùng trong Đe để cường hóa trang bị."),
        HERB(ChatFormatting.GREEN, "✿", "Linh Thảo", "Nguyên liệu luyện đan."),
        MOB(ChatFormatting.RED, "❖", "Nguyên liệu Quái", "Rơi từ quái tu tiên và bí cảnh."),
        BOSS(ChatFormatting.GOLD, "✪", "Nguyên liệu Boss", "Rơi từ boss và bí cảnh khó."),
        ARRAY(ChatFormatting.AQUA, "☯", "Tinh Thạch", "Dùng kích hoạt Tụ Linh Trận."),
        SPECIAL(ChatFormatting.LIGHT_PURPLE, "◆", "Vật phẩm Tu Tiên", "Nguyên liệu đặc biệt."),
        LIQUID(ChatFormatting.BLUE, "✧", "Linh Tuyền", "Phụ liệu luyện đan.");

        public final ChatFormatting color;
        public final String mark;
        public final String group;
        public final String desc;

        Kind(ChatFormatting color, String mark, String group, String desc) {
            this.color = color;
            this.mark = mark;
            this.group = group;
            this.desc = desc;
        }
    }

    private final Kind kind;
    private final String displayName;
    private final ChatFormatting rarityColor;

    public ColoredMaterialItem(Kind kind, String displayName, Properties properties) {
        this(kind, displayName, kind.color, properties);
    }

    public ColoredMaterialItem(Kind kind, String displayName, ChatFormatting rarityColor, Properties properties) {
        super(properties);
        this.kind = kind;
        this.displayName = displayName;
        this.rarityColor = rarityColor;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(kind.mark + " " + displayName + " " + kind.mark).withStyle(rarityColor);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("════════════════").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal(kind.group).withStyle(kind.color));
        tooltip.add(Component.literal(kind.desc).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Không phải vật phẩm vanilla.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("════════════════").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
