package com.example.examplemod.appearance;

import com.example.examplemod.mystic.MysticAxeItem;
import com.example.examplemod.mystic.MysticSwordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public final class WeaponAppearanceManager {
    public static final String WEAPON_APPEARANCE_TAG = "WeaponAppearanceItem";
    public static final String COPIED_WEAPON_APPEARANCE_TAG = "CopiedWeaponAppearanceItem";
    public static final String COPIED_WEAPON_TYPE_TAG = "CopiedWeaponAppearanceType";

    private WeaponAppearanceManager() {
    }

    public static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    public static boolean isMysticWeapon(ItemStack stack) {
        return stack.getItem() instanceof MysticSwordItem || stack.getItem() instanceof MysticAxeItem;
    }

    @Nullable
    public static String getWeaponType(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof SwordItem) {
            return "sword";
        }
        if (item instanceof AxeItem) {
            return "axe";
        }
        return null;
    }

    public static boolean sameWeaponType(ItemStack a, ItemStack b) {
        String ta = getWeaponType(a);
        String tb = getWeaponType(b);
        return ta != null && ta.equals(tb);
    }

    public static void copyWeaponAppearance(Player player, ResourceLocation itemId, String weaponType) {
        CompoundTag data = player.getPersistentData();
        data.putString(COPIED_WEAPON_APPEARANCE_TAG, itemId.toString());
        data.putString(COPIED_WEAPON_TYPE_TAG, weaponType);
    }

    @Nullable
    public static ResourceLocation getCopiedWeaponAppearanceId(Player player) {
        String value = player.getPersistentData().getString(COPIED_WEAPON_APPEARANCE_TAG);
        return value.isEmpty() ? null : ResourceLocation.tryParse(value);
    }

    @Nullable
    public static String getCopiedWeaponType(Player player) {
        String value = player.getPersistentData().getString(COPIED_WEAPON_TYPE_TAG);
        return value.isEmpty() ? null : value;
    }

    public static void setWeaponAppearance(ItemStack stack, ResourceLocation itemId) {
        stack.getOrCreateTag().putString(WEAPON_APPEARANCE_TAG, itemId.toString());
    }

    @Nullable
    public static ResourceLocation getWeaponAppearanceId(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(WEAPON_APPEARANCE_TAG)) {
            return null;
        }
        return ResourceLocation.tryParse(tag.getString(WEAPON_APPEARANCE_TAG));
    }

    public static void clearWeaponAppearance(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(WEAPON_APPEARANCE_TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    @Nullable
    public static Item getWeaponAppearanceItem(ItemStack stack) {
        ResourceLocation id = getWeaponAppearanceId(stack);
        if (id == null) {
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(id);
    }
}
