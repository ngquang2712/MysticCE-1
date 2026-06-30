package com.example.examplemod.boss;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.events.EraPowerHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
class MysticBossModEvents {
    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ExampleMod.MYSTIC_BOSS.get(), MysticBossEntity.createAttributes().build());
    }
}

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class MysticBossEvents {
    private static final Random RANDOM = new Random();
    private static long lastCheckTick = 0L;
    private static long nextChaosAllowedTick = 0L;
    private static long nextEmperorAllowedTick = 0L;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() - lastCheckTick < 20 * 60) return;
        lastCheckTick = event.getServer().getTickCount();
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null || level.players().isEmpty()) return;
        if (hasLivingDisasterBoss(level)) return;

        long gameTime = level.getGameTime();
        if (EraPowerHandler.isGalaxyEraOrHigher()) {
            if (gameTime >= nextEmperorAllowedTick && gameTime % (45L * 60L * 20L) < 20L * 60L && RANDOM.nextInt(100) < 5) {
                spawnDisaster(level, MysticBossKind.TIEN_DE_THANH_LAM);
                nextEmperorAllowedTick = gameTime + 90L * 60L * 20L;
            }
        } else if (EraPowerHandler.isChaosEraOrHigher()) {
            if (gameTime >= nextChaosAllowedTick && gameTime % (30L * 60L * 20L) < 20L * 60L && RANDOM.nextInt(100) < 8) {
                spawnDisaster(level, MysticBossKind.CUU_TINH_CO_THAN);
                nextChaosAllowedTick = gameTime + 45L * 60L * 20L;
            }
        }
    }

    private static boolean hasLivingDisasterBoss(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            for (MysticBossEntity boss : level.getEntitiesOfClass(MysticBossEntity.class, player.getBoundingBox().inflate(512.0D), MysticBossEntity::isAlive)) {
                if (boss.getBossKind().isDisaster()) return true;
            }
        }
        return false;
    }

    private static void spawnDisaster(ServerLevel level, MysticBossKind kind) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;
        ServerPlayer target = players.get(RANDOM.nextInt(players.size()));
        BlockPos pos = findSpawnPos(level, target, kind == MysticBossKind.TIEN_DE_THANH_LAM ? 160 : 110);
        MysticBossEntity boss = ExampleMod.MYSTIC_BOSS.get().create(level);
        if (boss == null) return;
        boss.setBossKind(kind);
        boss.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, RANDOM.nextFloat() * 360.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(boss);
        boss.setupBossStats();
        level.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, 8, 2.0D, 1.0D, 2.0D, 0.1D);
        Component msg = kind == MysticBossKind.CUU_TINH_CO_THAN
                ? Component.literal("§b§lCửu tinh dị tượng xuất hiện trên bầu trời! §3Cửu Tinh Cổ Thần đã giáng lâm...")
                : Component.literal("§5§lĐế uy bao phủ vạn giới! §dTiên Đế Thanh Lâm đã giáng lâm, thiên địa rung chuyển!");
        level.getServer().getPlayerList().broadcastSystemMessage(msg, false);
    }

    private static BlockPos findSpawnPos(ServerLevel level, Player target, int baseDistance) {
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
            int distance = baseDistance + RANDOM.nextInt(80);
            int x = target.blockPosition().getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = target.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos spawn = level.getSharedSpawnPos();
            int dx = pos.getX() - spawn.getX();
            int dz = pos.getZ() - spawn.getZ();
            if (dx * dx + dz * dz > 80 * 80) {
                return pos;
            }
        }
        return target.blockPosition().offset(baseDistance, 0, 0);
    }
}
