package com.example.examplemod.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum MysticBossKind {
    HO_PHAP_THUONG_CO(
            "ho_phap_thuong_co",
            "Hộ Pháp Thượng Cổ",
            3.0F,
            10000.0D,
            35.0D,
            20.0D,
            ChatFormatting.GRAY,
            Items.IRON_HELMET,
            Items.IRON_CHESTPLATE,
            Items.IRON_LEGGINGS,
            Items.IRON_BOOTS,
            BossGrade.THUONG_CO
    ),
    THIEN_DAO_HOA_THAN(
            "thien_dao_hoa_than",
            "Thiên Đạo Hóa Thân",
            3.5F,
            20000.0D,
            45.0D,
            25.0D,
            ChatFormatting.GOLD,
            Items.GOLDEN_HELMET,
            Items.GOLDEN_CHESTPLATE,
            Items.GOLDEN_LEGGINGS,
            Items.GOLDEN_BOOTS,
            BossGrade.THIEN_DAO
    ),
    CUU_TINH_CO_THAN(
            "cuu_tinh_co_than",
            "Cửu Tinh Cổ Thần",
            4.0F,
            50000.0D,
            55.0D,
            30.0D,
            ChatFormatting.AQUA,
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS,
            BossGrade.CO_THAN
    ),
    TIEN_DE_THANH_LAM(
            "tien_de_thanh_lam",
            "Tiên Đế Thanh Lâm",
            4.5F,
            100000.0D,
            70.0D,
            40.0D,
            ChatFormatting.DARK_PURPLE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,
            BossGrade.TIEN_DE
    );

    private final String id;
    private final String displayName;
    private final float scale;
    private final double health;
    private final double attack;
    private final double armor;
    private final ChatFormatting color;
    private final Item helmet;
    private final Item chestplate;
    private final Item leggings;
    private final Item boots;
    private final BossGrade grade;

    MysticBossKind(String id, String displayName, float scale, double health, double attack, double armor,
                   ChatFormatting color, Item helmet, Item chestplate, Item leggings, Item boots, BossGrade grade) {
        this.id = id;
        this.displayName = displayName;
        this.scale = scale;
        this.health = health;
        this.attack = attack;
        this.armor = armor;
        this.color = color;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.grade = grade;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public float getScale() { return scale; }
    public double getHealth() { return health; }
    public double getAttack() { return attack; }
    public double getArmor() { return armor; }
    public ChatFormatting getColor() { return color; }
    public Item getHelmet() { return helmet; }
    public Item getChestplate() { return chestplate; }
    public Item getLeggings() { return leggings; }
    public Item getBoots() { return boots; }
    public BossGrade getGrade() { return grade; }

    public boolean canSummonByItem() {
        return this == HO_PHAP_THUONG_CO || this == THIEN_DAO_HOA_THAN;
    }

    public boolean isDisaster() {
        return this == CUU_TINH_CO_THAN || this == TIEN_DE_THANH_LAM;
    }

    public static MysticBossKind byId(String id) {
        if (id == null) return HO_PHAP_THUONG_CO;
        String key = id.trim().toLowerCase().replace("-", "_").replace(" ", "_");
        if (key.equals("hophap") || key.equals("ho_phap")) return HO_PHAP_THUONG_CO;
        if (key.equals("thiendao") || key.equals("thien_dao")) return THIEN_DAO_HOA_THAN;
        if (key.equals("cuutinh") || key.equals("cuu_tinh")) return CUU_TINH_CO_THAN;
        if (key.equals("tiende") || key.equals("tien_de")) return TIEN_DE_THANH_LAM;
        for (MysticBossKind kind : values()) {
            if (kind.id.equalsIgnoreCase(key) || kind.name().equalsIgnoreCase(key)) {
                return kind;
            }
        }
        return HO_PHAP_THUONG_CO;
    }

    public enum BossGrade {
        THUONG_CO(4, 6),
        THIEN_DAO(4, 6),
        CO_THAN(4, 6),
        TIEN_DE(4, 6);

        private final int minDrops;
        private final int maxDrops;

        BossGrade(int minDrops, int maxDrops) {
            this.minDrops = minDrops;
            this.maxDrops = maxDrops;
        }

        public int getMinDrops() { return minDrops; }
        public int getMaxDrops() { return maxDrops; }
    }
}
