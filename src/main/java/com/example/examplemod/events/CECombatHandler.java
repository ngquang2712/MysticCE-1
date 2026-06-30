package com.example.examplemod.events;

import com.example.examplemod.ce.CEType;
import com.example.examplemod.ce.CEUtil;
import com.example.examplemod.mystic.MysticShieldItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class CECombatHandler {

    private static final Map<UUID, Float> CO_DOC_HEAL_REDUCE = new HashMap<>();
    private static final Map<UUID, Boolean> HO_DAU_NEXT_HIT = new HashMap<>();
    private static final Map<UUID, Boolean> QUY_CHAN_NEXT_HIT = new HashMap<>();
    private static final Map<UUID, Boolean> PHA_KHONG_NEXT_HIT = new HashMap<>();

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> AN_KHANG_IMMUNE = new HashMap<>();
    private static final Map<UUID, Long> NHAT_NGUYET_CRIT_UNTIL = new HashMap<>();

    private static final Map<UUID, Integer> BONG_LANH_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> BONG_LANH_INTERVAL = new HashMap<>();
    private static final Map<UUID, Float> BONG_LANH_DAMAGE = new HashMap<>();

    private static final List<DelayedDamage> DELAYED_DAMAGES = new ArrayList<>();
    private static boolean PROCESSING_DELAYED_DAMAGE = false;

    private static final int TICKS_PER_SECOND = 20;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {

        if (event.getEntity() instanceof Player player) {
            handlePlayerDefence(event, player);
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (PROCESSING_DELAYED_DAMAGE) {
            return;
        }

        LivingEntity target = event.getEntity();
        ItemStack weapon = getUsedWeapon(player);

        float originalDamage = event.getAmount();
        float damage = originalDamage;
        StringBuilder message = new StringBuilder();

        damage = applyMaSoi(player, weapon, damage, message);
        damage = applyCoDoc(player, weapon, target, damage, message);
        damage = applyHoDauNextHit(player, target, damage, message);
        damage = applyQuyChanNextHit(player, damage, message);
        damage = applyPhaKhongNextHit(player, damage, message);
        damage = applyNhatNguyet(player, damage, message);
        damage = applyKhaiThien(player, weapon, target, damage, message);
        damage = applyBanTia(player, weapon, target, damage, message);
        damage = applyXaKich(player, weapon, target, damage, message);
        damage = applyHoangBao(player, weapon, target, damage, message);

        event.setAmount(damage);

        applyBongLanh(player, weapon, target, message);
        applyVanKiem(player, weapon, target, damage, message);

        if (message.length() > 0) {
            player.displayClientMessage(Component.literal(message.toString()), true);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        handleBongLanhTick(event.getEntity());

        if (!event.getEntity().level().isClientSide) {
            handleDelayedDamages(event.getEntity().level().getGameTime());
        }
    }

    private static void handlePlayerDefence(LivingHurtEvent event, Player player) {

        applyMysticShield(player, event);

        StringBuilder message = new StringBuilder();

        boolean phaKhong = applyPhaKhong(player, event, message);
        if (phaKhong) {
            if (message.length() > 0) player.displayClientMessage(Component.literal(message.toString()), true);
            return;
        }

        boolean thienMenh = applyThienMenh(player, event, message);
        if (thienMenh) {
            if (message.length() > 0) player.displayClientMessage(Component.literal(message.toString()), true);
            return;
        }

        boolean quyChan = applyQuyChan(player, event, message);
        if (quyChan) {
            if (message.length() > 0) player.displayClientMessage(Component.literal(message.toString()), true);
            return;
        }

        boolean anKhangActivated = applyAnKhang(player, event, message);

        if (!anKhangActivated) {
            applyVayRong(player, event, message);
        }

        applyNhatNguyetDefence(player, message);

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = player.getMainHandItem();

            applyHoDau(player, weapon, attacker, event);
            applyBuaYeu(player, attacker, message);
        }

        if (message.length() > 0) {
            player.displayClientMessage(Component.literal(message.toString()), true);
        }
    }

    private static void applyMysticShield(Player player, LivingHurtEvent event) {
        if (!player.isBlocking()) return;
        ItemStack used = player.getUseItem();
        if (!(used.getItem() instanceof MysticShieldItem shield)) return;
        float reduce = shield.getBlockReducePercent() / 100.0F;
        event.setAmount(event.getAmount() * (1.0F - reduce));
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        UUID uuid = entity.getUUID();

        if (!CO_DOC_HEAL_REDUCE.containsKey(uuid)) return;

        float reduceAmount = CO_DOC_HEAL_REDUCE.get(uuid);
        float healAmount = event.getAmount();

        if (reduceAmount >= healAmount) {
            event.setAmount(0);
            CO_DOC_HEAL_REDUCE.put(uuid, reduceAmount - healAmount);
        } else {
            event.setAmount(healAmount - reduceAmount);
            CO_DOC_HEAL_REDUCE.remove(uuid);
        }

        if (CO_DOC_HEAL_REDUCE.getOrDefault(uuid, 0F) <= 0F) {
            CO_DOC_HEAL_REDUCE.remove(uuid);
        }
    }

    private static float applyMaSoi(Player player, ItemStack weapon, float damage, StringBuilder message) {
        int level = CEUtil.getCELevel(weapon, CEType.MA_SOI);
        if (level <= 0 || !player.level().isNight()) return damage;

        float bonus = switch (level) {
            case 1 -> 0.03F;
            case 2 -> 0.05F;
            case 3 -> 0.07F;
            case 4 -> 0.10F;
            default -> 0F;
        };

        appendMessage(message, "🌙 Ma Sói +" + (int) (bonus * 100) + "%");
        return damage * (1.0F + bonus);
    }

    private static float applyCoDoc(Player player, ItemStack weapon, LivingEntity target, float damage, StringBuilder message) {
        int level = CEUtil.getCELevel(weapon, CEType.CO_DOC);
        if (level <= 0) return damage;

        UUID targetId = target.getUUID();
        CO_DOC_HEAL_REDUCE.put(targetId, CO_DOC_HEAL_REDUCE.getOrDefault(targetId, 0F) + damage);
        appendMessage(message, "☠ Cô Độc -" + String.format("%.1f", damage) + " hồi phục");
        return damage;
    }

    private static void applyBongLanh(Player player, ItemStack weapon, LivingEntity target, StringBuilder message) {
        int level = CEUtil.getCELevel(weapon, CEType.BONG_LANH);
        if (level <= 0) return;

        String key = cooldownKey(player, CEType.BONG_LANH);
        if (isOnCooldown(player, key)) return;

        int duration = switch (level) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            case 4 -> 5;
            default -> 0;
        };
        if (duration <= 0) return;

        float trueDamagePerSecond = target.getMaxHealth() * 0.10F;
        UUID targetId = target.getUUID();

        BONG_LANH_TICKS.put(targetId, duration * TICKS_PER_SECOND);
        BONG_LANH_INTERVAL.put(targetId, TICKS_PER_SECOND);
        BONG_LANH_DAMAGE.put(targetId, trueDamagePerSecond);

        startCooldown(player, key, 10);
        spawnParticles(target, ParticleTypes.SNOWFLAKE, 25);
        playSound(target, SoundEvents.PLAYER_HURT_FREEZE, 1.0F, 1.0F);
        appendMessage(message, "❄ Bỏng Lạnh: sát thương chuẩn " + duration + "s");
    }

    private static float applyBanTia(Player player, ItemStack weapon, LivingEntity target, float damage, StringBuilder message) {
        int level = CEUtil.getCELevel(weapon, CEType.BAN_TIA);
        if (level <= 0) return damage;

        String key = cooldownKey(player, CEType.BAN_TIA);
        if (isOnCooldown(player, key)) return damage;

        if (target instanceof Player targetPlayer) {
            long gameTime = targetPlayer.level().getGameTime();
            if (AN_KHANG_IMMUNE.getOrDefault(targetPlayer.getUUID(), 0L) > gameTime) return damage;
            if (targetPlayer.isInvulnerable() || targetPlayer.isCreative() || targetPlayer.isSpectator()) return damage;
        }

        float armorBypassPercent = switch (level) {
            case 1 -> 0.10F;
            case 2 -> 0.20F;
            case 3 -> 0.30F;
            case 4 -> 0.40F;
            case 5 -> 0.50F;
            default -> 0F;
        };

        int cooldown = switch (level) {
            case 1 -> 60;
            case 2 -> 50;
            case 3 -> 40;
            case 4 -> 30;
            case 5 -> 20;
            default -> 60;
        };

        float armorValue = target.getArmorValue();
        float bonusDamage = armorValue * armorBypassPercent * 0.5F;

        startCooldown(player, key, cooldown);
        spawnParticles(target, ParticleTypes.CRIT, 25);
        playSound(target, SoundEvents.ARROW_HIT_PLAYER, 1.0F, 1.2F);
        appendMessage(message, "🎯 Bắn Tỉa xuyên " + (int) (armorBypassPercent * 100) + "% giáp");
        return damage + bonusDamage;
    }


    private static float applyXaKich(Player player, ItemStack weapon, LivingEntity target, float damage, StringBuilder message) {
        int level = CEUtil.getCELevel(weapon, CEType.XA_KICH);
        if (level <= 0) return damage;

        float percent = switch (level) {
            case 1 -> 0.03F;
            case 2 -> 0.05F;
            case 3 -> 0.07F;
            case 4 -> 0.09F;
            case 5 -> 0.15F;
            default -> 0F;
        };

        float bonusDamage = target.getMaxHealth() * percent;
        spawnParticles(target, ParticleTypes.CRIT, 35);
        playSound(target, SoundEvents.ARROW_HIT_PLAYER, 1.0F, 1.5F);
        appendMessage(message, "🏹 Xạ Kích +" + (int) (percent * 100) + "% máu tối đa");
        return damage + bonusDamage;
    }


    private static float applyHoangBao(Player player, ItemStack weapon, LivingEntity target, float damage, StringBuilder message) {
        int level = CEUtil.getCELevel(weapon, CEType.HOANG_BAO);
        if (level <= 0) return damage;
        MobEffectInstance resistance = target.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistance == null) return damage;

        float chance = switch (level) { case 1 -> 0.40F; case 2 -> 0.50F; case 3 -> 0.60F; default -> 0F; };
        if (player.getRandom().nextFloat() > chance) return damage;

        int amplifier = resistance.getAmplifier() + 1;
        float reduction = Math.min(0.80F, amplifier * 0.20F);
        if (reduction <= 0.0F || reduction >= 1.0F) return damage;

        float bypassedDamage = damage / (1.0F - reduction);
        spawnParticles(target, ParticleTypes.ENCHANT, 45);
        playSound(target, SoundEvents.BEACON_ACTIVATE, 1.0F, 1.35F);
        appendMessage(message, "♛ Hoàng Bào bỏ qua Đề Kháng");
        return bypassedDamage;
    }

    private static boolean applyAnKhang(Player player, LivingHurtEvent event, StringBuilder message) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        int level = CEUtil.getCELevel(leggings, CEType.AN_KHANG);
        if (level <= 0) return false;

        String key = cooldownKey(player, CEType.AN_KHANG);
        long gameTime = player.level().getGameTime();

        if (AN_KHANG_IMMUNE.getOrDefault(player.getUUID(), 0L) > gameTime) {
            event.setAmount(0F);
            return true;
        }
        if (isOnCooldown(player, key)) return false;

        float chance = switch (level) {
            case 1 -> 0.15F;
            case 2 -> 0.20F;
            case 3 -> 0.25F;
            case 4 -> 0.30F;
            case 5 -> 0.40F;
            default -> 0F;
        };
        int duration = switch (level) {
            case 1 -> 2;
            case 2 -> 3;
            case 3, 4 -> 4;
            case 5 -> 5;
            default -> 0;
        };
        int cooldown = switch (level) {
            case 1, 2, 3 -> 120;
            case 4 -> 90;
            case 5 -> 60;
            default -> 120;
        };

        if (player.getRandom().nextFloat() > chance) return false;

        event.setAmount(0F);
        AN_KHANG_IMMUNE.put(player.getUUID(), gameTime + duration * TICKS_PER_SECOND);
        startCooldown(player, key, cooldown);
        spawnParticles(player, ParticleTypes.TOTEM_OF_UNDYING, 40);
        playSound(player, SoundEvents.TOTEM_USE, 1.0F, 1.0F);
        appendMessage(message, "🛡 An Khang miễn nhiễm " + duration + "s");
        return true;
    }

    private static void applyBuaYeu(Player player, LivingEntity attacker, StringBuilder message) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        int level = CEUtil.getCELevel(helmet, CEType.BUA_YEU);
        if (level <= 0) return;

        String key = cooldownKey(player, CEType.BUA_YEU);
        if (isOnCooldown(player, key)) return;

        float chance = switch (level) { case 1 -> 0.15F; case 2 -> 0.20F; case 3 -> 0.30F; default -> 0F; };
        int duration = switch (level) { case 1 -> 2; case 2 -> 3; case 3 -> 4; default -> 0; };
        int cooldown = switch (level) { case 1 -> 120; case 2 -> 90; case 3 -> 60; default -> 120; };

        if (player.getRandom().nextFloat() > chance) return;

        attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration * TICKS_PER_SECOND, 0));
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration * TICKS_PER_SECOND, 0));
        attacker.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration * TICKS_PER_SECOND, 0));

        startCooldown(player, key, cooldown);
        spawnParticles(attacker, ParticleTypes.HEART, 15);
        playSound(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.2F);
        appendMessage(message, "💘 Bùa Yêu: Mù + Chậm + Hoa mắt");
    }

    private static void applyVayRong(Player player, LivingHurtEvent event, StringBuilder message) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = CEUtil.getCELevel(chestplate, CEType.VAY_RONG);
        if (level <= 0) return;

        String key = cooldownKey(player, CEType.VAY_RONG);
        if (isOnCooldown(player, key)) return;

        float healthAfterDamage = player.getHealth() - event.getAmount();
        if (healthAfterDamage > player.getMaxHealth() * 0.50F) return;

        float chance = switch (level) { case 1 -> 0.15F; case 2 -> 0.20F; case 3 -> 0.30F; default -> 0F; };
        if (player.getRandom().nextFloat() > chance) return;

        float absorptionPercent = switch (level) { case 1 -> 0.20F; case 2 -> 0.30F; case 3 -> 0.40F; default -> 0F; };
        float healPercent = absorptionPercent;
        int cooldown = switch (level) { case 1 -> 120; case 2 -> 90; case 3 -> 60; default -> 120; };

        float absorptionAmount = player.getMaxHealth() * absorptionPercent;
        float healAmount = player.getMaxHealth() * healPercent;

        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), absorptionAmount));
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
        removeBadEffects(player);
        startCooldown(player, key, cooldown);
        spawnParticles(player, ParticleTypes.DRAGON_BREATH, 50);
        playSound(player, SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 0.8F);
        appendMessage(message, "🐉 Vảy Rồng +" + (int) (absorptionPercent * 100) + "% máu vàng");
    }

    private static void applyHoDau(Player player, ItemStack weapon, LivingEntity attacker, LivingHurtEvent event) {
        int level = CEUtil.getCELevel(weapon, CEType.HO_DAU);
        if (level <= 0) return;

        float chance = switch (level) { case 1 -> 0.10F; case 2 -> 0.15F; case 3 -> 0.20F; case 4 -> 0.25F; default -> 0F; };
        if (player.getRandom().nextFloat() > chance) return;

        float reflectedDamage = event.getAmount();
        attacker.hurt(player.damageSources().generic(), reflectedDamage);
        HO_DAU_NEXT_HIT.put(player.getUUID(), true);
        removeBadEffects(player);

        player.displayClientMessage(Component.literal("🐯 Hổ Đấu phản " + String.format("%.1f", reflectedDamage) + " damage | Đòn sau +20% HP"), true);
    }

    private static float applyHoDauNextHit(Player player, LivingEntity target, float damage, StringBuilder message) {
        UUID playerId = player.getUUID();
        if (!HO_DAU_NEXT_HIT.getOrDefault(playerId, false)) return damage;
        HO_DAU_NEXT_HIT.remove(playerId);

        float bonusDamage = target.getMaxHealth() * 0.20F;
        appendMessage(message, "🐯 Hổ Đấu +" + String.format("%.1f", bonusDamage));
        return damage + bonusDamage;
    }

    private static boolean applyThienMenh(Player player, LivingHurtEvent event, StringBuilder message) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        int level = CEUtil.getCELevel(helmet, CEType.THIEN_MENH);
        if (level <= 0) return false;

        String key = cooldownKey(player, CEType.THIEN_MENH);
        if (isOnCooldown(player, key)) return false;

        float healthAfterDamage = player.getHealth() - event.getAmount();
        if (healthAfterDamage > 0.0F) return false;

        int duration = switch (level) { case 1 -> 4; case 2 -> 6; case 3 -> 8; default -> 4; };

        event.setAmount(0.0F);
        player.setHealth(Math.max(1.0F, player.getHealth()));
        player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), player.getMaxHealth()));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration * TICKS_PER_SECOND, 2));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration * TICKS_PER_SECOND, 2));
        startCooldown(player, key, 200);
        spawnParticles(player, ParticleTypes.TOTEM_OF_UNDYING, 80);
        playSound(player, SoundEvents.TOTEM_USE, 1.0F, 0.8F);
        appendMessage(message, "👑 Thiên Mệnh kích hoạt");
        return true;
    }

    private static boolean applyQuyChan(Player player, LivingHurtEvent event, StringBuilder message) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = CEUtil.getCELevel(chestplate, CEType.QUY_CHAN);
        if (level <= 0) return false;

        String key = cooldownKey(player, CEType.QUY_CHAN);
        if (isOnCooldown(player, key)) return false;

        float healthAfterDamage = player.getHealth() - event.getAmount();
        if (healthAfterDamage > player.getMaxHealth() * 0.20F) return false;

        event.setAmount(0.0F);
        player.setHealth(player.getMaxHealth());
        removeBadEffects(player);
        QUY_CHAN_NEXT_HIT.put(player.getUUID(), true);
        startCooldown(player, key, 120);
        spawnParticles(player, ParticleTypes.ENCHANT, 70);
        playSound(player, SoundEvents.BEACON_ACTIVATE, 1.0F, 1.4F);
        appendMessage(message, "🛡 Quy Chân hồi đầy máu");
        return true;
    }

    private static float applyQuyChanNextHit(Player player, float damage, StringBuilder message) {
        if (!QUY_CHAN_NEXT_HIT.getOrDefault(player.getUUID(), false)) return damage;
        QUY_CHAN_NEXT_HIT.remove(player.getUUID());
        appendMessage(message, "🛡 Quy Chân +50%");
        return damage * 1.50F;
    }

    private static void applyNhatNguyetDefence(Player player, StringBuilder message) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        int level = CEUtil.getCELevel(leggings, CEType.NHAT_NGUYET);
        if (level <= 0) return;

        String key = cooldownKey(player, CEType.NHAT_NGUYET);
        if (isOnCooldown(player, key)) return;
        if (player.getRandom().nextFloat() > 0.30F) return;

        int duration = switch (level) { case 1 -> 2; case 2 -> 3; case 3 -> 4; case 4 -> 5; case 5 -> 6; default -> 2; };
        NHAT_NGUYET_CRIT_UNTIL.put(player.getUUID(), player.level().getGameTime() + duration * TICKS_PER_SECOND);
        startCooldown(player, key, 120);
        spawnParticles(player, ParticleTypes.CRIT, 45);
        playSound(player, SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
        appendMessage(message, "☀🌙 Nhật Nguyệt chí mạng " + duration + "s");
    }

    private static float applyNhatNguyet(Player player, float damage, StringBuilder message) {
        long until = NHAT_NGUYET_CRIT_UNTIL.getOrDefault(player.getUUID(), 0L);
        if (until <= player.level().getGameTime()) return damage;
        appendMessage(message, "☀🌙 Chí mạng");
        return damage * 1.50F;
    }

    private static boolean applyPhaKhong(Player player, LivingHurtEvent event, StringBuilder message) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int level = CEUtil.getCELevel(boots, CEType.PHA_KHONG);
        if (level <= 0) return false;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return false;

        String key = cooldownKey(player, CEType.PHA_KHONG);
        if (isOnCooldown(player, key)) return false;

        event.setAmount(0.0F);
        teleportBehind(player, attacker);
        PHA_KHONG_NEXT_HIT.put(player.getUUID(), true);
        startCooldown(player, key, 120);
        spawnParticles(player, ParticleTypes.PORTAL, 80);
        playSound(player, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        appendMessage(message, "👢 Phá Không né đòn");
        return true;
    }

    private static float applyPhaKhongNextHit(Player player, float damage, StringBuilder message) {
        if (!PHA_KHONG_NEXT_HIT.getOrDefault(player.getUUID(), false)) return damage;
        PHA_KHONG_NEXT_HIT.remove(player.getUUID());
        appendMessage(message, "👢 Phá Không x3");
        return damage * 3.0F;
    }

    private static void teleportBehind(Player player, LivingEntity attacker) {
        double yaw = Math.toRadians(attacker.getYRot());
        double x = attacker.getX() + Math.sin(yaw) * 1.5D;
        double z = attacker.getZ() - Math.cos(yaw) * 1.5D;
        player.teleportTo(x, attacker.getY(), z);
    }

    private static void applyVanKiem(Player player, ItemStack weapon, LivingEntity target, float damage, StringBuilder message) {
        int level = CEUtil.getCELevel(weapon, CEType.VAN_KIEM);
        if (level <= 0) return;

        String key = cooldownKey(player, CEType.VAN_KIEM);
        if (isOnCooldown(player, key)) return;
        if (player.getRandom().nextFloat() > 0.30F) return;

        int hits = Math.min(level, 4);
        long now = player.level().getGameTime();
        UUID targetId = target.getUUID();
        UUID playerId = player.getUUID();

        for (int i = 1; i <= hits; i++) {
            DELAYED_DAMAGES.add(new DelayedDamage(now + i * 8L, playerId, targetId, damage));
        }

        startCooldown(player, key, 120);
        spawnParticles(target, ParticleTypes.SWEEP_ATTACK, 20);
        playSound(target, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.1F);
        appendMessage(message, "⚔ Vạn Kiếm: " + hits + " nhịp");
    }

    private static float applyKhaiThien(Player player, ItemStack weapon, LivingEntity target, float damage, StringBuilder message) {
        int level = CEUtil.getCELevel(weapon, CEType.KHAI_THIEN);
        if (level <= 0) return damage;

        float percent = switch (level) { case 1 -> 0.02F; case 2 -> 0.05F; case 3 -> 0.10F; default -> 0F; };
        float bonus = target.getHealth() * percent;
        appendMessage(message, "🪓 Khai Thiên +" + String.format("%.1f", bonus));
        return damage + bonus;
    }

    private static void handleBongLanhTick(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        if (!BONG_LANH_TICKS.containsKey(uuid)) return;

        int ticksLeft = BONG_LANH_TICKS.get(uuid);
        int interval = BONG_LANH_INTERVAL.getOrDefault(uuid, TICKS_PER_SECOND);
        float trueDamage = BONG_LANH_DAMAGE.getOrDefault(uuid, 0F);

        interval--;
        if (interval <= 0) {
            float newHealth = entity.getHealth() - trueDamage;
            if (newHealth <= 0F) {
                entity.hurt(entity.damageSources().magic(), 9999F);
            } else {
                entity.setHealth(newHealth);
            }
            spawnParticles(entity, ParticleTypes.SNOWFLAKE, 12);
            playSound(entity, SoundEvents.PLAYER_HURT_FREEZE, 0.6F, 1.2F);
            interval = TICKS_PER_SECOND;
        }

        ticksLeft--;
        if (ticksLeft <= 0 || entity.isDeadOrDying()) {
            BONG_LANH_TICKS.remove(uuid);
            BONG_LANH_INTERVAL.remove(uuid);
            BONG_LANH_DAMAGE.remove(uuid);
            return;
        }

        BONG_LANH_TICKS.put(uuid, ticksLeft);
        BONG_LANH_INTERVAL.put(uuid, interval);
    }

    private static void handleDelayedDamages(long gameTime) {
        if (DELAYED_DAMAGES.isEmpty()) return;

        Iterator<DelayedDamage> iterator = DELAYED_DAMAGES.iterator();
        while (iterator.hasNext()) {
            DelayedDamage data = iterator.next();
            if (data.tick > gameTime) continue;
            iterator.remove();

            ServerLevel level = data.levelRef();
            if (level == null) continue;
            Player player = level.getPlayerByUUID(data.playerId);
            net.minecraft.world.entity.Entity entity = level.getEntity(data.targetId);
            if (player == null || !(entity instanceof LivingEntity target) || target.isDeadOrDying()) continue;

            PROCESSING_DELAYED_DAMAGE = true;
            target.hurt(player.damageSources().playerAttack(player), data.damage);
            PROCESSING_DELAYED_DAMAGE = false;
            spawnParticles(target, ParticleTypes.SWEEP_ATTACK, 8);
        }
    }

    private static ItemStack getUsedWeapon(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (CEUtil.getCELevel(mainHand, CEType.BAN_TIA) > 0 || CEUtil.getCELevel(mainHand, CEType.XA_KICH) > 0 || CEUtil.getCELevel(mainHand, CEType.HOANG_BAO) > 0) return mainHand;
        if (CEUtil.getCELevel(offHand, CEType.BAN_TIA) > 0 || CEUtil.getCELevel(offHand, CEType.XA_KICH) > 0 || CEUtil.getCELevel(offHand, CEType.HOANG_BAO) > 0) return offHand;
        return mainHand;
    }

    private static String cooldownKey(Player player, CEType type) { return player.getUUID() + "_" + type.name(); }
    private static boolean isOnCooldown(Player player, String key) { return COOLDOWNS.getOrDefault(key, 0L) > player.level().getGameTime(); }
    private static void startCooldown(Player player, String key, int seconds) { COOLDOWNS.put(key, player.level().getGameTime() + seconds * TICKS_PER_SECOND); }

    private static void spawnParticles(LivingEntity entity, net.minecraft.core.particles.SimpleParticleType particle, int count) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(particle, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(), count, 0.4D, 0.5D, 0.4D, 0.02D);
    }

    private static void playSound(LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        entity.level().playSound(null, entity.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void removeBadEffects(Player player) {
        player.removeEffect(MobEffects.POISON);
        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.BLINDNESS);
        player.removeEffect(MobEffects.HUNGER);
        player.removeEffect(MobEffects.CONFUSION);
    }

    private static void appendMessage(StringBuilder builder, String text) {
        if (builder.length() > 0) builder.append(" | ");
        builder.append(text);
    }

    private static class DelayedDamage {
        private final long tick;
        private final UUID playerId;
        private final UUID targetId;
        private final float damage;

        private DelayedDamage(long tick, UUID playerId, UUID targetId, float damage) {
            this.tick = tick;
            this.playerId = playerId;
            this.targetId = targetId;
            this.damage = damage;
        }

        private ServerLevel levelRef() {
            net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return null;
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getPlayerByUUID(playerId) != null) return level;
            }
            return null;
        }
    }
}
