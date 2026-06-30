package com.example.examplemod.appearance;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Detection-only adapter for mods that commonly use custom armor models/renderers.
 * It intentionally returns null texture so the game falls back instead of rendering broken armor.
 * Real 3D support for each mod can be added later by replacing this with a dedicated client renderer adapter.
 */
public class CustomModelNoticeAdapter implements AppearanceAdapter {
    private static final Set<String> CUSTOM_MODEL_MODS = Set.of(
            "cataclysm",
            "iceandfire",
            "legendmonster",
            "legend_monster",
            "mowziesmobs",
            "born_in_chaos_v1",
            "born_in_chaos",
            "l_enders_cataclysm"
    );

    @Override
    public boolean matches(ResourceLocation itemId, ArmorItem armorItem) {
        return CUSTOM_MODEL_MODS.contains(itemId.getNamespace());
    }

    @Override
    @Nullable
    public String getArmorTexture(ItemStack mysticStack, ResourceLocation appearanceId, ArmorItem appearanceArmor, EquipmentSlot slot, @Nullable String type) {
        return null;
    }

    @Override
    public String getStatus(ResourceLocation itemId, ArmorItem armorItem) {
        return "custom 3D/model riêng - cần adapter riêng để hiện đủ sừng/cánh/model";
    }
}
