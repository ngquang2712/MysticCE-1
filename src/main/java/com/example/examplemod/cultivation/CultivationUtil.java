package com.example.examplemod.cultivation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.text.DecimalFormat;
import java.util.Locale;

public final class CultivationUtil {
    public static final int MAX_RENDERED_HEARTS = 40;
    public static final int VANILLA_BASE_HEARTS = 10;
    public static final int MAX_RENDERED_BONUS_HEARTS = MAX_RENDERED_HEARTS - VANILLA_BASE_HEARTS;
    private static final long[][] BREAKTHROUGH_COSTS = new long[][]{
            {100L, 250L, 500L, 900L, 1500L, 2300L, 3500L, 5000L, 8000L},
            {12000L, 18000L, 25000L, 40000L},
            {60000L, 90000L, 130000L, 200000L},
            {300000L, 450000L, 650000L, 1000000L},
            {1500000L, 2200000L, 3000000L, 5000000L},
            {7000000L, 10000000L, 15000000L, 25000000L},
            {35000000L, 50000000L, 75000000L, 120000000L},
            {180000000L, 250000000L, 350000000L, 500000000L},
            {700000000L, 900000000L, 1200000000L, 1800000000L},
            {2500000000L, 3500000000L, 5000000000L, 8000000000L},
            {12000000000L, 18000000000L, 25000000000L, 35000000000L, 100000000000L},
            {Long.MAX_VALUE}
    };

    private static final double[][] SUCCESS_CHANCES = new double[][]{
            {100D, 100D, 100D, 100D, 100D, 100D, 100D, 100D, 100D},
            {100D, 93D, 86D, 80D},
            {80D, 77D, 73D, 70D},
            {70D, 63D, 56D, 50D},
            {50D, 43D, 36D, 30D},
            {30D, 27D, 23D, 20D},
            {20D, 18D, 16D, 15D},
            {15D, 13D, 11D, 10D},
            {10D, 8D, 6D, 5D},
            {5D, 5D, 5D, 5D},
            {2D, 2D, 2D, 2D, 0.005D},
            {0D}
    };

    private CultivationUtil() {}

    public static long getBreakthroughCost(CultivationRealm realm, int stage) {
        if (realm == CultivationRealm.TIEN_DE) return Long.MAX_VALUE;
        long[] costs = BREAKTHROUGH_COSTS[realm.ordinal()];
        int index = Math.max(0, Math.min(stage - 1, costs.length - 1));
        return costs[index];
    }

    public static double getSuccessChance(CultivationRealm realm, int stage) {
        if (realm == CultivationRealm.TIEN_DE) return 0D;
        double[] chances = SUCCESS_CHANCES[realm.ordinal()];
        int index = Math.max(0, Math.min(stage - 1, chances.length - 1));
        return chances[index];
    }

    public static boolean canAdvance(CultivationRealm realm, int stage) {
        return realm != CultivationRealm.TIEN_DE;
    }

    public static String formatNumber(long value) {
        return String.format(Locale.US, "%,d", value);
    }

    public static String formatChance(double chance) {
        if (chance < 0.01D && chance > 0D) return new DecimalFormat("0.######").format(chance) + "%";
        if (Math.abs(chance - Math.round(chance)) < 0.0001D) return ((long)Math.round(chance)) + "%";
        return new DecimalFormat("0.###").format(chance) + "%";
    }

    public static MutableComponent realmComponent(CultivationRealm realm, int stage) {
        return Component.literal(realm.formatStage(stage)).withStyle(realm.getColor(), ChatFormatting.BOLD);
    }

    public static CultivationRealm nextRealm(CultivationRealm realm) {
        return realm.next();
    }

    public static CultivationRealm getNextRealmAfterFinal(CultivationRealm realm) {
        return realm.next();
    }

    public static boolean isFinalStage(CultivationRealm realm, int stage) {
        return stage >= realm.getMaxStage();
    }

    public static String nextRealmOrStageName(CultivationRealm realm, int stage) {
        if (realm == CultivationRealm.TIEN_DE) return "Đã đạt Tiên Đế";
        if (stage < realm.getMaxStage()) {
            return realm.formatStage(stage + 1);
        }
        CultivationRealm next = realm.next();
        if (next == null) return "Đã đạt tối đa";
        return next.formatStage(1);
    }

    public static int totalBonusHearts(CultivationRealm realm, int stage) {
        int total = 0;
        for (CultivationRealm r : CultivationRealm.values()) {
            if (r == CultivationRealm.TIEN_DE) break;
            if (realm == CultivationRealm.TIEN_DE || r.ordinal() < realm.ordinal()) {
                total += r.getHeartsPerStage() * r.getMaxStage();
            } else if (r == realm) {
                total += r.getHeartsPerStage() * Math.min(stage, r.getMaxStage());
                break;
            }
        }
        return total;
    }

    public static int displayedBonusHearts(CultivationRealm realm, int stage) {
        return Math.min(totalBonusHearts(realm, stage), MAX_RENDERED_BONUS_HEARTS);
    }

    public static int virtualBonusHearts(CultivationRealm realm, int stage) {
        return Math.max(0, totalBonusHearts(realm, stage) - displayedBonusHearts(realm, stage));
    }

    public static float virtualHealthMax(CultivationRealm realm, int stage) {
        return virtualBonusHearts(realm, stage) * 2.0F;
    }

    public static int totalBonusDamage(CultivationRealm realm, int stage) {
        int total = 0;
        for (CultivationRealm r : CultivationRealm.values()) {
            if (r == CultivationRealm.TIEN_DE) break;
            if (realm == CultivationRealm.TIEN_DE || r.ordinal() < realm.ordinal()) {
                total += r.getDamagePerStage() * r.getMaxStage();
            } else if (r == realm) {
                total += r.getDamagePerStage() * Math.min(stage, r.getMaxStage());
                break;
            }
        }
        return total;
    }

    public static void sendCultivationPanel(Player player) {
        CultivationRealm realm = CultivationData.getRealm(player);
        int stage = CultivationData.getStage(player);
        long linhKhi = CultivationData.getLinhKhi(player);
        long cost = getBreakthroughCost(realm, stage);
        double chance = getSuccessChance(realm, stage);
        int hp = totalBonusHearts(realm, stage);
        int renderedHp = displayedBonusHearts(realm, stage);
        int virtualHpHearts = virtualBonusHearts(realm, stage);
        float virtualHp = CultivationData.getVirtualHealth(player);
        float virtualHpMax = CultivationData.getVirtualHealthMax(player);
        int damage = totalBonusDamage(realm, stage);
        String protection = CultivationData.getProtection(player);

        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.literal("━━━━━━━━━━━━ TU TIÊN ━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Người chơi: ").withStyle(ChatFormatting.GRAY).append(Component.literal(player.getName().getString()).withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Cảnh giới: ").withStyle(ChatFormatting.GRAY).append(realmComponent(realm, stage)));
        if (realm == CultivationRealm.TIEN_DE) {
            player.sendSystemMessage(Component.literal("Linh khí: ").withStyle(ChatFormatting.GRAY).append(Component.literal(formatNumber(linhKhi)).withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("Trạng thái: Bất tử, vạn đạo quy phục.").withStyle(ChatFormatting.GOLD));
        } else {
            player.sendSystemMessage(Component.literal("Linh khí: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(formatNumber(linhKhi) + " / " + formatNumber(cost)).withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("Mục tiêu: ").withStyle(ChatFormatting.GRAY).append(Component.literal(nextRealmOrStageName(realm, stage)).withStyle(ChatFormatting.YELLOW)));
            player.sendSystemMessage(Component.literal("Tỉ lệ đột phá: ").withStyle(ChatFormatting.GRAY).append(Component.literal(formatChance(chance)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        }
        player.sendSystemMessage(Component.literal("Chỉ số tu tiên: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("+" + hp + " tim, +" + damage + " dame").withStyle(ChatFormatting.RED)));
        if (virtualHpMax > 0.0F) {
            player.sendSystemMessage(Component.literal("HP Tu Tiên: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(formatNumber((long) virtualHp) + " / " + formatNumber((long) virtualHpMax)).withStyle(ChatFormatting.RED))
                    .append(Component.literal(" | Tim hiển thị: +" + renderedHp + ", ẩn: +" + virtualHpHearts).withStyle(ChatFormatting.DARK_GRAY)));
        }
        player.sendSystemMessage(Component.literal("Đặc quyền: ").withStyle(ChatFormatting.GRAY).append(Component.literal(privilegeSummary(realm)).withStyle(ChatFormatting.LIGHT_PURPLE)));
        player.sendSystemMessage(Component.literal("Đan bảo vệ: ").withStyle(ChatFormatting.GRAY).append(Component.literal(protection == null || protection.isBlank() ? "Không có" : protection).withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.empty());
    }

    public static String privilegeSummary(CultivationRealm realm) {
        if (realm == CultivationRealm.TIEN_DE) return "Bất tử, aura đế uy, miễn áp chế/diệt sát/định thân, toàn bộ đặc quyền Đạp Thiên, /racdauthanhbinh";
        if (realm.isAtLeast(CultivationRealm.BUOC_4)) return "Định thân, aura Đạp Thiên, thuấn di phím R, /dietsat, /apche, /thiennhan, [i], /tubao, bay, không đói";
        if (realm.isAtLeast(CultivationRealm.BUOC_3)) return "Aura Khuy Niết, thuấn di phím R, /hophonghoanvu, /dietsat, /apche, /thiennhan, [i], /tubao, bay, không đói";
        if (realm.isAtLeast(CultivationRealm.DUONG_THUC)) return "/apche, /thiennhan, [i], bay, không đói, nhìn đêm";
        if (realm.isAtLeast(CultivationRealm.AM_HU)) return "/thiennhan, [i], bay, không đói, nhìn đêm";
        if (realm.isAtLeast(CultivationRealm.VAN_DINH)) return "/thiennhan, [i], bay, không đói";
        if (realm.isAtLeast(CultivationRealm.NGUYEN_ANH)) return "Bay, không đói";
        if (realm.isAtLeast(CultivationRealm.KIM_DAN)) return "Không đói";
        return "Chưa có đặc quyền";
    }

    public static int mobKillReward(double maxHealth) {
        if (maxHealth < 20D) return 5;
        if (maxHealth < 50D) return 10;
        if (maxHealth < 100D) return 50;
        if (maxHealth < 150D) return 100;
        if (maxHealth < 200D) return 150;
        if (maxHealth < 400D) return 200;
        if (maxHealth < 500D) return 300;
        if (maxHealth < 1000D) return 500;
        return 1000;
    }

    public static int playerKillReward(CultivationRealm realm, int stage) {
        if (realm == CultivationRealm.LUYEN_KHI) return Math.max(1, Math.min(9, stage));
        int index = Math.max(0, stage - 1);
        return switch (realm) {
            case TRUC_CO -> new int[]{5, 10, 15, 20}[Math.min(index, 3)];
            case KIM_DAN -> new int[]{20, 30, 45, 60}[Math.min(index, 3)];
            case NGUYEN_ANH -> new int[]{60, 120, 180, 240}[Math.min(index, 3)];
            case HOA_THAN -> new int[]{240, 340, 440, 500}[Math.min(index, 3)];
            case ANH_BIEN -> new int[]{600, 800, 1000, 1200}[Math.min(index, 3)];
            case VAN_DINH -> new int[]{1200, 1400, 1600, 1800}[Math.min(index, 3)];
            case AM_HU -> new int[]{1800, 2000, 2200, 2400}[Math.min(index, 3)];
            case DUONG_THUC -> new int[]{2500, 2800, 3200, 3600}[Math.min(index, 3)];
            case BUOC_3 -> new int[]{4000, 5000, 6000, 7000}[Math.min(index, 3)];
            case BUOC_4 -> new int[]{7000, 8000, 9000, 10000, 15000}[Math.min(index, 4)];
            case TIEN_DE -> 0;
            default -> 0;
        };
    }
}
