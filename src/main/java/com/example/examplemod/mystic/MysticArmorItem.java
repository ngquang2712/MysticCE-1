package com.example.examplemod.mystic;

import com.example.examplemod.appearance.AppearanceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class MysticArmorItem extends ArmorItem implements DyeableLeatherItem {
    private final MysticTier mysticTier;

    public MysticArmorItem(ArmorMaterial material, Type type, MysticTier mysticTier, Properties properties) {
        super(material, type, properties);
        this.mysticTier = mysticTier;
    }

    public MysticTier getMysticTier() {
        return mysticTier;
    }

    @Override
    public int getColor(ItemStack stack) {
        if (stack.hasTag()) {
            var display = stack.getTag().getCompound("display");
            if (display.contains("color", 99)) {
                return display.getInt("color");
            }
        }
        return switch (mysticTier) {
            case HUYEN_THOAI -> 0xA06540; // da mặc định/tông nâu đồng nhẹ
            case SIEU_SAIYAN -> 0x6A00C8; // tím
            case THO_MO -> switch (getType()) {
                case HELMET -> 0xFFD21F; // mũ vàng
                default -> 0x26A641; // áo/quần/giày xanh lá
            };
            case NHAM_DAN -> 0xFF5A1F; // đỏ cam
            default -> 0xFFFFFF;
        };
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        applyBuiltInEnchantments(stack);
        return stack;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        applyBuiltInEnchantments(stack);
    }

    private void applyBuiltInEnchantments(ItemStack stack) {
        if (mysticTier == MysticTier.SIEU_SAIYAN && stack.getEnchantmentLevel(Enchantments.THORNS) < 5) {
            stack.enchant(Enchantments.THORNS, 5);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Bộ: " + mysticTier.getDisplayName()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(getFullSetHint(mysticTier)).withStyle(ChatFormatting.YELLOW));
        if (mysticTier == MysticTier.SIEU_SAIYAN) {
            tooltip.add(Component.literal("Mặc định có Gai V trên giáp").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        if (AppearanceManager.getAppearanceId(stack) != null) {
            tooltip.add(Component.literal("Ngoại hình: " + AppearanceManager.getAppearanceId(stack)).withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String appearanceTexture = AppearanceManager.getAppearanceArmorTexture(stack, slot, type);
        if (appearanceTexture != null) {
            return appearanceTexture;
        }
        return super.getArmorTexture(stack, entity, slot, type);
    }

    private String getFullSetHint(MysticTier tier) {
        return switch (tier) {
            case TRUYEN_THUYET -> "Mặc full set để kích hoạt Đề Kháng I";
            case TOI_CAO -> "Mặc full set để kích hoạt Đề Kháng III";
            case THUONG_CO -> "Mặc full set để kích hoạt Đề Kháng V";
            case THIEN_HA -> "Mặc full set để kích hoạt Đề Kháng V";
            case NHAM_DAN -> "Full set: +20 HP, Sức Mạnh II, Đề Kháng I, Hấp Thụ III";
            case HUYEN_THOAI -> "Full set: +20 tim, Sức Mạnh II";
            case SIEU_SAIYAN -> "Full set: +50 tim, Đề Kháng III, Hấp Thụ V, Hồi Phục II";
            case THO_MO -> "Full set: +20 HP, Đào Nhanh V, Nhảy Cao II, Tốc Độ III";
        };
    }
}
