package com.example.examplemod.mystic;

public enum MysticTier {
    TRUYEN_THUYET("Truyền Thuyết", 0, 0.15F, 0.0D),
    TOI_CAO("Tối Cao", 2, 0.20F, 0.0D),
    THUONG_CO("Thượng Cổ", 4, 0.25F, 0.0D),
    THIEN_HA("Thiên Hà", 4, 0.30F, 100.0D),
    NHAM_DAN("Nhâm Dần", 0, 0.0F, 20.0D),
    HUYEN_THOAI("Huyền Thoại", -1, 0.0F, 40.0D),
    SIEU_SAIYAN("Siêu Saiyan", 2, 0.0F, 100.0D),
    THO_MO("Thợ Mỏ", -1, 0.0F, 20.0D);

    private final String displayName;
    private final int resistanceAmplifier;
    private final float weaponDamageMultiplier;
    private final double setHealthBonus;

    MysticTier(String displayName, int resistanceAmplifier, float weaponDamageMultiplier, double setHealthBonus) {
        this.displayName = displayName;
        this.resistanceAmplifier = resistanceAmplifier;
        this.weaponDamageMultiplier = weaponDamageMultiplier;
        this.setHealthBonus = setHealthBonus;
    }

    public String getDisplayName() { return displayName; }
    public int getResistanceAmplifier() { return resistanceAmplifier; }
    public float getWeaponDamageMultiplier() { return weaponDamageMultiplier; }
    public float getSetDamageBonus() { return weaponDamageMultiplier; }
    public double getSetHealthBonus() { return setHealthBonus; }
}
