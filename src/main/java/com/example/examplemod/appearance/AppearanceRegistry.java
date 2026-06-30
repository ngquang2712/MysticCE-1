package com.example.examplemod.appearance;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class AppearanceRegistry {
    private static final List<AppearanceAdapter> ADAPTERS = new ArrayList<>();
    private static final AppearanceAdapter VANILLA = new VanillaArmorAppearanceAdapter();

    static {
        register(new CustomModelNoticeAdapter());
        register(VANILLA);
    }

    private AppearanceRegistry() {
    }

    public static void register(AppearanceAdapter adapter) {
        ADAPTERS.add(adapter);
    }

    public static AppearanceAdapter getAdapter(ResourceLocation itemId, ArmorItem armorItem) {
        for (AppearanceAdapter adapter : ADAPTERS) {
            if (adapter.matches(itemId, armorItem)) {
                return adapter;
            }
        }
        return VANILLA;
    }

    @Nullable
    public static String getArmorTexture(ItemStack mysticStack, ResourceLocation appearanceId, ArmorItem armorItem, EquipmentSlot slot, @Nullable String type) {
        return getAdapter(appearanceId, armorItem).getArmorTexture(mysticStack, appearanceId, armorItem, slot, type);
    }

    public static String getStatus(ResourceLocation itemId, ArmorItem armorItem) {
        return getAdapter(itemId, armorItem).getStatus(itemId, armorItem);
    }
}
