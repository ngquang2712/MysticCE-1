package com.example.examplemod.mystic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.Item;

public class ShieldHelper {
    public static final String SHIELD_ENHANCE_LIST = "MYSTIC_SHIELD_ENHANCE";

    public static boolean addEnhance(ItemStack shieldStack, ShieldEnhanceType enhanceType) {
        if (shieldStack.isEmpty()) return false;
        Item item = shieldStack.getItem();
        if (!(item instanceof ShieldItem)) return false;

        CompoundTag tag = shieldStack.getOrCreateTag();
        ListTag list = tag.getList(SHIELD_ENHANCE_LIST, Tag.TAG_COMPOUND);

        // prevent duplicates of same type
        for (int i = 0; i < list.size(); i++) {
            if (list.getCompound(i).getString("ENHANCE_ID").equals(enhanceType.name())) {
                return false;
            }
        }

        CompoundTag e = new CompoundTag();
        e.putString("ENHANCE_ID", enhanceType.name());
        list.add(e);
        tag.put(SHIELD_ENHANCE_LIST, list);
        return true;
    }

    public static ListTag getEnhanceList(ItemStack shieldStack) {
        if (shieldStack.isEmpty() || !shieldStack.hasTag()) return new ListTag();
        return shieldStack.getTag().getList(SHIELD_ENHANCE_LIST, Tag.TAG_COMPOUND);
    }
}
