package com.example.examplemod.ce;

public enum CERarity {

    HIEM("Hiếm"),
    HUYEN_THOAI("Huyền Thoại"),
    TRUYEN_THUYET("Truyền Thuyết"),
    THACH_DAU("Thách Đấu"),
    MA_VANG("Mạ Vàng");

    private final String displayName;

    CERarity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}