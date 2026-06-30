package com.example.examplemod.expansion;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.boss.MysticBossEntity;
import com.example.examplemod.boss.MysticBossKind;
import com.example.examplemod.cultivation.CultivationMobEvents;
import com.example.examplemod.cultivation.CultivationRealm;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class BicanhEvents {
    private static final Random RANDOM = new Random();
    private static final long BICANH_COOLDOWN_TICKS = 20L * 60L * 20L;
    private static final long GUARDIAN_LIFETIME_TICKS = 5L * 60L * 20L;
    private static final int GUARDIAN_MIN_DISTANCE = 20;
    private static final int GUARDIAN_MAX_DISTANCE = 30;
    private static final int RAID_JOIN_RADIUS = 35;
    private static final int RAID_ACTIVE_RADIUS = 96;

    private static long nextGuardianTick = BICANH_COOLDOWN_TICKS;
    private static UUID guardianUuid = null;
    private static long guardianSpawnTick = 0L;
    private static BlockPos guardianPos = null;
    private static Raid raid = null;

    private static class Raid {
        BlockPos pos;
        long lockAt;
        long nextAction;
        int wave;
        boolean locked;
        Set<UUID> participants = new HashSet<>();

        Raid(BlockPos pos, long now) {
            this.pos = pos;
            this.lockAt = now + 60L * 20L;
            this.nextAction = lockAt + 10L * 20L;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return;
        long now = level.getGameTime();

        tickGuardian(level, now);
        if (raid != null) tickRaid(level, now);

        if (level.players().isEmpty()) return;
        if (raid == null && guardianUuid == null && now >= nextGuardianTick) {
            if (!spawnGuardian(level, now)) {
                nextGuardianTick = now + 60L * 20L;
            }
        }
    }

    private static boolean spawnGuardian(ServerLevel level, long now) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return false;

        // Thử nhiều lần để chắc chắn Kẻ gác cổng chỉ xuất hiện trên mặt đất quanh người chơi 20-30 block.
        for (int playerTry = 0; playerTry < Math.min(players.size(), 8); playerTry++) {
            ServerPlayer target = players.get(RANDOM.nextInt(players.size()));
            BlockPos pos = findSafeSurfacePosAround(level, target.blockPosition(), GUARDIAN_MIN_DISTANCE, GUARDIAN_MAX_DISTANCE, 64);
            if (pos == null) continue;

            Zombie guardian = EntityType.ZOMBIE.create(level);
            if (guardian == null) return false;
            guardian.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0, 0);
            guardian.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
            guardian.setPersistenceRequired();
            guardian.getPersistentData().putBoolean("MysticBicanhGuardian", true);
            guardian.getPersistentData().putBoolean("MysticBicanhMob", true);
            guardian.getPersistentData().putBoolean("MysticNoCultivationAutoConvert", true);
            VirtualHealthManager.setMaxHealth(guardian, 1000.0D);
            if (guardian.getAttribute(Attributes.ATTACK_DAMAGE) != null) guardian.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(35.0D);
            if (guardian.getAttribute(Attributes.ARMOR) != null) guardian.getAttribute(Attributes.ARMOR).setBaseValue(20.0D);
            if (guardian.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) guardian.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8D);
            guardian.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
            guardian.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
            guardian.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
            guardian.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
            guardian.setDropChance(EquipmentSlot.HEAD, 0.0F);
            guardian.setDropChance(EquipmentSlot.CHEST, 0.0F);
            guardian.setDropChance(EquipmentSlot.LEGS, 0.0F);
            guardian.setDropChance(EquipmentSlot.FEET, 0.0F);
            guardian.setCustomName(Component.literal("§5§lKẻ Gác Cổng Bí Cảnh §dThượng Cổ §c★★★ §7[1000 HP]"));
            guardian.setCustomNameVisible(true);
            level.addFreshEntity(guardian);

            guardianUuid = guardian.getUUID();
            guardianSpawnTick = now;
            guardianPos = pos;
            nextGuardianTick = now + BICANH_COOLDOWN_TICKS;

            level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§d§lKẻ Gác Cổng Bí Cảnh Thượng Cổ ★★★ đã xuất hiện trên mặt đất ở tọa độ X:" + pos.getX() + " Y:" + pos.getY() + " Z:" + pos.getZ()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            return true;
        }
        return false;
    }

    private static void tickGuardian(ServerLevel level, long now) {
        if (guardianUuid == null) return;
        Entity entity = level.getEntity(guardianUuid);
        if (entity == null || !entity.isAlive()) {
            guardianUuid = null;
            guardianSpawnTick = 0L;
            guardianPos = null;
            return;
        }
        if (now - guardianSpawnTick >= GUARDIAN_LIFETIME_TICKS) {
            BlockPos pos = entity.blockPosition();
            entity.discard();
            finishBicanh(level, pos, "§7Kẻ Gác Cổng Bí Cảnh tồn tại quá 5 phút không ai tiêu diệt, Bí Cảnh đã tự đóng. §e20 phút sau mới xuất hiện tiếp.");
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level)) return;
        if (dead.getPersistentData().getBoolean("MysticBicanhGuardian")) {
            BlockPos deathPos = dead.blockPosition();
            BlockPos pos = findSafeSurfacePos(level, deathPos.getX(), deathPos.getZ());
            if (pos == null && guardianPos != null) pos = guardianPos;
            if (pos == null) pos = deathPos;
            guardianUuid = null;
            guardianSpawnTick = 0L;
            guardianPos = null;
            raid = new Raid(pos, level.getGameTime());
            level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§6Cổng bí cảnh sắp mở ra trên mặt đất! Hãy tập hợp quanh tọa độ X:" + pos.getX() + " Y:" + pos.getY() + " Z:" + pos.getZ() + ". Sau 60 giây danh sách sẽ được chốt."), false);
        }
        if (raid != null && dead instanceof Player player) {
            raid.participants.remove(player.getUUID());
            player.sendSystemMessage(Component.literal("§cBạn đã chết và bị loại khỏi danh sách Bí Cảnh."));
        }
    }

    private static void tickRaid(ServerLevel level, long now) {
        if (!raid.locked && now >= raid.lockAt) {
            raid.locked = true;
            for (ServerPlayer p : level.players()) {
                if (p.distanceToSqr(raid.pos.getX() + 0.5D, raid.pos.getY(), raid.pos.getZ() + 0.5D) <= RAID_JOIN_RADIUS * RAID_JOIN_RADIUS) {
                    raid.participants.add(p.getUUID());
                }
            }
            if (raid.participants.isEmpty()) {
                finishBicanh(level, raid.pos, "§7Không có ai tham gia Bí Cảnh, Bí Cảnh đã tự đóng. §e20 phút sau mới xuất hiện tiếp.");
                return;
            }
            Component list = Component.literal("§eDanh sách tham gia Bí Cảnh: ");
            for (UUID id : raid.participants) {
                ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
                if (p != null) list = list.copy().append(Component.literal(p.getName().getString() + " "));
            }
            level.getServer().getPlayerList().broadcastSystemMessage(list, false);
            level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§cLàn sóng tấn công Bí Cảnh chuẩn bị bắt đầu trong 10 giây!"), false);
        }
        if (!raid.locked || now < raid.nextAction) return;
        if (!hasActiveParticipants(level)) {
            finishBicanh(level, raid.pos, "§7Không còn người chơi nào tham gia Bí Cảnh, Bí Cảnh đã tự đóng. §e20 phút sau mới xuất hiện tiếp.");
            return;
        }
        if (hasRaidMobs(level)) return;
        raid.wave++;
        if (raid.wave <= 4) {
            spawnWave(level, raid.wave);
            raid.nextAction = now + 5L * 20L;
        } else {
            reward(level);
            finishBicanh(level, raid.pos, "§6Bí Cảnh đã được chinh phục! §e20 phút sau mới xuất hiện tiếp.");
        }
    }

    private static boolean hasActiveParticipants(ServerLevel level) {
        if (raid == null) return false;
        Iterator<UUID> it = raid.participants.iterator();
        while (it.hasNext()) {
            UUID id = it.next();
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
            if (p == null || !p.isAlive() || p.level() != level || p.distanceToSqr(raid.pos.getX() + 0.5D, raid.pos.getY(), raid.pos.getZ() + 0.5D) > RAID_ACTIVE_RADIUS * RAID_ACTIVE_RADIUS) {
                it.remove();
            }
        }
        return !raid.participants.isEmpty();
    }

    private static boolean hasRaidMobs(ServerLevel level) {
        if (raid == null) return false;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(raid.pos).inflate(RAID_ACTIVE_RADIUS), e -> e.isAlive() && e.getPersistentData().getBoolean("MysticBicanhMob"))) {
            return true;
        }
        return false;
    }

    private static void spawnWave(ServerLevel level, int wave) {
        int count = switch (wave) { case 1 -> 4 + RANDOM.nextInt(2); case 2 -> 7 + RANDOM.nextInt(2); case 3 -> 10; default -> 7 + RANDOM.nextInt(2); };
        if (wave == 4) spawnFinalBoss(level);
        for (int i = 0; i < count; i++) spawnExistingCultivationMob(level, wave);
        level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§5Bí Cảnh - Đợt " + wave + " đã bắt đầu!"), false);
    }

    private static void spawnExistingCultivationMob(ServerLevel level, int wave) {
        if (raid == null) return;
        EntityType<? extends Mob> type = switch (level.random.nextInt(5)) {
            case 0 -> EntityType.ZOMBIE;
            case 1 -> EntityType.SKELETON;
            case 2 -> EntityType.HUSK;
            case 3 -> EntityType.STRAY;
            default -> EntityType.DROWNED;
        };
        Mob mob = type.create(level);
        if (mob == null) return;
        BlockPos pos = findSafeSurfacePosAround(level, raid.pos, 2, 8, 24);
        if (pos == null) pos = raid.pos;
        mob.moveTo(pos.getX()+0.5D, pos.getY(), pos.getZ()+0.5D, 0,0);
        CultivationRealm realm = switch (wave) {
            case 1 -> CultivationRealm.KIM_DAN;
            case 2 -> CultivationRealm.NGUYEN_ANH;
            case 3 -> CultivationRealm.HOA_THAN;
            default -> CultivationRealm.ANH_BIEN;
        };
        int stage = 1 + level.random.nextInt(Math.max(1, realm.getMaxStage()));
        CultivationMobEvents.applyMobCultivation(mob, realm, stage);
        mob.getPersistentData().putBoolean("MysticBicanhMob", true);
        level.addFreshEntity(mob);
    }

    private static void spawnFinalBoss(ServerLevel level) {
        if (raid == null) return;
        MysticBossKind[] kinds = {
                MysticBossKind.HO_PHAP_THUONG_CO,
                MysticBossKind.THIEN_DAO_HOA_THAN,
                MysticBossKind.CUU_TINH_CO_THAN,
                MysticBossKind.TIEN_DE_THANH_LAM
        };
        MysticBossKind kind = kinds[RANDOM.nextInt(kinds.length)];
        MysticBossEntity boss = ExampleMod.MYSTIC_BOSS.get().create(level);
        if (boss == null) return;
        BlockPos pos = findSafeSurfacePosAround(level, raid.pos, 1, 5, 16);
        if (pos == null) pos = raid.pos;
        boss.setBossKind(kind);
        boss.moveTo(pos.getX()+0.5D, pos.getY(), pos.getZ()+0.5D, 0,0);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        boss.getPersistentData().putBoolean("MysticBicanhMob", true);
        level.addFreshEntity(boss);
        boss.setupBossStats();
        boss.setCustomName(Component.literal("§4Boss Bí Cảnh - " + kind.getDisplayName()).withStyle(kind.getColor(), ChatFormatting.BOLD));
        boss.setCustomNameVisible(true);
    }

    private static BlockPos findSafeSurfacePosAround(ServerLevel level, BlockPos center, int minDist, int maxDist, int attempts) {
        for (int i = 0; i < attempts; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
            int dist = minDist + RANDOM.nextInt(Math.max(1, maxDist - minDist + 1));
            int x = center.getX() + (int)Math.round(Math.cos(angle) * dist);
            int z = center.getZ() + (int)Math.round(Math.sin(angle) * dist);
            BlockPos safe = findSafeSurfacePos(level, x, z);
            if (safe != null) return safe;
        }
        return null;
    }

    private static BlockPos findSafeSurfacePos(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinBuildHeight() + 1 || y >= level.getMaxBuildHeight() - 2) return null;
        BlockPos feet = new BlockPos(x, y, z);
        if (!level.hasChunkAt(feet)) return null;
        BlockPos head = feet.above();
        BlockPos ground = feet.below();
        BlockState feetState = level.getBlockState(feet);
        BlockState headState = level.getBlockState(head);
        BlockState groundState = level.getBlockState(ground);
        if (!feetState.isAir() || !headState.isAir()) return null;
        if (!feetState.getFluidState().isEmpty() || !headState.getFluidState().isEmpty() || !groundState.getFluidState().isEmpty()) return null;
        if (groundState.getCollisionShape(level, ground).isEmpty()) return null;
        return feet;
    }

    private static void reward(ServerLevel level) {
        if (raid == null) return;
        for (UUID id : raid.participants) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
            if (p == null || !p.isAlive()) continue;
            give(p, new ItemStack(ModExpansion.YEU_DAN.get(), 2 + RANDOM.nextInt(3)));
            give(p, new ItemStack(ModExpansion.LINH_THAO.get(), 3 + RANDOM.nextInt(4)));
            if (RANDOM.nextBoolean()) give(p, new ItemStack(ModExpansion.MANH_CUONG_HOA_SAC_BEN.get()));
            if (RANDOM.nextInt(100) < 35) give(p, new ItemStack(ModExpansion.THIEN_LINH_THAO.get()));
            if (RANDOM.nextInt(100) < 25) give(p, new ItemStack(ModExpansion.MA_HACH.get()));
            if (RANDOM.nextInt(100) < 15) give(p, new ItemStack(ModExpansion.THAN_HUYET.get()));
            p.sendSystemMessage(Component.literal("§aHoàn thành Bí Cảnh! Phần thưởng đã được thêm vào túi."));
        }
    }

    private static void finishBicanh(ServerLevel level, BlockPos center, String message) {
        cleanupBicanhMobs(level, center);
        raid = null;
        guardianUuid = null;
        guardianSpawnTick = 0L;
        guardianPos = null;
        nextGuardianTick = level.getGameTime() + BICANH_COOLDOWN_TICKS;
        if (message != null && !message.isEmpty()) {
            level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        }
    }

    private static void cleanupBicanhMobs(ServerLevel level, BlockPos center) {
        if (center == null) center = guardianPos;
        if (center == null) return;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(RAID_ACTIVE_RADIUS), e -> e.isAlive() && e.getPersistentData().getBoolean("MysticBicanhMob"))) {
            e.discard();
        }
    }

    private static void give(ServerPlayer p, ItemStack s) {
        if (!p.getInventory().add(s)) p.drop(s, false);
    }
}
