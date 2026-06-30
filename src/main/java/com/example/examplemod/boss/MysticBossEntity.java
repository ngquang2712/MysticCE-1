package com.example.examplemod.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.example.examplemod.expansion.VirtualHealthManager;

import java.util.List;
import java.util.Random;

public class MysticBossEntity extends Zombie {
    private static final EntityDataAccessor<String> BOSS_KIND = SynchedEntityData.defineId(MysticBossEntity.class, EntityDataSerializers.STRING);
    private static final Random RANDOM = new Random();

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Mystic Boss"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    );
    private boolean dropsRolled = false;

    public MysticBossEntity(EntityType<? extends MysticBossEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.ATTACK_DAMAGE, 70.0D)
                .add(Attributes.ARMOR, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BOSS_KIND, MysticBossKind.HO_PHAP_THUONG_CO.getId());
    }

    public MysticBossKind getBossKind() {
        return MysticBossKind.byId(this.entityData.get(BOSS_KIND));
    }

    public void setBossKind(MysticBossKind kind) {
        this.entityData.set(BOSS_KIND, kind.getId());
        setupBossStats();
    }

    public void setupBossStats() {
        MysticBossKind kind = getBossKind();
        VirtualHealthManager.setMaxHealth(this, kind.getHealth());
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(kind.getAttack());
        }
        if (this.getAttribute(Attributes.ARMOR) != null) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(kind.getArmor());
        }
        if (this.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.85D);
        }
        this.setCustomName(Component.literal(kind.getDisplayName()).withStyle(kind.getColor(), ChatFormatting.BOLD));
        this.setCustomNameVisible(true);
        equipArmor(kind);
        updateBossBarName();
    }

    private void equipArmor(MysticBossKind kind) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(kind.getHelmet()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(kind.getChestplate()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(kind.getLeggings()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(kind.getBoots()));
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
        this.setDropChance(EquipmentSlot.LEGS, 0.0F);
        this.setDropChance(EquipmentSlot.FEET, 0.0F);
    }

    private void updateBossBarName() {
        MysticBossKind kind = getBossKind();
        this.bossEvent.setName(VirtualHealthManager.formatHealthName(Component.literal(kind.getDisplayName()).withStyle(kind.getColor(), ChatFormatting.BOLD), this));
        this.bossEvent.setColor(switch (kind) {
            case HO_PHAP_THUONG_CO -> BossEvent.BossBarColor.WHITE;
            case THIEN_DAO_HOA_THAN -> BossEvent.BossBarColor.YELLOW;
            case CUU_TINH_CO_THAN -> BossEvent.BossBarColor.BLUE;
            case TIEN_DE_THANH_LAM -> BossEvent.BossBarColor.PURPLE;
        });
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        MysticBossKind kind = getBossKind();
        double totalMax = VirtualHealthManager.getTotalMaxHealth(this);
        double totalNow = VirtualHealthManager.getCurrentTotalHealth(this);
        this.bossEvent.setProgress(totalMax <= 0.0D ? 0.0F : (float)Math.max(0.0D, Math.min(1.0D, totalNow / totalMax)));
        if (this.tickCount % 20 == 0) updateBossBarName();
        if (this.tickCount % 20 == 0) {
            this.level().addParticle(ParticleTypes.ENCHANT, this.getX(), this.getY() + 2.0D, this.getZ(), 0.0D, 0.05D, 0.0D);
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        LivingEntity target = this.getTarget();
        if (!(target instanceof ServerPlayer player) || !target.isAlive()) {
            Player nearest = serverLevel.getNearestPlayer(this, 48.0D);
            if (nearest instanceof ServerPlayer nearestPlayer) {
                this.setTarget(nearestPlayer);
                target = nearestPlayer;
            }
        }
        if (!(target instanceof ServerPlayer serverPlayer)) return;

        if (kind == MysticBossKind.CUU_TINH_CO_THAN) {
            if (this.tickCount % (25 * 20) == 0) summonMinions(serverLevel, 4, false);
            if (this.tickCount % (18 * 20) == 0) heavenlyTribulation(serverLevel, serverPlayer, 3, 10.0F, 0.06F, 60.0F);
        } else if (kind == MysticBossKind.TIEN_DE_THANH_LAM) {
            if (this.tickCount % (22 * 20) == 0) summonMinions(serverLevel, 5, true);
            if (this.tickCount % (15 * 20) == 0) heavenlyTribulation(serverLevel, serverPlayer, 5, 14.0F, 0.08F, 80.0F);
            if (VirtualHealthManager.getCurrentTotalHealth(this) < VirtualHealthManager.getTotalMaxHealth(this) * 0.15D && this.tickCount % 80 == 0) {
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 120, 2));
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 120, 1));
            }
        } else if (kind == MysticBossKind.THIEN_DAO_HOA_THAN && this.tickCount % (22 * 20) == 0) {
            heavenlyTribulation(serverLevel, serverPlayer, 2, 8.0F, 0.04F, 45.0F);
        }
    }

    private void summonMinions(ServerLevel level, int amount, boolean emperor) {
        int nearbyMinions = level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(48.0D), mob -> mob.getPersistentData().getBoolean("MysticBossMinion")).size();
        int limit = emperor ? 16 : 12;
        if (nearbyMinions >= limit) return;
        for (int i = 0; i < amount && nearbyMinions + i < limit; i++) {
            Mob minion = createMinion(level, emperor);
            if (minion == null) continue;
            double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
            double radius = 3.0D + RANDOM.nextDouble() * 4.0D;
            minion.moveTo(this.getX() + Math.cos(angle) * radius, this.getY(), this.getZ() + Math.sin(angle) * radius, RANDOM.nextFloat() * 360.0F, 0.0F);
            minion.finalizeSpawn(level, level.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.EVENT, null, null);
            minion.getPersistentData().putBoolean("MysticBossMinion", true);
            if (this.getTarget() != null) minion.setTarget(this.getTarget());
            level.addFreshEntity(minion);
        }
        level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 2.0D, 1.0D, 2.0D, 0.05D);
    }

    private Mob createMinion(ServerLevel level, boolean emperor) {
        Mob minion;
        if (emperor) {
            minion = RANDOM.nextBoolean() ? EntityType.WITHER_SKELETON.create(level) : EntityType.VINDICATOR.create(level);
        } else {
            minion = RANDOM.nextBoolean() ? EntityType.ZOMBIE.create(level) : EntityType.SKELETON.create(level);
        }
        if (minion == null) return null;
        if (minion.getAttribute(Attributes.MAX_HEALTH) != null) {
            minion.getAttribute(Attributes.MAX_HEALTH).setBaseValue(emperor ? 80.0D : 50.0D);
            minion.setHealth(minion.getMaxHealth());
        }
        if (minion.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            minion.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(emperor ? 18.0D : 12.0D);
        }
        return minion;
    }

    private void heavenlyTribulation(ServerLevel level, ServerPlayer target, int strikes, float flatDamage, float percent, float cap) {
        for (int i = 0; i < strikes; i++) {
            double ox = (RANDOM.nextDouble() - 0.5D) * 5.0D;
            double oz = (RANDOM.nextDouble() - 0.5D) * 5.0D;
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning != null) {
                lightning.moveTo(target.getX() + ox, target.getY(), target.getZ() + oz);
                lightning.setVisualOnly(true);
                level.addFreshEntity(lightning);
            }
        }
        float damage = flatDamage + target.getMaxHealth() * percent;
        if (cap > 0.0F) damage = Math.min(damage, cap);
        target.hurt(this.damageSources().magic(), damage);
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 1.2D, 1.0D, 1.2D, 0.1D);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && !dropsRolled) {
            dropsRolled = true;
            List<Item> drops = MysticBossDrops.rollPills(getBossKind());
            for (Item item : drops) {
                this.spawnAtLocation(new ItemStack(item));
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(96.0D), mob -> mob.getPersistentData().getBoolean("MysticBossMinion"))
                        .forEach(Mob::discard);
                serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("✦ ").withStyle(getBossKind().getColor(), ChatFormatting.BOLD)
                                .append(Component.literal(getBossKind().getDisplayName()).withStyle(getBossKind().getColor(), ChatFormatting.BOLD))
                                .append(Component.literal(" đã bị tiêu diệt, đan dược tản lạc khắp chiến trường! ✦").withStyle(ChatFormatting.GOLD)),
                        false
                );
            }
        }
        this.bossEvent.removeAllPlayers();
        super.die(source);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("MysticBossKind", getBossKind().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MysticBossKind")) {
            setBossKind(MysticBossKind.byId(tag.getString("MysticBossKind")));
        } else {
            setupBossStats();
        }
    }
}
