    package com.example.examplemod.cultivation;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.expansion.TuLinhHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class CultivationEvents {
    private static final UUID CULTIVATION_HEALTH_UUID = UUID.fromString("7db3c1c4-e7cd-4c67-a41d-fd99e548ef11");
    private static final UUID CULTIVATION_DAMAGE_UUID = UUID.fromString("04de735a-ef94-4219-b5e6-b5b516af43ad");
    private static final int EFFECT_REFRESH_TICKS = 219;
    private static final String MYSTIC_CULTIVATION_AURA_DISABLED = "MysticCultivationAuraDisabled";

    public static boolean isAuraDisabled(ServerPlayer player) {
        return player.getPersistentData().getBoolean(MYSTIC_CULTIVATION_AURA_DISABLED);
    }

    public static void setAuraEnabled(ServerPlayer player, boolean enabled) {
        player.getPersistentData().putBoolean(MYSTIC_CULTIVATION_AURA_DISABLED, !enabled);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CultivationData.root(player);
        applyStatsAndPrivileges(player);
        sendHighRealmJoinMessage(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        event.getEntity().getPersistentData().put("MysticCultivation", event.getOriginal().getPersistentData().getCompound("MysticCultivation").copy());
        CultivationData.refillVirtualHealth(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        applyStatsAndPrivileges(player);
        handleFreeze(player);
        handleRealmAura(player);
        CultivationTribulation.tick(player);
        handleToaTu(player);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        CultivationData.root(target).putBoolean("ToaTu", false);

        CultivationRealm targetRealm = CultivationData.getRealm(target);
        if (targetRealm == CultivationRealm.TIEN_DE) {
            event.setCanceled(true);
            target.setHealth(target.getMaxHealth());
            return;
        }

        float virtualHp = CultivationData.getVirtualHealth(target);
        if (virtualHp <= 0.0F) return;

        float damage = event.getAmount();
        if (virtualHp >= damage) {
            CultivationData.setVirtualHealth(target, virtualHp - damage);
            event.setCanceled(true);
            target.displayClientMessage(Component.literal("HP Tu Tiên: " + (long)Math.max(0.0F, virtualHp - damage) + " / " + (long)CultivationData.getVirtualHealthMax(target)).withStyle(ChatFormatting.RED), true);
        } else {
            CultivationData.setVirtualHealth(target, 0.0F);
            event.setAmount(damage - virtualHp);
            target.displayClientMessage(Component.literal("HP Tu Tiên đã vỡ!").withStyle(ChatFormatting.RED), true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player target && CultivationData.getRealm(target) == CultivationRealm.TIEN_DE) {
            event.setCanceled(true);
            target.setHealth(target.getMaxHealth());
            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        if (victim == killer) return;

        int reward;
        if (victim instanceof Player playerVictim) {
            CultivationRealm realm = CultivationData.getRealm(playerVictim);
            int stage = CultivationData.getStage(playerVictim);
            reward = CultivationUtil.playerKillReward(realm, stage);
            if (reward <= 0) return;
            CultivationData.addLinhKhi(killer, reward);
            killer.displayClientMessage(Component.literal("+" + reward + " linh khí từ tu sĩ " + realm.formatStage(stage)).withStyle(ChatFormatting.AQUA), true);
        } else {
            reward = CultivationUtil.mobKillReward(victim.getMaxHealth());
            CultivationData.addLinhKhi(killer, reward);
            killer.displayClientMessage(Component.literal("+" + reward + " linh khí").withStyle(ChatFormatting.AQUA), true);
        }
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        Object rawMessage = event.getMessage();
        String message = rawMessage instanceof Component component ? component.getString() : String.valueOf(rawMessage);
        String trimmed = message.trim();

        if (message.contains("[i]")) {
            event.setCanceled(true);
            long now = player.serverLevel().getGameTime();
            long cooldown = CultivationData.getCooldown(player, CultivationData.itemShowCooldownKey());
            if (cooldown > now) {
                long seconds = (cooldown - now + 19L) / 20L;
                player.sendSystemMessage(colored("&cBạn phải chờ " + seconds + "s nữa mới có thể show item."));
                return;
            }
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) {
                player.sendSystemMessage(colored("&cBạn phải cầm item trên tay để dùng [i]!"));
                return;
            }
            CultivationData.setCooldown(player, CultivationData.itemShowCooldownKey(), now + 5L * 20L);
            MutableComponent result = Component.empty();
            String[] parts = message.split("\\[i\\]", -1);
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    result.append(Component.literal(parts[i]).withStyle(ChatFormatting.WHITE));
                }
                if (i < parts.length - 1) {
                    result.append(itemDisplayComponent(stack));
                }
            }
            MutableComponent broadcast = Component.literal("<").withStyle(ChatFormatting.GRAY)
                    .append(player.getDisplayName())
                    .append(Component.literal("> ").withStyle(ChatFormatting.GRAY))
                    .append(result);
            player.server.getPlayerList().broadcastSystemMessage(broadcast, false);
            return;
        }

        if (trimmed.toLowerCase().startsWith("định ") || trimmed.toLowerCase().startsWith("dinh ")) {
            if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.BUOC_4)) {
                return;
            }
            String[] split = trimmed.split("\\s+", 2);
            if (split.length < 2) return;
            ServerPlayer target = player.server.getPlayerList().getPlayerByName(split[1].trim());
            if (target == null) {
                player.sendSystemMessage(colored("&cKhông tìm thấy người chơi: " + split[1].trim()));
                event.setCanceled(true);
                return;
            }
            event.setCanceled(true);
            CultivationCommands.useDinh(player, target);
        }
    }

    private static MutableComponent colored(String text) {
        return Component.literal(text.replace('&', ChatFormatting.PREFIX_CODE));
    }

    public static MutableComponent itemDisplayComponent(ItemStack stack) {
        MutableComponent name = stack.getHoverName().copy().withStyle(ChatFormatting.AQUA);
        MutableComponent display = Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(name)
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
        return display.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))));
    }


    private static void handleToaTu(ServerPlayer player) {
        if (!CultivationData.root(player).getBoolean("ToaTu")) return;
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.TRUC_CO)) {
            CultivationData.root(player).putBoolean("ToaTu", false);
            return;
        }
        long now = player.serverLevel().getGameTime();
        if (CultivationData.root(player).getLong("ToaTuNextTick") > now) return;
        CultivationData.root(player).putLong("ToaTuNextTick", now + 5L * 20L);
        CultivationRealm realm = CultivationData.getRealm(player);
        int stage = CultivationData.getStage(player);
        long cost = CultivationUtil.getBreakthroughCost(realm, stage);
        long gain = Math.max(100L, cost / 120L);
        gain = TuLinhHelper.applyMultiplier(player, gain);
        CultivationData.addLinhKhi(player, gain);
        player.displayClientMessage(Component.literal("Tọa tu +" + CultivationUtil.formatNumber(gain) + " linh khí").withStyle(ChatFormatting.AQUA), true);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 110, 10, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 110, 10, true, false, false));
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 18, 0.5D, 0.4D, 0.5D, 0.05D);
    }

    private static void applyStatsAndPrivileges(ServerPlayer player) {
        CultivationRealm realm = CultivationData.getRealm(player);
        int stage = CultivationData.getStage(player);
        int bonusHearts = CultivationUtil.totalBonusHearts(realm, stage);
        int bonusDamage = CultivationUtil.totalBonusDamage(realm, stage);

        int displayedBonusHearts = CultivationUtil.displayedBonusHearts(realm, stage);
        float virtualMax = CultivationUtil.virtualHealthMax(realm, stage);
        CultivationData.updateVirtualHealthMax(player, virtualMax);

        applyAttribute(player, Attributes.MAX_HEALTH, CULTIVATION_HEALTH_UUID, "Cultivation visible health", displayedBonusHearts * 2.0D, AttributeModifier.Operation.ADDITION);
        applyAttribute(player, Attributes.ATTACK_DAMAGE, CULTIVATION_DAMAGE_UUID, "Cultivation damage", bonusDamage, AttributeModifier.Operation.ADDITION);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        if (realm.isAtLeast(CultivationRealm.KIM_DAN)) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
        }

        if (realm.isAtLeast(CultivationRealm.NGUYEN_ANH)) {
            boolean flightEnabled = CultivationData.root(player).getBoolean("CultivationFlightEnabled");
            boolean inCombat = CultivationData.root(player).getLong("CultivationCombatUntil") > player.serverLevel().getGameTime();
            if (flightEnabled && !inCombat) {
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            } else if (!player.isCreative() && !player.isSpectator() && player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            CultivationData.root(player).putBoolean("CultivationFlightGranted", true);
        } else if (CultivationData.root(player).getBoolean("CultivationFlightGranted") && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            CultivationData.root(player).putBoolean("CultivationFlightGranted", false);
        }

        if (realm.isAtLeast(CultivationRealm.AM_HU)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_REFRESH_TICKS, 0, true, false, true));
        }

        if (realm == CultivationRealm.TIEN_DE) {
            player.setHealth(player.getMaxHealth());
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.CONFUSION);
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }

    private static void applyAttribute(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid, String name, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier old = instance.getModifier(uuid);
        if (old != null) instance.removeModifier(uuid);
        if (Math.abs(value) > 0.0001D) {
            instance.addPermanentModifier(new AttributeModifier(uuid, name, value, operation));
        }
    }

    private static void handleFreeze(ServerPlayer player) {
        long until = CultivationData.getFrozenUntil(player);
        if (until <= 0L) return;
        long now = player.serverLevel().getGameTime();
        if (now >= until) {
            CultivationData.clearFreeze(player);
            return;
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(CultivationData.frozenX(player), CultivationData.frozenY(player), CultivationData.frozenZ(player));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 255, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 255, true, false, false));
    }

    public static void performStepTeleport(ServerPlayer player) {
        CultivationRealm realm = CultivationData.getRealm(player);
        if (!realm.isAtLeast(CultivationRealm.BUOC_3)) {
            player.displayClientMessage(Component.literal("Cần đạt Khuy Niết để thuấn di.").withStyle(ChatFormatting.RED), true);
            return;
        }

        long now = player.serverLevel().getGameTime();
        if (now - CultivationData.getLastWPress(player) < 10L) {
            return;
        }
        CultivationData.setLastWPress(player, now);

        Vec3 look = player.getLookAngle().normalize();
        double distance = 9.0D;
        double x = player.getX() + look.x * distance;
        double y = player.getY() + look.y * 2.0D;
        double z = player.getZ() + look.z * distance;
        double beforeX = player.getX();
        double beforeY = player.getY();
        double beforeZ = player.getZ();

        player.serverLevel().sendParticles(ParticleTypes.PORTAL, beforeX, beforeY + 1.0D, beforeZ, 28, 0.5D, 0.8D, 0.5D, 0.25D);
        player.teleportTo(x, y, z);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 36, 0.5D, 0.8D, 0.5D, 0.25D);
        player.displayClientMessage(Component.literal("⚡ Thuấn di 9 block").withStyle(ChatFormatting.AQUA), true);
    }

    private static void handleRealmAura(ServerPlayer player) {
        if (isAuraDisabled(player)) return;
        CultivationRealm realm = CultivationData.getRealm(player);
        long time = player.serverLevel().getGameTime();
if (realm == CultivationRealm.TIEN_DE) {
    if (time % 6L != 0L) return;

    double x = player.getX();
    double y = player.getY();
    double z = player.getZ();

    // Điện là chính, rộng hơn một chút
    player.serverLevel().sendParticles(
            ParticleTypes.ELECTRIC_SPARK,
            x, y + 1.0D, z,
            4,
            0.62D, 0.80D, 0.62D,
            0.025D
    );

    // Tím nhẹ, rộng hơn
    player.serverLevel().sendParticles(
            ParticleTypes.PORTAL,
            x, y + 1.0D, z,
            3,
            0.60D, 0.80D, 0.60D,
            0.025D
    );

    // Tro bụi / khói, rộng hơn
    player.serverLevel().sendParticles(
            ParticleTypes.SMOKE,
            x, y + 0.85D, z,
            3,
            0.65D, 0.60D, 0.65D,
            0.006D
    );

    // Lửa đỏ nhiều hơn, rộng gần giống Đạp Thiên
    player.serverLevel().sendParticles(
            ParticleTypes.FLAME,
            x, y + 0.55D, z,
            6,
            0.85D, 0.25D, 0.85D,
            0.012D
    );

    // Xanh lá nhẹ, rộng hơn một chút nhưng vẫn ít
    if (time % 12L == 0L) {
        player.serverLevel().sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                x, y + 0.95D, z,
                2,
                0.50D, 0.65D, 0.50D,
                0.005D
        );
    }

    // Trắng rất ít, chỉ làm điểm sáng/sấm
    if (time % 28L == 0L) {
        player.serverLevel().sendParticles(
                ParticleTypes.END_ROD,
                x, y + 1.05D, z,
                1,
                0.30D, 0.40D, 0.30D,
                0.003D
        );
    }
} else if (realm == CultivationRealm.BUOC_4) {
            if (time % 6L != 0L) return;
            player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.2D, player.getZ(), 6, 0.8D, 0.1D, 0.8D, 0.02D);
            player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 8, 0.7D, 0.9D, 0.7D, 0.05D);
        } else if (realm == CultivationRealm.BUOC_3) {
            if (time % 10L != 0L) return;
            player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 6, 0.6D, 0.8D, 0.6D, 0.04D);
            player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 3, 0.5D, 0.7D, 0.5D, 0.03D);
        }
    }

    public static void spawnRealmAuraBurst(ServerPlayer player) {
        if (isAuraDisabled(player)) return;
        CultivationRealm realm = CultivationData.getRealm(player);
        if (realm == CultivationRealm.TIEN_DE) {
            player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 14, 0.8D, 1.0D, 0.8D, 0.05D);
            player.serverLevel().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.8D, 1.0D, 0.8D, 0.05D);
            player.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.7D, player.getZ(), 8, 0.7D, 0.5D, 0.7D, 0.03D);
            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 6, 0.7D, 0.8D, 0.7D, 0.02D);
            player.serverLevel().sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 0.9D, player.getZ(), 16, 0.9D, 0.9D, 0.9D, 0.02D);
            player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.1D, player.getZ(), 3, 0.5D, 0.6D, 0.5D, 0.01D);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8F, 1.2F);
        } else if (realm == CultivationRealm.BUOC_4) {
            player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.5D, player.getZ(), 60, 1.0D, 0.6D, 1.0D, 0.08D);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5F, 1.8F);
        } else if (realm == CultivationRealm.BUOC_3) {
            player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.9D, 1.0D, 0.9D, 0.08D);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void announceHighRealmBreakthrough(ServerPlayer player, CultivationRealm realm, int stage) {
        if (realm == CultivationRealm.BUOC_3) {
            player.server.getPlayerList().broadcastSystemMessage(colored("&d&l[Tu Vi] &f" + player.getName().getString() + " &7đã đột phá tới &dKhuy Niết&7!"), false);
            player.displayClientMessage(colored("&d&lKHỞI NIẾT! &7Linh thể niết bàn, thoát khỏi phàm thai"), false);
        } else if (realm == CultivationRealm.BUOC_4) {
            player.server.getPlayerList().broadcastSystemMessage(colored("&b&l[Tu Vi] &f" + player.getName().getString() + " &7đã bước vào cảnh giới &bĐạp Thiên&7!"), false);
            player.displayClientMessage(colored("&b&lĐẠP THIÊN! &7Một bước đạp trời, phá vỡ thiên mệnh"), false);
        } else if (realm == CultivationRealm.TIEN_DE) {
            player.server.getPlayerList().broadcastSystemMessage(colored("&6&l[Tu Vi] &f" + player.getName().getString() + " &eđã chứng đạo &6Tiên Đế&e, vạn giới chấn động!"), false);
            player.displayClientMessage(colored("&6&lTIÊN ĐẾ GIÁNG LÂM! &eĐế uy bao phủ vạn giới"), false);
        }
        spawnRealmAuraBurst(player);
    }

    private static void sendHighRealmJoinMessage(ServerPlayer player) {
        CultivationRealm realm = CultivationData.getRealm(player);
        String message = switch (realm) {
            case AM_HU -> "✦ Tu Sĩ Âm Hư " + player.getName().getString() + " đã xuất hiện ✦";
            case DUONG_THUC -> "✧ Đại Năng Dương Thực " + player.getName().getString() + " giáng lâm ✧";
            case BUOC_3 -> "⚡ Khuy Niết Đại Năng " + player.getName().getString() + " đã hàng lâm nhân giới ⚡";
            case BUOC_4 -> "☯ Đạp Thiên Chí Tôn " + player.getName().getString() + " xuất hiện, thiên địa chấn động ☯";
            case TIEN_DE -> "♛ Tiên Đế " + player.getName().getString() + " giáng thế, vạn đạo quy phục ♛";
            default -> null;
        };
        if (message == null) return;
        ChatFormatting color = switch (realm) {
            case AM_HU -> ChatFormatting.DARK_PURPLE;
            case DUONG_THUC -> ChatFormatting.GOLD;
            case BUOC_3 -> ChatFormatting.AQUA;
            case BUOC_4 -> ChatFormatting.DARK_RED;
            case TIEN_DE -> ChatFormatting.YELLOW;
            default -> ChatFormatting.WHITE;
        };
        player.server.getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(color, ChatFormatting.BOLD), false);
    }
}
