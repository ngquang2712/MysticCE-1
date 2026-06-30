package com.example.examplemod.mystic;

public enum ShieldEnhanceType {
    SHIELD_I("Mảnh Cường Hóa Khiên I", 20.0D, 5.0D),
    SHIELD_II("Mảnh Cường Hóa Khiên II", 40.0D, 10.0D),
    SHIELD_III("Mảnh Cường Hóa Khiên III", 60.0D, 15.0D);

    private final String displayName;
    private final double healthBonus;
    private final double armorBonus;

    ShieldEnhanceType(String displayName, double healthBonus, double armorBonus) {
        this.displayName = displayName;
        this.healthBonus = healthBonus;
        this.armorBonus = armorBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getHealthBonus() {
        return healthBonus;
    }

    public double getArmorBonus() {
        return armorBonus;
    }
}
