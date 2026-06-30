package com.example.examplemod.mystic;

import com.example.examplemod.appearance.WeaponAppearanceManager;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class MysticSwordItem extends SwordItem {
    private final MysticTier tier;
    private final int bonusDamage;

    public MysticSwordItem(Tier toolTier, int bonusDamage, float speed, MysticTier tier, Properties properties) {
        super(toolTier, bonusDamage, speed, properties);
        this.tier = tier;
        this.bonusDamage = bonusDamage;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Bậc: " + tier.getDisplayName()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Thông số: +" + bonusDamage + " dame").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Full set +" + getDamagePercent(tier) + "% sát thương").withStyle(ChatFormatting.YELLOW));
        ResourceLocation appearance = WeaponAppearanceManager.getWeaponAppearanceId(stack);
        if (appearance != null) {
            tooltip.add(Component.literal("Ngoại hình: " + appearance).withStyle(ChatFormatting.AQUA));
        }
    }

    private int getDamagePercent(MysticTier tier) {
        return switch (tier) {
            case TRUYEN_THUYET -> 15;
            case TOI_CAO -> 20;
            case THUONG_CO -> 25;
            case THIEN_HA -> 30;
            case NHAM_DAN, HUYEN_THOAI, SIEU_SAIYAN, THO_MO -> 0;
        };
    }
}
