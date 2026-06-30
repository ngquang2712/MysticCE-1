package com.example.examplemod.cultivation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class CultivationData {
    private static final String ROOT = "MysticCultivation";
    private static final String REALM = "Realm";
    private static final String STAGE = "Stage";
    private static final String LINH_KHI = "LinhKhi";
    private static final String PROTECTION = "Protection";
    private static final String APCHE_COOLDOWN = "ApCheCooldown";
    private static final String DIETSAT_COOLDOWN = "DietSatCooldown";
    private static final String DINH_COOLDOWN = "DinhCooldown";
    private static final String HO_PHONG_COOLDOWN = "HoPhongHoanVuCooldown";
    private static final String RAC_DAU_COOLDOWN = "RacDauThanhBinhCooldown";
    private static final String TU_BAO_COOLDOWN = "TuBaoCooldown";
    private static final String ITEM_SHOW_COOLDOWN = "ItemShowCooldown";
    private static final String LAST_W_PRESS = "LastWPress";
    private static final String FROZEN_UNTIL = "FrozenUntil";
    private static final String FROZEN_X = "FrozenX";
    private static final String FROZEN_Y = "FrozenY";
    private static final String FROZEN_Z = "FrozenZ";
    private static final String VIRTUAL_HP = "VirtualHp";
    private static final String VIRTUAL_HP_MAX = "VirtualHpMax";

    private CultivationData() {}

    public static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) {
            CompoundTag tag = new CompoundTag();
            tag.putString(REALM, CultivationRealm.LUYEN_KHI.name());
            tag.putInt(STAGE, 1);
            tag.putLong(LINH_KHI, 0L);
            persistent.put(ROOT, tag);
        }
        CompoundTag tag = persistent.getCompound(ROOT);
        if (!tag.contains(REALM)) tag.putString(REALM, CultivationRealm.LUYEN_KHI.name());
        if (!tag.contains(STAGE)) tag.putInt(STAGE, 1);
        if (!tag.contains(LINH_KHI)) tag.putLong(LINH_KHI, 0L);
        return tag;
    }

    public static CultivationRealm getRealm(Player player) {
        return CultivationRealm.safeValueOf(root(player).getString(REALM));
    }

    public static int getStage(Player player) {
        CultivationRealm realm = getRealm(player);
        int stage = root(player).getInt(STAGE);
        if (stage < 1) stage = 1;
        if (stage > realm.getMaxStage()) stage = realm.getMaxStage();
        return stage;
    }

    public static void setRealmStage(Player player, CultivationRealm realm, int stage) {
        CompoundTag tag = root(player);
        int safeStage = Math.max(1, Math.min(stage, realm.getMaxStage()));
        tag.putString(REALM, realm.name());
        tag.putInt(STAGE, safeStage);
    }

    public static long getLinhKhi(Player player) {
        return root(player).getLong(LINH_KHI);
    }

    public static void setLinhKhi(Player player, long value) {
        root(player).putLong(LINH_KHI, Math.max(0L, value));
    }

    public static void addLinhKhi(Player player, long amount) {
        if (amount <= 0L) return;
        long current = getLinhKhi(player);
        long next = current > Long.MAX_VALUE - amount ? Long.MAX_VALUE : current + amount;
        setLinhKhi(player, next);
    }

    public static String getProtection(Player player) {
        return root(player).getString(PROTECTION);
    }

    public static void setProtection(Player player, String protection) {
        if (protection == null || protection.isBlank()) root(player).remove(PROTECTION);
        else root(player).putString(PROTECTION, protection);
    }

    public static long getCooldown(Player player, String key) {
        return root(player).getLong(key);
    }

    public static void setCooldown(Player player, String key, long untilGameTime) {
        root(player).putLong(key, untilGameTime);
    }

    public static String apCheCooldownKey() { return APCHE_COOLDOWN; }
    public static String dietSatCooldownKey() { return DIETSAT_COOLDOWN; }
    public static String dinhCooldownKey() { return DINH_COOLDOWN; }
    public static String hoPhongCooldownKey() { return HO_PHONG_COOLDOWN; }
    public static String racDauCooldownKey() { return RAC_DAU_COOLDOWN; }
    public static String tuBaoCooldownKey() { return TU_BAO_COOLDOWN; }
    public static String itemShowCooldownKey() { return ITEM_SHOW_COOLDOWN; }

    public static long getLastWPress(Player player) { return root(player).getLong(LAST_W_PRESS); }
    public static void setLastWPress(Player player, long tick) { root(player).putLong(LAST_W_PRESS, tick); }

    public static long getFrozenUntil(Player player) { return root(player).getLong(FROZEN_UNTIL); }

    public static void freeze(Player player, long untilTick) {
        CompoundTag tag = root(player);
        tag.putLong(FROZEN_UNTIL, untilTick);
        tag.putDouble(FROZEN_X, player.getX());
        tag.putDouble(FROZEN_Y, player.getY());
        tag.putDouble(FROZEN_Z, player.getZ());
    }

    public static float getVirtualHealth(Player player) {
        return root(player).getFloat(VIRTUAL_HP);
    }

    public static void setVirtualHealth(Player player, float value) {
        root(player).putFloat(VIRTUAL_HP, Math.max(0.0F, value));
    }

    public static float getVirtualHealthMax(Player player) {
        return root(player).getFloat(VIRTUAL_HP_MAX);
    }

    public static void setVirtualHealthMax(Player player, float value) {
        root(player).putFloat(VIRTUAL_HP_MAX, Math.max(0.0F, value));
    }

    public static void updateVirtualHealthMax(Player player, float newMax) {
        CompoundTag tag = root(player);
        float oldMax = tag.getFloat(VIRTUAL_HP_MAX);
        float current = tag.getFloat(VIRTUAL_HP);
        newMax = Math.max(0.0F, newMax);

        if (newMax <= 0.0F) {
            tag.putFloat(VIRTUAL_HP_MAX, 0.0F);
            tag.putFloat(VIRTUAL_HP, 0.0F);
            return;
        }

        if (oldMax <= 0.0F) {
            tag.putFloat(VIRTUAL_HP_MAX, newMax);
            tag.putFloat(VIRTUAL_HP, newMax);
            return;
        }

        if (newMax > oldMax) {
            current += newMax - oldMax;
        }

        tag.putFloat(VIRTUAL_HP_MAX, newMax);
        tag.putFloat(VIRTUAL_HP, Math.max(0.0F, Math.min(current, newMax)));
    }

    public static void refillVirtualHealth(Player player) {
        setVirtualHealth(player, getVirtualHealthMax(player));
    }

    public static void clearFreeze(Player player) {
        CompoundTag tag = root(player);
        tag.remove(FROZEN_UNTIL);
        tag.remove(FROZEN_X);
        tag.remove(FROZEN_Y);
        tag.remove(FROZEN_Z);
    }

    public static double frozenX(Player player) { return root(player).getDouble(FROZEN_X); }
    public static double frozenY(Player player) { return root(player).getDouble(FROZEN_Y); }
    public static double frozenZ(Player player) { return root(player).getDouble(FROZEN_Z); }
}
