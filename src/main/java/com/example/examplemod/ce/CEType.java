package com.example.examplemod.ce;

import net.minecraft.world.item.ItemStack;

public enum CEType {

    BONG_LANH("Bỏng Lạnh", CERarity.THACH_DAU, CEApplyType.WEAPON, 4),
    HO_DAU("Hổ Đấu", CERarity.THACH_DAU, CEApplyType.SWORD_AXE, 4),
    CO_DOC("Cô Độc", CERarity.THACH_DAU, CEApplyType.SWORD_AXE, 1),
    MA_SOI("Ma Sói", CERarity.THACH_DAU, CEApplyType.SWORD_AXE, 4),
    BAN_TIA("Bắn Tỉa", CERarity.THACH_DAU, CEApplyType.BOW, 5),
    AN_KHANG("An Khang", CERarity.THACH_DAU, CEApplyType.LEGGINGS, 5),
    BUA_YEU("Bùa Yêu", CERarity.THACH_DAU, CEApplyType.HELMET, 3),
    VAY_RONG("Vảy Rồng", CERarity.THACH_DAU, CEApplyType.CHESTPLATE, 3),

    THIEN_MENH("Thiên Mệnh", CERarity.THACH_DAU, CEApplyType.HELMET, 3),
    QUY_CHAN("Quy Chân", CERarity.THACH_DAU, CEApplyType.CHESTPLATE, 3),
    VAN_KIEM("Vạn Kiếm", CERarity.THACH_DAU, CEApplyType.SWORD, 4),
    NHAT_NGUYET("Nhật Nguyệt", CERarity.THACH_DAU, CEApplyType.LEGGINGS, 5),
    PHA_KHONG("Phá Không", CERarity.THACH_DAU, CEApplyType.BOOTS, 1),
    KHAI_THIEN("Khai Thiên", CERarity.THACH_DAU, CEApplyType.AXE, 3),
    XA_KICH("Xạ Kích", CERarity.THACH_DAU, CEApplyType.BOW, 5),
    HOANG_BAO("Hoàng Bào", CERarity.THACH_DAU, CEApplyType.WEAPON, 3);

    private final String displayName;
    private final CERarity rarity;
    private final CEApplyType applyType;
    private final int maxLevel;

    CEType(String displayName, CERarity rarity, CEApplyType applyType, int maxLevel) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.applyType = applyType;
        this.maxLevel = maxLevel;
    }

    public String getDisplayName() { return displayName; }
    public CERarity getRarity() { return rarity; }
    public CEApplyType getApplyType() { return applyType; }
    public int getMaxLevel() { return maxLevel; }
    public boolean canApply(ItemStack stack) { return applyType.canApply(stack); }
}
