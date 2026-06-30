package com.example.examplemod.cultivation;

import net.minecraft.ChatFormatting;

public enum CultivationRealm {
    LUYEN_KHI("Luyện Khí", 9, 1, 1, ChatFormatting.GREEN),
    TRUC_CO("Trúc Cơ", 4, 2, 1, ChatFormatting.AQUA),
    KIM_DAN("Kim Đan", 4, 3, 1, ChatFormatting.GOLD),
    NGUYEN_ANH("Nguyên Anh", 4, 5, 2, ChatFormatting.LIGHT_PURPLE),
    HOA_THAN("Hóa Thần", 4, 6, 2, ChatFormatting.DARK_PURPLE),
    ANH_BIEN("Anh Biến", 4, 7, 2, ChatFormatting.RED),
    VAN_DINH("Vấn Đỉnh", 4, 7, 2, ChatFormatting.DARK_RED),
    AM_HU("Âm Hư", 4, 10, 3, ChatFormatting.DARK_PURPLE),
    DUONG_THUC("Dương Thực", 4, 12, 3, ChatFormatting.YELLOW),
    BUOC_3("Khuy Niết", 4, 15, 4, ChatFormatting.BLUE),
    BUOC_4("Đạp Thiên", 5, 17, 4, ChatFormatting.DARK_RED),
    TIEN_DE("Tiên Đế", 1, 0, 0, ChatFormatting.GOLD);

    private static final CultivationRealm[] VALUES = values();

    private final String displayName;
    private final int maxStage;
    private final int heartsPerStage;
    private final int damagePerStage;
    private final ChatFormatting color;

    CultivationRealm(String displayName, int maxStage, int heartsPerStage, int damagePerStage, ChatFormatting color) {
        this.displayName = displayName;
        this.maxStage = maxStage;
        this.heartsPerStage = heartsPerStage;
        this.damagePerStage = damagePerStage;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxStage() {
        return maxStage;
    }

    public int getHeartsPerStage() {
        return heartsPerStage;
    }

    public int getDamagePerStage() {
        return damagePerStage;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public boolean isAtLeast(CultivationRealm other) {
        return this.ordinal() >= other.ordinal();
    }

    public CultivationRealm next() {
        int next = ordinal() + 1;
        return next >= VALUES.length ? null : VALUES[next];
    }

    public CultivationRealm previous() {
        int previous = ordinal() - 1;
        return previous < 0 ? null : VALUES[previous];
    }

    public String formatStage(int stage) {
        if (this == TIEN_DE) {
            return displayName;
        }
        if (this == LUYEN_KHI) {
            return displayName + " tầng " + stage;
        }
        return displayName + " " + stageName(stage);
    }

    public static String stageName(int stage) {
        return switch (stage) {
            case 1 -> "sơ kỳ";
            case 2 -> "trung kỳ";
            case 3 -> "hậu kỳ";
            case 4 -> "viên mãn";
            case 5 -> "đại viên mãn";
            default -> "bậc " + stage;
        };
    }

    public static CultivationRealm safeValueOf(String name) {
        if (name == null) return LUYEN_KHI;
        String normalized = name.trim().toUpperCase()
                .replace(' ', '_')
                .replace('-', '_');
        normalized = normalized
                .replace("KHUY_NIET", "BUOC_3")
                .replace("KHUYNIET", "BUOC_3")
                .replace("DAP_THIEN", "BUOC_4")
                .replace("DAPTHIEN", "BUOC_4");
        try {
            return CultivationRealm.valueOf(normalized);
        } catch (Exception ignored) {
            return LUYEN_KHI;
        }
    }
}
