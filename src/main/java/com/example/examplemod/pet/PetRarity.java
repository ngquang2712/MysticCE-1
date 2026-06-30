package com.example.examplemod.pet;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum PetRarity {
    TRUYEN_THUYET("truyen_thuyet", "Truyền Thuyết", 10, 40.0D, 10.0D, 5.0D, 60.0D, 50, ChatFormatting.GOLD),
    TOI_CAO("toi_cao", "Tối Cao", 30, 100.0D, 15.0D, 10.0D, 30.0D, 120, ChatFormatting.AQUA),
    THUONG_CO("thuong_co", "Thượng Cổ", 50, 150.0D, 25.0D, 15.0D, 10.0D, 0, ChatFormatting.DARK_PURPLE);

    private final String id;
    private final String displayName;
    private final int maxLevel;
    private final double baseHealth;
    private final double baseAttack;
    private final double baseArmor;
    private final double captureChancePercent;
    private final int maxTargetHP;
    private final ChatFormatting color;

    PetRarity(String id, String displayName, int maxLevel, double baseHealth, double baseAttack, double baseArmor, double captureChancePercent, int maxTargetHP, ChatFormatting color) {
        this.id = id;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.baseHealth = baseHealth;
        this.baseAttack = baseAttack;
        this.baseArmor = baseArmor;
        this.captureChancePercent = captureChancePercent;
        this.maxTargetHP = maxTargetHP;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayComponent() {
        return Component.literal(displayName).withStyle(color);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getBaseHealth() {
        return baseHealth;
    }

    public double getBaseAttack() {
        return baseAttack;
    }

    public double getBaseArmor() {
        return baseArmor;
    }

    public double getHealthAtLevel(int level) {
        return baseHealth + Math.max(0, level - 1) * 2.0D;
    }

    public double getAttackAtLevel(int level) {
        return baseAttack + Math.max(0, level - 1) * 0.75D;
    }

    public double getArmorAtLevel(int level) {
        return baseArmor + Math.max(0, level - 1) * 0.25D;
    }

    public int getExpToNextLevel(int level) {
        return 50 + level * 25;
    }

    public double getCaptureChancePercent() {
        return captureChancePercent;
    }

    /**
     * Max target HP allowed for capture. 0 means no limit.
     */
    public int getMaxTargetHP() {
        return maxTargetHP;
    }

    public static PetRarity byId(String id) {
        for (PetRarity rarity : values()) {
            if (rarity.id.equals(id)) {
                return rarity;
            }
        }
        return TRUYEN_THUYET;
    }
}
