package com.example.examplemod.mystic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class MysticShieldItem extends ShieldItem {
    private final MysticTier tier;

    public MysticShieldItem(MysticTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public MysticTier getMysticTier() {
        return tier;
    }

    public int getBlockReducePercent() {
        return switch (tier) {
            case TRUYEN_THUYET -> 15;
            case TOI_CAO -> 30;
            case THUONG_CO -> 50;
            case THIEN_HA -> 60;
            case NHAM_DAN, HUYEN_THOAI, SIEU_SAIYAN, THO_MO -> 0;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Bậc: " + tier.getDisplayName()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Khi đỡ đòn: giảm " + getBlockReducePercent() + "% sát thương").withStyle(ChatFormatting.AQUA));
        
        // hiển thị các Mảnh Cường Hóa đã gắn trên khiên
        net.minecraft.nbt.ListTag list = ShieldHelper.getEnhanceList(stack);
        if (!list.isEmpty()) {
            tooltip.add(Component.literal("Mảnh Cường Hóa:").withStyle(ChatFormatting.LIGHT_PURPLE));
            for (int i = 0; i < list.size(); i++) {
                String id = list.getCompound(i).getString("ENHANCE_ID");
                try {
                    ShieldEnhanceType t = ShieldEnhanceType.valueOf(id);
                    tooltip.add(Component.literal("- " + t.getDisplayName()).withStyle(ChatFormatting.AQUA));
                } catch (IllegalArgumentException ignored) {
                    tooltip.add(Component.literal("- Mảnh lỗi").withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
