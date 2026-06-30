package com.example.examplemod.appearance;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class VanillaArmorAppearanceAdapter implements AppearanceAdapter {
    @Override
    public boolean matches(ResourceLocation itemId, ArmorItem armorItem) {
        return true;
    }

    @Override
    @Nullable
    public String getArmorTexture(ItemStack mysticStack, ResourceLocation appearanceId, ArmorItem appearanceArmor, EquipmentSlot slot, @Nullable String type) {
        String materialName = appearanceArmor.getMaterial().getName();
        String namespace = appearanceId.getNamespace();
        String pathName = materialName;

        ResourceLocation materialId = ResourceLocation.tryParse(materialName);
        if (materialId != null && materialName.contains(":")) {
            namespace = materialId.getNamespace();
            pathName = materialId.getPath();
        } else {
            ResourceLocation registeredId = ForgeRegistries.ITEMS.getKey(appearanceArmor);
            if (registeredId != null) {
                namespace = registeredId.getNamespace();
            }
        }

        int layer = slot == EquipmentSlot.LEGS ? 2 : 1;
        String suffix = type == null ? "" : "_" + type;
        return namespace + ":textures/models/armor/" + pathName + "_layer_" + layer + suffix + ".png";
    }
}
