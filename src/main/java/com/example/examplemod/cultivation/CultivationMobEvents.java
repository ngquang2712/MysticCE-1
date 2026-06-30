package com.example.examplemod.cultivation;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.expansion.VirtualHealthManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class CultivationMobEvents {
    private static final String TAG = "MysticCultivationMob";
    private static final String REALM = "MysticMobRealm";
    private static final String STAGE = "MysticMobStage";
    private static long nextSpawnTick = 20L * 60L * 5L;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long tick = event.getServer().overworld().getGameTime();
        if (tick < nextSpawnTick) return;
        nextSpawnTick = tick + (5L * 60L * 20L) + event.getServer().overworld().random.nextInt(5 * 60 * 20 + 1);

        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) return;
        ServerPlayer player = players.get(event.getServer().overworld().random.nextInt(players.size()));
        ServerLevel level = player.serverLevel();
        int count = 1 + level.random.nextInt(7);
        for (int i = 0; i < count; i++) spawnCultivationMob(level, player);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer victim && event.getSource().getEntity() instanceof Player attacker) {
            markCombat(victim);
            if (attacker instanceof ServerPlayer sp) markCombat(sp);
        }
        // PvP combat marker only applies when both sides are players.
        // Do not mark PvE combat here, otherwise /bay could be disabled by mobs.
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!isCultivationMob(entity)) return;
        CultivationRealm realm = getRealm(entity);
        if (!realm.isAtLeast(CultivationRealm.BUOC_3)) return;
        Random random = new Random();
        double chance = realm == CultivationRealm.BUOC_4 ? 0.55D : 0.35D;
        if (random.nextDouble() > chance) return;
        ItemStack stack = randomRareDrop(entity);
        if (!stack.isEmpty()) {
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
        }
    }

    private static void markCombat(ServerPlayer player) {
        long until = player.serverLevel().getGameTime() + 15L * 20L;
        CultivationData.root(player).putLong("CultivationCombatUntil", until);
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
        }
    }

    private static void spawnCultivationMob(ServerLevel level, ServerPlayer near) {
        BlockPos base = near.blockPosition();
        BlockPos pos = base.offset(level.random.nextInt(65) - 32, 0, level.random.nextInt(65) - 32);
        pos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        if (!level.getWorldBorder().isWithinBounds(pos)) return;

        EntityType<? extends Mob> type = switch (level.random.nextInt(5)) {
            case 0 -> EntityType.ZOMBIE;
            case 1 -> EntityType.SKELETON;
            case 2 -> EntityType.HUSK;
            case 3 -> EntityType.STRAY;
            default -> EntityType.DROWNED;
        };
        Mob mob = type.create(level);
        if (mob == null) return;
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360F, 0F);

        CultivationRealm realm = randomRealm(level);
        int stage = 1 + level.random.nextInt(realm.getMaxStage());
        if (realm == CultivationRealm.BUOC_4) stage = Math.min(stage, 5);
        applyMobCultivation(mob, realm, stage);
        level.addFreshEntity(mob);
    }

    private static CultivationRealm randomRealm(ServerLevel level) {
        int roll = level.random.nextInt(1000);
        if (roll < 5) return CultivationRealm.BUOC_4;
        if (roll < 25) return CultivationRealm.BUOC_3;
        if (roll < 70) return CultivationRealm.DUONG_THUC;
        if (roll < 130) return CultivationRealm.AM_HU;
        if (roll < 220) return CultivationRealm.VAN_DINH;
        if (roll < 330) return CultivationRealm.ANH_BIEN;
        if (roll < 460) return CultivationRealm.HOA_THAN;
        if (roll < 620) return CultivationRealm.NGUYEN_ANH;
        if (roll < 760) return CultivationRealm.KIM_DAN;
        if (roll < 890) return CultivationRealm.TRUC_CO;
        return CultivationRealm.LUYEN_KHI;
    }

    public static void applyMobCultivation(Mob mob, CultivationRealm realm, int stage) {
        CompoundTag tag = mob.getPersistentData();
        tag.putBoolean(TAG, true);
        tag.putString(REALM, realm.name());
        tag.putInt(STAGE, stage);
        double hp = 20.0D + CultivationUtil.totalBonusHearts(realm, stage) * 2.0D;
        double damage = 2.0D + CultivationUtil.totalBonusDamage(realm, stage);
        VirtualHealthManager.setMaxHealth(mob, hp);
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        mob.setHealth(mob.getMaxHealth());
        mob.setCustomName(Component.literal("[" + rarityName(realm) + "] ").withStyle(realm.getColor())
                .append(Component.literal(realm.formatStage(stage)).withStyle(realm.getColor(), ChatFormatting.BOLD))
                .append(Component.literal(" " + mob.getType().getDescription().getString()).withStyle(ChatFormatting.GRAY)));
        mob.setCustomNameVisible(true);
    }

    private static String rarityName(CultivationRealm realm) {
        if (realm.isAtLeast(CultivationRealm.BUOC_4)) return "THẦN THOẠI";
        if (realm.isAtLeast(CultivationRealm.BUOC_3)) return "HUYỀN THOẠI";
        if (realm.isAtLeast(CultivationRealm.AM_HU)) return "SỬ THI";
        if (realm.isAtLeast(CultivationRealm.NGUYEN_ANH)) return "HIẾM";
        return "THƯỜNG";
    }

    private static boolean isCultivationMob(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TAG);
    }

    private static CultivationRealm getRealm(LivingEntity entity) {
        return CultivationRealm.safeValueOf(entity.getPersistentData().getString(REALM));
    }

    private static ItemStack randomRareDrop(LivingEntity entity) {
        int roll = entity.level().random.nextInt(10);
        if (roll < 4) {
            return switch (entity.level().random.nextInt(6)) {
                case 0 -> new ItemStack(ExampleMod.HO_MENH_DAN.get());
                case 1 -> new ItemStack(ExampleMod.NIET_BAN_DAN.get());
                case 2 -> new ItemStack(ExampleMod.DAI_NIET_BAN_DAN.get());
                case 3 -> new ItemStack(ExampleMod.THIEN_MENH_DAN.get());
                case 4 -> new ItemStack(ExampleMod.AM_DUONG_DAN.get());
                default -> new ItemStack(ExampleMod.DAI_DAO_DAN.get());
            };
        }
        return switch (entity.level().random.nextInt(4)) {
            case 0 -> new ItemStack(ExampleMod.SUNG_KI_LAN.get());
            case 1 -> new ItemStack(ExampleMod.MANH_VO_THUONG_CO.get());
            case 2 -> new ItemStack(ExampleMod.LONG_PHUONG_NGU_SAC.get());
            default -> new ItemStack(ExampleMod.VAY_LONG_DE.get());
        };
    }
}
