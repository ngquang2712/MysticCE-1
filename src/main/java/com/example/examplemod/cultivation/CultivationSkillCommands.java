package com.example.examplemod.cultivation;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.boss.MysticBossEntity;
import com.example.examplemod.expansion.VirtualHealthManager;
import com.example.examplemod.mystic.WeaponStoneItem;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class CultivationSkillCommands {
    private static final int TICKS_PER_SECOND = 20;
    private static final int REQUIRED_KILLS = 5000;

    private static final String CD_DAP_GIO = "DapGioCooldown";
    private static final String CD_TICH_DIET_CHI = "TichDietChiCooldown";
    private static final String CD_TANG_CUONG = "TangCuongCooldown";
    private static final String CD_LOI_TIEN_KIEP = "LoiTienKiepCooldown";
    private static final String CD_NGUYEN_LUC_LOI_DINH = "NguyenLucLoiDinhCooldown";
    private static final String CD_TRAM_LA_QUYET = "TramLaQuyetCooldown";
    private static final String CD_KHAI_THIEN = "KhaiThienCommandCooldown";
    private static final String CD_XA_KICH = "XaKichCommandCooldown";
    private static final String NGUYEN_LUC_UNTIL = "NguyenLucLoiDinhUntil";

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("dapgio")
                        .executes(context -> dapGio(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("tichdietchi")
                        .executes(context -> tichDietChi(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("tangcuong")
                        .executes(context -> tangCuong(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("loitienkiep")
                        .executes(context -> loiTienKiep(context.getSource().getPlayerOrException()))
        );
        event.getDispatcher().register(
                Commands.literal("loikiep")
                        .executes(context -> loiTienKiep(context.getSource().getPlayerOrException()))
        );
        event.getDispatcher().register(
                Commands.literal("loithienkiep")
                        .executes(context -> loiTienKiep(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("nguyenlucloidinh")
                        .executes(context -> nguyenLucLoiDinh(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("tramlaquyet")
                        .executes(context -> tramLaQuyet(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("khaithien")
                        .executes(context -> khaiThien(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("xakich")
                        .executes(context -> xaKich(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("tangkill")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> tangKill(
                                        context.getSource().getPlayerOrException(),
                                        context.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(context, "amount"))))
                        .then(Commands.argument("user", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> tangKill(
                                                context.getSource().getPlayerOrException(),
                                                EntityArgument.getPlayer(context, "user"),
                                                IntegerArgumentType.getInteger(context, "amount")))))
        );
    }

    private static int dapGio(ServerPlayer player) {
        if (!requireRealm(player, CultivationRealm.TRUC_CO, "Đạp Gió")) return 0;
        if (!checkCooldown(player, CD_DAP_GIO, "Đạp Gió")) return 0;
        setCooldown(player, CD_DAP_GIO, 30);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 15 * TICKS_PER_SECOND, 2, true, false, true));
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.25D, player.getZ(), 45, 0.7D, 0.12D, 0.7D, 0.12D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.55F, 1.7F);
        player.sendSystemMessage(Component.literal("§bĐạp Gió! §7Tốc độ tăng mạnh trong 15 giây."));
        return 1;
    }

    private static int tangCuong(ServerPlayer player) {
        if (!requireRealm(player, CultivationRealm.KIM_DAN, "Tăng Cường")) return 0;
        if (!checkCooldown(player, CD_TANG_CUONG, "Tăng Cường")) return 0;
        setCooldown(player, CD_TANG_CUONG, 60);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * TICKS_PER_SECOND, 1, true, false, true));
        player.serverLevel().sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 55, 0.7D, 0.9D, 0.7D, 0.18D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.25F);
        player.sendSystemMessage(Component.literal("§6Tăng Cường! §7Nhận §cSức Mạnh II §7trong 20 giây."));
        return 1;
    }

    private static int tichDietChi(ServerPlayer player) {
        if (!requireRealm(player, CultivationRealm.KIM_DAN, "Tịch Diệt Chỉ")) return 0;
        if (!checkCooldown(player, CD_TICH_DIET_CHI, "Tịch Diệt Chỉ")) return 0;

        LivingEntity target = findTargetInLine(player, 32.0D, 0.965D);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cKhông có mục tiêu trước mặt trong 32 block."));
            return 0;
        }

        float health = player.getHealth();
        float cost = Math.max(1.0F, health * 0.30F);
        if (health - cost < 1.0F) cost = health - 1.0F;
        if (cost <= 0.0F) {
            player.sendSystemMessage(Component.literal("§cMáu quá thấp, không thể dùng Tịch Diệt Chỉ."));
            return 0;
        }

        setCooldown(player, CD_TICH_DIET_CHI, 60);
        player.setHealth(Math.max(1.0F, health - cost));
        float damage = Math.max(10.0F, cost * 2.0F + getAttackDamage(player));
        drawBeam(player.serverLevel(), player.getEyePosition(), target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), BeamType.FIRE);
        applySkillDamage(player, target, damage, 0.0F);
        target.setSecondsOnFire(4);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.sendSystemMessage(Component.literal("§4Tịch Diệt Chỉ! §7Tiêu hao §c" + Math.round(cost) + " HP §7gây §c" + Math.round(damage) + " damage§7."));
        return 1;
    }

    private static int loiTienKiep(ServerPlayer player) {
        if (!requireRealm(player, CultivationRealm.NGUYEN_ANH, "Lôi Tiên Kiếp")) return 0;
        if (!checkCooldown(player, CD_LOI_TIEN_KIEP, "Lôi Tiên Kiếp")) return 0;
        setCooldown(player, CD_LOI_TIEN_KIEP, 120);

        ServerLevel level = player.serverLevel();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(15.0D), entity -> isValidTarget(player, entity));
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        int hit = 0;
        float damage = Math.max(12.0F, getAttackDamage(player) + 12.0F);
        for (LivingEntity target : targets) {
            if (hit >= 8) break;
            spawnVisualLightning(level, target.getX(), target.getY(), target.getZ());
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 0.5D, 0.9D, 0.5D, 0.25D);
            applySkillDamage(player, target, damage, 1000.0F);
            hit++;
        }
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.server.getPlayerList().broadcastSystemMessage(Component.literal("§b§lLôi Tiên Kiếp! §f" + player.getName().getString() + " §7gọi thiên lôi đánh §f" + hit + " §7mục tiêu."), false);
        return hit > 0 ? 1 : 0;
    }

    private static int nguyenLucLoiDinh(ServerPlayer player) {
        if (!requireRealm(player, CultivationRealm.NGUYEN_ANH, "Nguyên Lực Lôi Đình")) return 0;
        if (!checkCooldown(player, CD_NGUYEN_LUC_LOI_DINH, "Nguyên Lực Lôi Đình")) return 0;
        setCooldown(player, CD_NGUYEN_LUC_LOI_DINH, 60);
        CultivationData.root(player).putLong(NGUYEN_LUC_UNTIL, player.serverLevel().getGameTime() + 10L * TICKS_PER_SECOND);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10 * TICKS_PER_SECOND, 1, true, false, true));
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 0.8D, 1.0D, 0.8D, 0.35D);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.5F);
        player.sendSystemMessage(Component.literal("§bNguyên Lực Lôi Đình! §7Trong 10 giây, sát thương nhận vào chuyển thành hồi máu."));
        return 1;
    }

    private static int tramLaQuyet(ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof SwordItem)) {
            player.sendSystemMessage(Component.literal("§cTrảm La Quyết cần cầm kiếm có Ngọc Theo Dõi Kill."));
            return 0;
        }
        if (!hasEnoughKills(player, weapon)) return 0;
        if (!checkCooldown(player, CD_TRAM_LA_QUYET, "Trảm La Quyết")) return 0;

        LivingEntity target = findTargetInLine(player, 34.0D, 0.94D);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cKhông có mục tiêu trước mặt trong 34 block."));
            return 0;
        }

        setCooldown(player, CD_TRAM_LA_QUYET, 120);
        damageWeapon(player, weapon, 0.20F);
        float damage = Math.max(16.0F, getAttackDamage(player) * 2.0F);
        drawBeam(player.serverLevel(), player.getEyePosition(), target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), BeamType.SWORD_QI);
        applySkillDamage(player, target, damage, 1200.0F);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.65F);
        player.sendSystemMessage(Component.literal("§aTrảm La Quyết! §7Kiếm khí chém mục tiêu gây §c" + Math.round(damage) + " damage§7."));
        return 1;
    }

    private static int khaiThien(ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof AxeItem)) {
            player.sendSystemMessage(Component.literal("§cKhai Thiên cần cầm rìu có Ngọc Theo Dõi Kill."));
            return 0;
        }
        if (!hasEnoughKills(player, weapon)) return 0;
        if (!checkCooldown(player, CD_KHAI_THIEN, "Khai Thiên")) return 0;

        LivingEntity target = findTargetInLine(player, 28.0D, 0.94D);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cKhông có mục tiêu trước mặt trong 28 block."));
            return 0;
        }

        setCooldown(player, CD_KHAI_THIEN, 300);
        damageWeapon(player, weapon, 0.20F);
        float currentHp = (float) VirtualHealthManager.getCurrentTotalHealth(target);
        if (currentHp <= 0.0F) currentHp = target.getHealth();
        float damage = currentHp * 0.60F;
        float bossCap = target instanceof MysticBossEntity ? 1500.0F : 0.0F;
        drawPillar(player.serverLevel(), target);
        applySkillDamage(player, target, damage, bossCap);
        target.setSecondsOnFire(8);
        player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.7F);
        player.server.getPlayerList().broadcastSystemMessage(Component.literal("§c§lKhai Thiên! §f" + player.getName().getString() + " §7bổ ra trụ lửa xé trời."), false);
        return 1;
    }

    private static int xaKich(ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof BowItem) && !(weapon.getItem() instanceof CrossbowItem)) {
            player.sendSystemMessage(Component.literal("§cXạ Kích cần cầm cung/nỏ có Ngọc Theo Dõi Kill."));
            return 0;
        }
        if (!hasEnoughKills(player, weapon)) return 0;
        if (!checkCooldown(player, CD_XA_KICH, "Xạ Kích")) return 0;

        LivingEntity target = findTargetInLine(player, 42.0D, 0.965D);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cKhông có mục tiêu trước mặt trong 42 block."));
            return 0;
        }

        setCooldown(player, CD_XA_KICH, 120);
        damageWeapon(player, weapon, 0.20F);
        float maxHp = (float) VirtualHealthManager.getTotalMaxHealth(target);
        if (maxHp <= 0.0F) maxHp = target.getMaxHealth();
        float damage = Math.max(20.0F, getAttackDamage(player) * 2.5F + maxHp * 0.15F);
        float bossCap = target instanceof MysticBossEntity ? 1200.0F : 0.0F;
        drawBeam(player.serverLevel(), player.getEyePosition(), target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), BeamType.PURPLE_ARROW);
        applySkillDamage(player, target, damage, bossCap);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 0.45F);
        player.sendSystemMessage(Component.literal("§5Xạ Kích! §7Tử quang xuyên phá mục tiêu gây §c" + Math.round(Math.min(damage, bossCap > 0 ? bossCap : damage)) + " damage§7."));
        return 1;
    }

    private static int tangKill(ServerPlayer sender, ServerPlayer targetPlayer, int amount) {
        ItemStack weapon = targetPlayer.getMainHandItem();
        if (weapon.isEmpty()) {
            sender.sendSystemMessage(Component.literal("§cNgười chơi phải cầm vũ khí cần tăng kill trên tay chính."));
            return 0;
        }
        CompoundTag tag = weapon.getOrCreateTag();
        tag.putBoolean(WeaponStoneItem.KILL_TRACKER, true);
        int before = tag.getInt(WeaponStoneItem.MOB_KILLS) + tag.getInt(WeaponStoneItem.PLAYER_KILLS);
        tag.putInt(WeaponStoneItem.MOB_KILLS, Math.max(0, tag.getInt(WeaponStoneItem.MOB_KILLS) + amount));
        int after = tag.getInt(WeaponStoneItem.MOB_KILLS) + tag.getInt(WeaponStoneItem.PLAYER_KILLS);
        sender.sendSystemMessage(Component.literal("§aĐã tăng kill vũ khí của " + targetPlayer.getName().getString() + ": §f" + before + " §7→ §e" + after));
        if (sender != targetPlayer) {
            targetPlayer.sendSystemMessage(Component.literal("§aVũ khí đang cầm đã được tăng thêm §e" + amount + " §akill."));
        }
        return 1;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long until = CultivationData.root(player).getLong(NGUYEN_LUC_UNTIL);
        if (until <= player.serverLevel().getGameTime()) return;
        float amount = event.getAmount();
        if (amount <= 0.0F) return;
        event.setCanceled(true);
        player.heal(amount);
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 25, 0.55D, 0.8D, 0.55D, 0.3D);
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity living && living != player) {
            applySkillDamage(player, living, Math.min(amount * 0.5F, 30.0F), 100.0F);
        }
        player.displayClientMessage(Component.literal("§bNguyên Lực Lôi Đình hấp thu sát thương và hồi §a" + Math.round(amount) + " HP"), true);
    }

    private static boolean requireRealm(ServerPlayer player, CultivationRealm realm, String skillName) {
        if (!CultivationData.getRealm(player).isAtLeast(realm)) {
            player.sendSystemMessage(Component.literal("§cCần đạt " + realm.getDisplayName() + " để dùng " + skillName + "."));
            return false;
        }
        return true;
    }

    private static boolean checkCooldown(ServerPlayer player, String key, String name) {
        long now = player.serverLevel().getGameTime();
        long until = CultivationData.getCooldown(player, key);
        if (until > now) {
            long seconds = (until - now + 19L) / 20L;
            player.sendSystemMessage(Component.literal("§c" + name + " đang hồi chiêu. Còn " + seconds + " giây."));
            return false;
        }
        return true;
    }

    private static void setCooldown(ServerPlayer player, String key, long seconds) {
        CultivationData.setCooldown(player, key, player.serverLevel().getGameTime() + seconds * TICKS_PER_SECOND);
    }

    private static float getAttackDamage(ServerPlayer player) {
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        return attack == null ? 1.0F : (float) attack.getValue();
    }

    private static boolean hasEnoughKills(ServerPlayer player, ItemStack weapon) {
        if (!weapon.hasTag() || !weapon.getTag().getBoolean(WeaponStoneItem.KILL_TRACKER)) {
            player.sendSystemMessage(Component.literal("§cVũ khí cần có Ngọc Theo Dõi Kill."));
            return false;
        }
        int kills = weapon.getTag().getInt(WeaponStoneItem.MOB_KILLS) + weapon.getTag().getInt(WeaponStoneItem.PLAYER_KILLS);
        if (kills < REQUIRED_KILLS) {
            player.sendSystemMessage(Component.literal("§cCần ít nhất " + REQUIRED_KILLS + " kill trên vũ khí. Hiện có " + kills + "."));
            return false;
        }
        return true;
    }

    private static void damageWeapon(ServerPlayer player, ItemStack weapon, float percentMaxDurability) {
        if (weapon.isEmpty() || !weapon.isDamageableItem()) return;
        int damage = Math.max(1, Math.round(weapon.getMaxDamage() * percentMaxDurability));
        weapon.hurtAndBreak(damage, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
    }

    private static boolean isValidTarget(ServerPlayer player, LivingEntity entity) {
        if (entity == player || !entity.isAlive()) return false;
        if (entity instanceof ServerPlayer targetPlayer && (targetPlayer.isCreative() || targetPlayer.isSpectator())) return false;
        return true;
    }

    private static LivingEntity findTargetInLine(ServerPlayer player, double range, double coneCos) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(3.0D);
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class, box, entity -> {
                    if (!isValidTarget(player, entity)) return false;
                    Vec3 center = entity.getBoundingBox().getCenter();
                    Vec3 toTarget = center.subtract(eye);
                    double distance = toTarget.length();
                    if (distance <= 0.0D || distance > range) return false;
                    double dot = toTarget.normalize().dot(look);
                    return dot >= coneCos && player.hasLineOfSight(entity);
                }).stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                .orElse(null);
    }

    private static void applySkillDamage(ServerPlayer player, LivingEntity target, float damage, float cap) {
        if (cap > 0.0F) damage = Math.min(damage, cap);
        if (damage <= 0.0F) return;
        VirtualHealthManager.applyDirectDamage(target, player.damageSources().playerAttack(player), damage);
    }

    private static void spawnVisualLightning(ServerLevel level, double x, double y, double z) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning == null) return;
        lightning.moveTo(x, y, z);
        lightning.setVisualOnly(true);
        level.addFreshEntity(lightning);
    }

    private static void drawBeam(ServerLevel level, Vec3 from, Vec3 to, BeamType type) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length <= 0.01D) return;
        Vec3 step = delta.normalize().scale(0.45D);
        int count = Math.max(1, (int) (length / 0.45D));
        for (int i = 0; i <= count; i++) {
            Vec3 pos = from.add(step.scale(i));
            switch (type) {
                case FIRE -> {
                    level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 2, 0.04D, 0.04D, 0.04D, 0.01D);
                    if (i % 2 == 0) level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0.03D, 0.03D, 0.03D, 0.01D);
                }
                case SWORD_QI -> {
                    level.sendParticles(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 3, 0.08D, 0.08D, 0.08D, 0.02D);
                    if (i % 2 == 0) level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y, pos.z, 1, 0.05D, 0.05D, 0.05D, 0.01D);
                }
                case PURPLE_ARROW -> {
                    level.sendParticles(ParticleTypes.DRAGON_BREATH, pos.x, pos.y, pos.z, 2, 0.06D, 0.06D, 0.06D, 0.015D);
                    if (i % 2 == 0) level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0.05D, 0.05D, 0.05D, 0.01D);
                }
            }
        }
    }

    private static void drawPillar(ServerLevel level, LivingEntity target) {
        for (int y = 0; y < 8; y++) {
            level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + y * 0.55D, target.getZ(), 18, 0.7D, 0.15D, 0.7D, 0.08D);
            level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + y * 0.55D, target.getZ(), 4, 0.35D, 0.08D, 0.35D, 0.02D);
        }
    }

    private enum BeamType {
        FIRE,
        SWORD_QI,
        PURPLE_ARROW
    }
}
