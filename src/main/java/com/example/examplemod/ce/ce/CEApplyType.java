package com.example.examplemod.ce;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.EquipmentSlot;

public enum CEApplyType {

    SWORD,
    AXE,
    BOW,

    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,

    ARMOR,
    WEAPON,

    SWORD_AXE,
    CHESTPLATE_LEGGINGS;

    public boolean canApply(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return switch (this) {
            case SWORD -> stack.getItem() instanceof SwordItem;

            case AXE -> stack.getItem() instanceof AxeItem;

            case BOW -> stack.getItem() instanceof BowItem;

            case WEAPON ->
                    stack.getItem() instanceof SwordItem
                            || stack.getItem() instanceof AxeItem
                            || stack.getItem() instanceof BowItem;

            case SWORD_AXE ->
                    stack.getItem() instanceof SwordItem
                            || stack.getItem() instanceof AxeItem;

            case ARMOR -> stack.getItem() instanceof ArmorItem;

            case HELMET ->
                    stack.getItem() instanceof ArmorItem armorItem
                            && armorItem.getEquipmentSlot() == EquipmentSlot.HEAD;

            case CHESTPLATE ->
                    stack.getItem() instanceof ArmorItem armorItem
                            && armorItem.getEquipmentSlot() == EquipmentSlot.CHEST;

            case LEGGINGS ->
                    stack.getItem() instanceof ArmorItem armorItem
                            && armorItem.getEquipmentSlot() == EquipmentSlot.LEGS;

            case BOOTS ->
                    stack.getItem() instanceof ArmorItem armorItem
                            && armorItem.getEquipmentSlot() == EquipmentSlot.FEET;

            case CHESTPLATE_LEGGINGS ->
                    stack.getItem() instanceof ArmorItem armorItem
                            && (
                            armorItem.getEquipmentSlot() == EquipmentSlot.CHEST
                                    || armorItem.getEquipmentSlot() == EquipmentSlot.LEGS
                    );
        };
    }
}