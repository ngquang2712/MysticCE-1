package com.example.examplemod.mystic;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public enum MysticArmorMaterial implements ArmorMaterial {
    TRUYEN_THUYET("truyen_thuyet", 42, 4, 7, 9, 4, 20, 3.0F, 0.08F),
    TOI_CAO("toi_cao", 50, 5, 8, 10, 5, 25, 4.0F, 0.12F),
    THUONG_CO("thuong_co", 60, 6, 9, 12, 6, 30, 5.0F, 0.16F),
    THIEN_HA("thien_ha", 70, 9, 12, 15, 9, 35, 6.0F, 0.20F),
    NHAM_DAN("nham_dan", 40, 4, 8, 6, 4, 15, 3.0F, 0.08F),
    HUYEN_THOAI("huyen_thoai", 40, 3, 8, 6, 3, 15, 3.0F, 0.08F),
    SIEU_SAIYAN("sieu_saiyan", 55, 4, 8, 8, 4, 25, 4.0F, 0.12F),
    THO_MO("tho_mo", 35, 3, 6, 6, 3, 15, 2.0F, 0.04F);

    private static final Map<ArmorItem.Type, Integer> HEALTH_PER_SLOT = new EnumMap<>(ArmorItem.Type.class);

    static {
        HEALTH_PER_SLOT.put(ArmorItem.Type.BOOTS, 13);
        HEALTH_PER_SLOT.put(ArmorItem.Type.LEGGINGS, 15);
        HEALTH_PER_SLOT.put(ArmorItem.Type.CHESTPLATE, 16);
        HEALTH_PER_SLOT.put(ArmorItem.Type.HELMET, 11);
    }

    private final String name;
    private final int durabilityMultiplier;
    private final Map<ArmorItem.Type, Integer> protection;
    private final int enchantmentValue;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    MysticArmorMaterial(String name, int durabilityMultiplier, int helmetDef, int chestDef, int legsDef, int bootsDef, int enchantmentValue, float toughness, float knockbackResistance) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protection = new EnumMap<>(ArmorItem.Type.class);
        this.protection.put(ArmorItem.Type.HELMET, helmetDef);
        this.protection.put(ArmorItem.Type.CHESTPLATE, chestDef);
        this.protection.put(ArmorItem.Type.LEGGINGS, legsDef);
        this.protection.put(ArmorItem.Type.BOOTS, bootsDef);
        this.enchantmentValue = enchantmentValue;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = () -> Ingredient.of(Items.NETHERITE_INGOT);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) { return HEALTH_PER_SLOT.get(type) * durabilityMultiplier; }
    @Override
    public int getDefenseForType(ArmorItem.Type type) { return protection.get(type); }
    @Override
    public int getEnchantmentValue() { return enchantmentValue; }
    @Override
    public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
    @Override
    public Ingredient getRepairIngredient() { return repairIngredient.get(); }

    @Override
    public String getName() {
        return switch (this) {
            case TRUYEN_THUYET -> "diamond";
            case TOI_CAO -> "iron";
            case THUONG_CO -> "gold";
            case THIEN_HA -> "netherite";
            case NHAM_DAN, HUYEN_THOAI, SIEU_SAIYAN, THO_MO -> "leather";
        };
    }

    @Override
    public float getToughness() { return toughness; }
    @Override
    public float getKnockbackResistance() { return knockbackResistance; }
}
