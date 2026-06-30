package com.example.examplemod.cultivation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class CultivationTribulation {
    private static final Map<UUID, ActiveTribulation> ACTIVE = new HashMap<>();
    private static final int DURATION_TICKS = 100;
    private static final Random RANDOM = new Random();

    private CultivationTribulation() {}

    public static boolean isActive(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static void start(ServerPlayer player, CultivationRealm currentRealm, int currentStage, long cost, double chance) {
        long now = player.serverLevel().getGameTime();
        CultivationData.setLinhKhi(player, CultivationData.getLinhKhi(player) - cost);
        ServerBossEvent boss = new ServerBossEvent(
                Component.literal("⚡ THIÊN KIẾP ĐANG GIÁNG XUỐNG ⚡").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                BossEvent.BossBarColor.YELLOW,
                BossEvent.BossBarOverlay.PROGRESS
        );
        boss.addPlayer(player);
        boss.setProgress(1.0F);
        ACTIVE.put(player.getUUID(), new ActiveTribulation(currentRealm, currentStage, chance, now, boss));

        player.server.getPlayerList().broadcastSystemMessage(
                Component.literal("⚡ Thiên kiếp giáng xuống! ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                        .append(player.getDisplayName())
                        .append(Component.literal(" đang đột phá " + currentRealm.formatStage(currentStage) + "!" ).withStyle(ChatFormatting.GOLD)),
                false
        );
    }

    public static void tick(ServerPlayer player) {
        ActiveTribulation active = ACTIVE.get(player.getUUID());
        if (active == null) return;
        long now = player.serverLevel().getGameTime();
        long elapsed = now - active.startTick;
        float progress = Math.max(0.0F, 1.0F - (elapsed / (float) DURATION_TICKS));
        active.boss.setProgress(progress);
        if (elapsed % 20L == 0L) {
            spawnVisualLightning(player);
        }
        if (elapsed >= DURATION_TICKS) {
            resolve(player, active);
        }
    }

    private static void resolve(ServerPlayer player, ActiveTribulation active) {
        ACTIVE.remove(player.getUUID());
        active.boss.removeAllPlayers();

        double roll = RANDOM.nextDouble() * 100.0D;
        boolean success = roll < active.chance;
        String protection = CultivationData.getProtection(player);

        if (!success && "Thiên Mệnh Đan".equals(protection)) {
            CultivationData.setProtection(player, "");
            if (RANDOM.nextDouble() < 0.30D) {
                success = true;
                player.sendSystemMessage(Component.literal("✦ Thiên Mệnh Đan nghịch chuyển thiên kiếp, biến thất bại thành thành công! ✦").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            }
        }

        if (success) {
            advance(player, active.realm, active.stage);
            return;
        }

        boolean shouldDie = true;
        boolean shouldDrop = true;
        if ("Hộ Mệnh Đan".equals(protection)) {
            shouldDrop = false;
        } else if ("Niết Bàn Đan".equals(protection)) {
            shouldDie = false;
        } else if ("Đại Niết Bàn Đan".equals(protection)) {
            shouldDie = false;
            shouldDrop = false;
        }
        if (protection != null && !protection.isBlank()) {
            CultivationData.setProtection(player, "");
        }

        if (shouldDrop) dropOneStage(player);
        player.server.getPlayerList().broadcastSystemMessage(
                Component.literal("☠ ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                        .append(player.getDisplayName())
                        .append(Component.literal(" độ kiếp thất bại, thân tử đạo tiêu, tu vi giảm một bậc! ☠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)),
                false
        );
        if (shouldDie) {
            player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
        } else {
            player.setHealth(Math.max(2.0F, Math.min(player.getMaxHealth(), player.getHealth())));
            player.sendSystemMessage(Component.literal("✦ Đan bảo vệ đã cứu mạng ngươi trong thiên kiếp. ✦").withStyle(ChatFormatting.GOLD));
        }
    }

    private static void advance(ServerPlayer player, CultivationRealm realm, int stage) {
        CultivationRealm newRealm = realm;
        int newStage = stage + 1;
        if (newStage > realm.getMaxStage()) {
            newRealm = realm.next();
            newStage = 1;
        }
        if (newRealm == null) return;
        CultivationData.setRealmStage(player, newRealm, newStage);
        player.server.getPlayerList().broadcastSystemMessage(
                Component.literal("✦ ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        .append(player.getDisplayName())
                        .append(Component.literal(" đã vượt qua thiên kiếp, đột phá thành công lên ").withStyle(ChatFormatting.YELLOW))
                        .append(CultivationUtil.realmComponent(newRealm, newStage))
                        .append(Component.literal("! ✦").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)),
                false
        );
        if (newRealm.isAtLeast(CultivationRealm.BUOC_3)) {
            CultivationEvents.announceHighRealmBreakthrough(player, newRealm, newStage);
        }
        if (newRealm == CultivationRealm.TIEN_DE) {
            player.server.getPlayerList().broadcastSystemMessage(
                    Component.literal("☯ Thiên địa sụp đổ! " + player.getName().getString() + " đã đột phá Đạp Thiên đại viên mãn, thành tựu Tiên Đế! ☯")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    false
            );
        }
    }

    public static void dropOneStage(ServerPlayer player) {
        CultivationRealm realm = CultivationData.getRealm(player);
        int stage = CultivationData.getStage(player);
        if (realm == CultivationRealm.LUYEN_KHI && stage <= 1) return;
        if (stage > 1) {
            CultivationData.setRealmStage(player, realm, stage - 1);
            return;
        }
        CultivationRealm previous = realm.previous();
        if (previous != null) {
            CultivationData.setRealmStage(player, previous, previous.getMaxStage());
        }
    }

    private static void spawnVisualLightning(ServerPlayer player) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(player.serverLevel());
        if (lightning == null) return;
        lightning.moveTo(player.getX() + (RANDOM.nextDouble() - 0.5D) * 4.0D, player.getY(), player.getZ() + (RANDOM.nextDouble() - 0.5D) * 4.0D);
        lightning.setVisualOnly(true);
        player.serverLevel().addFreshEntity(lightning);
    }

    private record ActiveTribulation(CultivationRealm realm, int stage, double chance, long startTick, ServerBossEvent boss) {}
}
