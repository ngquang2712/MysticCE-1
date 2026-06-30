package com.example.examplemod.cultivation;

public enum CultivationPillType {
    TU_KHI_DAN("Tụ Khí Đan", 500L, false),
    NGUNG_KHI_DAN("Ngưng Khí Đan", 2000L, false),
    LINH_NGUYEN_DAN("Linh Nguyên Đan", 10000L, false),
    HUYEN_NGUYEN_DAN("Huyền Nguyên Đan", 50000L, false),
    DIA_NGUYEN_DAN("Địa Nguyên Đan", 150000L, false),
    THIEN_NGUYEN_DAN("Thiên Nguyên Đan", 300000L, false),
    DAO_NGUYEN_DAN("Đạo Nguyên Đan", 700000L, false),
    AM_DUONG_DAN("Âm Dương Đan", 1500000L, false),
    HON_NGUYEN_DAN("Hỗn Nguyên Đan", 3000000L, false),
    DAI_DAO_DAN("Đại Đạo Đan", 8000000L, false),
    TIEN_NGUYEN_DAN("Tiên Nguyên Đan", 20000000L, false),
    DE_NGUYEN_DAN("Đế Nguyên Đan", 50000000L, false),

    HO_MENH_DAN("Hộ Mệnh Đan", 0L, true),
    NIET_BAN_DAN("Niết Bàn Đan", 0L, true),
    DAI_NIET_BAN_DAN("Đại Niết Bàn Đan", 0L, true),
    THIEN_MENH_DAN("Thiên Mệnh Đan", 0L, true);

    private final String displayName;
    private final long linhKhi;
    private final boolean protection;

    CultivationPillType(String displayName, long linhKhi, boolean protection) {
        this.displayName = displayName;
        this.linhKhi = linhKhi;
        this.protection = protection;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getLinhKhi() {
        return linhKhi;
    }

    public boolean isProtection() {
        return protection;
    }
}
