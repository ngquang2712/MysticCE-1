package com.example.examplemod.expansion;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.UUID;

public class EnhancementHelper {
    public enum Type {
        SHARP("sharp", "Sắc bén"), POWER("power", "Power"), ARMOR("armor", "Giáp"), EFFICIENCY("efficiency", "Hiệu suất");
        public final String key;
        public final String display;
        Type(String key, String display) { this.key = key; this.display = display; }
    }

    public static final int MAX = 10;
    private static final String ROOT = "MysticEnhance";
    private static final UUID ARMOR_UUID_HELMET = UUID.fromString("e79d144e-4a59-4635-8601-bdbcc2dff101");
    private static final UUID ARMOR_UUID_CHEST = UUID.fromString("e79d144e-4a59-4635-8601-bdbcc2dff102");
    private static final UUID ARMOR_UUID_LEGS = UUID.fromString("e79d144e-4a59-4635-8601-bdbcc2dff103");
    private static final UUID ARMOR_UUID_FEET = UUID.fromString("e79d144e-4a59-4635-8601-bdbcc2dff104");

    public static Type typeFromMaterial(ItemStack material) {
        if (material.is(ModExpansion.MANH_CUONG_HOA_SAC_BEN.get())) return Type.SHARP;
        if (material.is(ModExpansion.MANH_CUONG_HOA_POWER.get())) return Type.POWER;
        if (material.is(ModExpansion.MANH_CUONG_HOA_GIAP.get())) return Type.ARMOR;
        if (material.is(ModExpansion.MANH_CUONG_HOA_HIEU_SUAT.get())) return Type.EFFICIENCY;
        return null;
    }

    public static boolean canApply(ItemStack target, Type type) {
        if (target.isEmpty() || type == null) return false;
        Item item = target.getItem();
        return switch (type) {
            case SHARP -> item instanceof SwordItem || item instanceof AxeItem;
            case POWER -> item instanceof BowItem;
            case EFFICIENCY -> item instanceof PickaxeItem || item instanceof AxeItem || item instanceof ShovelItem || item instanceof HoeItem;
            case ARMOR -> item instanceof ArmorItem;
        };
    }

    public static int getCount(ItemStack stack, Type type) {
        if (!stack.hasTag()) return 0;
        CompoundTag root = stack.getTag().getCompound(ROOT);
        return Math.max(0, Math.min(MAX, root.getInt(type.key)));
    }

    public static void setCount(ItemStack stack, Type type, int value) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag root = tag.getCompound(ROOT);
        root.putInt(type.key, Math.max(0, Math.min(MAX, value)));
        tag.put(ROOT, root);
    }

    public static int chanceForNext(int current) {
        int next = current + 1;
        if (next <= 3) return 50;
        if (next <= 6) return 30;
        if (next <= 8) return 30;
        if (next == 9) return 20;
        return 10;
    }

    public static ItemStack preview(ItemStack input, Type type) {
        ItemStack out = input.copy();
        applySuccess(out, type);
        return out;
    }

    public static void applySuccess(ItemStack stack, Type type) {
        int current = getCount(stack, type);
        if (current >= MAX) return;
        setCount(stack, type, current + 1);
        switch (type) {
            case SHARP -> setEnchantmentLevel(stack, Enchantments.SHARPNESS, stack.getEnchantmentLevel(Enchantments.SHARPNESS) + 1);
            case POWER -> setEnchantmentLevel(stack, Enchantments.POWER_ARROWS, stack.getEnchantmentLevel(Enchantments.POWER_ARROWS) + 1);
            case EFFICIENCY -> setEnchantmentLevel(stack, Enchantments.BLOCK_EFFICIENCY, stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY) + 1);
            case ARMOR -> applyArmorAttribute(stack, current + 1);
        }
    }

    private static void setEnchantmentLevel(ItemStack stack, Enchantment enchantment, int level) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        enchantments.put(enchantment, Math.max(1, level));
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    private static void applyArmorAttribute(ItemStack stack, int count) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return;
        EquipmentSlot slot = armor.getEquipmentSlot();
        UUID uuid = switch (slot) {
            case HEAD -> ARMOR_UUID_HELMET;
            case CHEST -> ARMOR_UUID_CHEST;
            case LEGS -> ARMOR_UUID_LEGS;
            case FEET -> ARMOR_UUID_FEET;
            default -> ARMOR_UUID_CHEST;
        };
        removeMysticArmorModifier(stack, uuid);
        stack.addAttributeModifier(Attributes.ARMOR, new AttributeModifier(uuid, "Mystic armor enhancement", count, AttributeModifier.Operation.ADDITION), slot);
    }

    private static void removeMysticArmorModifier(ItemStack stack, UUID uuid) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("AttributeModifiers", 9)) return;
        ListTag list = tag.getList("AttributeModifiers", 10);
        for (int i = list.size() - 1; i >= 0; i--) {
            CompoundTag modifier = list.getCompound(i);
            if (modifier.hasUUID("UUID") && NbtUtils.loadUUID(modifier.get("UUID")).equals(uuid)) {
                list.remove(i);
            }
        }
        if (list.isEmpty()) tag.remove("AttributeModifiers");
    }

    public static Component infoLine(ItemStack stack, Type type) {
        int count = getCount(stack, type);
        if (count >= MAX) return Component.literal(type.display + ": " + count + "/" + MAX + " - Đã max").withStyle(ChatFormatting.GOLD);
        return Component.literal(type.display + ": " + count + "/" + MAX + " - Tỉ lệ lần sau: " + chanceForNext(count) + "%").withStyle(ChatFormatting.AQUA);
    }
}
