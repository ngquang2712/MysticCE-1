package com.example.examplemod.appearance;

import com.example.examplemod.mystic.MysticArmorItem;
import com.example.examplemod.mystic.MysticTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public final class AppearanceManager {
    public static final String APPEARANCE_TAG = "AppearanceItem";
    public static final String COPIED_APPEARANCE_TAG = "CopiedAppearanceItem";
    public static final String COPIED_APPEARANCE_SLOT_TAG = "CopiedAppearanceSlot";
    public static final String COPIED_TRIM_TAG = "CopiedAppearanceTrim";
    public static final String COPIED_HAS_TRIM_TAG = "CopiedAppearanceHasTrim";
    public static final String COPIED_TRIM_MARKER_TAG = "AppearanceCopiedTrim";

    private AppearanceManager() {
    }

    public static boolean hasFullMysticSet(Player player) {
        MysticTier tier = null;

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof MysticArmorItem armorItem)) {
                return false;
            }

            if (tier == null) {
                tier = armorItem.getMysticTier();
            } else if (tier != armorItem.getMysticTier()) {
                return false;
            }
        }

        return tier != null;
    }

    public static ItemStack getEquippedArmor(Player player, ArmorItem.Type type) {
        return player.getItemBySlot(slotForType(type));
    }

    public static EquipmentSlot slotForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
        };
    }

    public static boolean isMysticArmorOfType(ItemStack stack, ArmorItem.Type type) {
        return stack.getItem() instanceof MysticArmorItem armorItem && armorItem.getType() == type;
    }

    @Nullable
    public static ResourceLocation getAppearanceId(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(APPEARANCE_TAG)) {
            return null;
        }

        return ResourceLocation.tryParse(tag.getString(APPEARANCE_TAG));
    }

    public static void setAppearance(ItemStack stack, ResourceLocation itemId) {
        stack.getOrCreateTag().putString(APPEARANCE_TAG, itemId.toString());
    }

    public static void setAppearance(ItemStack stack, ResourceLocation itemId, @Nullable CompoundTag trimTag) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(APPEARANCE_TAG, itemId.toString());
        if (trimTag != null) {
            tag.put("Trim", trimTag.copy());
            tag.putBoolean(COPIED_TRIM_MARKER_TAG, true);
        }
    }

    public static void clearAppearance(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(APPEARANCE_TAG);
            if (tag.getBoolean(COPIED_TRIM_MARKER_TAG)) {
                tag.remove("Trim");
                tag.remove(COPIED_TRIM_MARKER_TAG);
            }
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    @Nullable
    public static ArmorItem getAppearanceArmor(ItemStack stack) {
        ResourceLocation id = getAppearanceId(stack);
        if (id == null) {
            return null;
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item instanceof ArmorItem armorItem) {
            return armorItem;
        }

        return null;
    }

    @Nullable
    public static String getAppearanceArmorTexture(ItemStack mysticStack, EquipmentSlot slot, @Nullable String type) {
        ResourceLocation appearanceId = getAppearanceId(mysticStack);
        if (appearanceId == null) {
            return null;
        }

        Item item = ForgeRegistries.ITEMS.getValue(appearanceId);
        if (!(item instanceof ArmorItem appearanceArmor)) {
            return null;
        }

        return AppearanceRegistry.getArmorTexture(mysticStack, appearanceId, appearanceArmor, slot, type);
    }

    /** Backwards-compatible helper used by older code. */
    @Nullable
    public static String getVanillaStyleArmorTexture(ItemStack mysticStack, boolean legsLayer) {
        return getAppearanceArmorTexture(mysticStack, legsLayer ? EquipmentSlot.LEGS : EquipmentSlot.CHEST, null);
    }

    public static void copyAppearance(Player player, ResourceLocation itemId, ArmorItem.Type type) {
        copyAppearance(player, itemId, type, null);
    }

    public static void copyAppearance(Player player, ResourceLocation itemId, ArmorItem.Type type, @Nullable CompoundTag trimTag) {
        CompoundTag data = player.getPersistentData();
        data.putString(COPIED_APPEARANCE_TAG, itemId.toString());
        data.putString(COPIED_APPEARANCE_SLOT_TAG, type.getName());
        if (trimTag != null) {
            data.put(COPIED_TRIM_TAG, trimTag.copy());
            data.putBoolean(COPIED_HAS_TRIM_TAG, true);
        } else {
            data.remove(COPIED_TRIM_TAG);
            data.putBoolean(COPIED_HAS_TRIM_TAG, false);
        }
    }

    @Nullable
    public static CompoundTag getCopiedTrim(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(COPIED_HAS_TRIM_TAG) || !data.contains(COPIED_TRIM_TAG)) {
            return null;
        }
        return data.getCompound(COPIED_TRIM_TAG).copy();
    }

    @Nullable
    public static CompoundTag getTrim(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Trim")) {
            return null;
        }
        return tag.getCompound("Trim").copy();
    }

    @Nullable
    public static ResourceLocation getCopiedAppearanceId(Player player) {
        String value = player.getPersistentData().getString(COPIED_APPEARANCE_TAG);
        return value.isEmpty() ? null : ResourceLocation.tryParse(value);
    }

    @Nullable
    public static ArmorItem.Type getCopiedAppearanceType(Player player) {
        String value = player.getPersistentData().getString(COPIED_APPEARANCE_SLOT_TAG);
        return typeByName(value);
    }

    @Nullable
    public static ArmorItem.Type typeByName(String name) {
        return switch (name) {
            case "helmet" -> ArmorItem.Type.HELMET;
            case "chestplate" -> ArmorItem.Type.CHESTPLATE;
            case "leggings" -> ArmorItem.Type.LEGGINGS;
            case "boots" -> ArmorItem.Type.BOOTS;
            default -> null;
        };
    }

    public static String getAppearanceStatus(ItemStack stack) {
        ResourceLocation id = getAppearanceId(stack);
        if (id == null) {
            return "mặc định";
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item instanceof ArmorItem armorItem) {
            return AppearanceRegistry.getStatus(id, armorItem);
        }
        return "không phải armor";
    }
}
