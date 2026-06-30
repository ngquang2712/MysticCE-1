package com.example.examplemod.mystic;

public enum MagicFireType {
    HEART_I("Lửa Phép Tim I", 10.0D, 0.0F),
    HEART_II("Lửa Phép Tim II", 20.0D, 0.0F),
    HEART_III("Lửa Phép Tim III", 30.0D, 0.0F),
    DAMAGE_I("Lửa Phép Sát Thương I", 0.0D, 5.0F),
    DAMAGE_II("Lửa Phép Sát Thương II", 0.0D, 10.0F),
    DAMAGE_III("Lửa Phép Sát Thương III", 0.0D, 15.0F);

    private final String displayName;
    private final double healthBonus;
    private final float damageBonus;

    MagicFireType(String displayName, double healthBonus, float damageBonus) {
        this.displayName = displayName;
        this.healthBonus = healthBonus;
        this.damageBonus = damageBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getHealthBonus() {
        return healthBonus;
    }

    public float getDamageBonus() {
        return damageBonus;
    }
}
