package com.example.examplemod.appearance;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Adapter layer for armor appearances.
 *
 * This project can safely support vanilla-style armor by swapping armor textures.
 * Armor with GeckoLib/custom 3D renderers needs a mod-specific adapter later.
 */
public interface AppearanceAdapter {
    /**
     * @return true when this adapter can handle the supplied appearance armor item.
     */
    boolean matches(ResourceLocation itemId, ArmorItem armorItem);

    /**
     * @return texture path in Forge armor texture format, or null to fallback.
     */
    @Nullable
    String getArmorTexture(ItemStack mysticStack, ResourceLocation appearanceId, ArmorItem appearanceArmor, EquipmentSlot slot, @Nullable String type);

    /**
     * @return short human readable status shown by /appearanceinfo.
     */
    default String getStatus(ResourceLocation itemId, ArmorItem armorItem) {
        return "vanilla/simple texture";
    }
}
