package com.example.examplemod.cultivation;

import com.example.examplemod.ExampleMod;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class CultivationCommands {
    private static final long COOLDOWN_TICKS = 120L * 20L;
    private static final long TU_BAO_COOLDOWN_TICKS = 300L * 20L;
    private static final long RAC_DAU_COOLDOWN_TICKS = 300L * 20L;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("tuvi")
                        .executes(context -> showTuVi(context.getSource().getPlayerOrException()))
                        .then(Commands.literal("help")
                                .executes(context -> tutienHelp(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("top")
                                .executes(context -> showTop(context.getSource().getPlayerOrException())))
        );

        event.getDispatcher().register(
                Commands.literal("tutien")
                        .then(Commands.literal("help")
                                .executes(context -> tutienHelp(context.getSource().getPlayerOrException())))
        );

        event.getDispatcher().register(
                Commands.literal("bay")
                        .executes(context -> bay(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("tu")
                        .executes(context -> toaTu(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("nadi")
                        .executes(context -> naDi(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("dotpha")
                        .executes(context -> dotPha(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("apche")
                        .then(Commands.argument("user", EntityArgument.player())
                                .executes(context -> apChe(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "user"))))
        );

        event.getDispatcher().register(
                Commands.literal("dietsat")
                        .then(Commands.argument("user", EntityArgument.player())
                                .executes(context -> dietSat(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "user"))))
        );

        event.getDispatcher().register(
                Commands.literal("dinh")
                        .then(Commands.argument("user", EntityArgument.player())
                                .executes(context -> useDinh(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "user"))))
        );

        event.getDispatcher().register(
                Commands.literal("hophonghoanvu")
                        .then(Commands.argument("user", EntityArgument.player())
                                .executes(context -> hoPhongHoanVu(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "user"))))
        );

        event.getDispatcher().register(
                Commands.literal("sucdiathanhthon")
                        .then(Commands.argument("user", EntityArgument.player())
                                .executes(context -> sucDiaThanhThon(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "user"))))
        );

        event.getDispatcher().register(
                Commands.literal("racdauthanhbinh")
                        .executes(context -> racDauThanhBinh(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("tubao")
                        .executes(context -> tuBao(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("aura")
                        .then(Commands.literal("on")
                                .executes(context -> aura(context.getSource().getPlayerOrException(), true)))
                        .then(Commands.literal("off")
                                .executes(context -> aura(context.getSource().getPlayerOrException(), false)))
        );

        event.getDispatcher().register(
                Commands.literal("thiennhan")
                        .then(Commands.argument("user", EntityArgument.player())
                                .executes(context -> thiennhan(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "user"))))
        );

        event.getDispatcher().register(
                Commands.literal("tuviadd")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("user", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0L))
                                        .executes(context -> adminAdd(
                                                context.getSource().getPlayerOrException(),
                                                EntityArgument.getPlayer(context, "user"),
                                                LongArgumentType.getLong(context, "amount")))))
        );

        event.getDispatcher().register(
                Commands.literal("tuviset")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("user", EntityArgument.player())
                                .then(Commands.argument("realm", StringArgumentType.word())
                                        .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                                .executes(context -> adminSet(
                                                        context.getSource().getPlayerOrException(),
                                                        EntityArgument.getPlayer(context, "user"),
                                                        StringArgumentType.getString(context, "realm"),
                                                        IntegerArgumentType.getInteger(context, "stage"))))))
        );
        event.getDispatcher().register(
                Commands.literal("settuvi")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("user", EntityArgument.player())
                                .then(Commands.argument("realm", StringArgumentType.word())
                                        .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                                .executes(context -> adminSet(
                                                        context.getSource().getPlayerOrException(),
                                                        EntityArgument.getPlayer(context, "user"),
                                                        StringArgumentType.getString(context, "realm"),
                                                        IntegerArgumentType.getInteger(context, "stage"))))))
        );
    }



    private static int toaTu(ServerPlayer player) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.TRUC_CO)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Trúc Cơ để tọa tu."));
            return 0;
        }
        boolean enabled = !CultivationData.root(player).getBoolean("ToaTu");
        CultivationData.root(player).putBoolean("ToaTu", enabled);
        player.sendSystemMessage(Component.literal(enabled ? "§aBắt đầu tọa tu. Không di chuyển/đánh/xài item." : "§eĐã dừng tọa tu."));
        return 1;
    }

    private static int bay(ServerPlayer player) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.NGUYEN_ANH)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Nguyên Anh để dùng /bay."));
            return 0;
        }
        if (CultivationData.root(player).getLong("CultivationCombatUntil") > player.serverLevel().getGameTime()) {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
            player.sendSystemMessage(Component.literal("§cĐang giao chiến với player khác, không thể bay."));
            return 0;
        }
        boolean enabled = !CultivationData.root(player).getBoolean("CultivationFlightEnabled");
        CultivationData.root(player).putBoolean("CultivationFlightEnabled", enabled);
        player.getAbilities().mayfly = enabled;
        if (!enabled) player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.sendSystemMessage(Component.literal(enabled ? "§aĐã bật bay." : "§eĐã tắt bay."));
        return 1;
    }

    private static int naDi(ServerPlayer player) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.NGUYEN_ANH)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Nguyên Anh để dùng /nadi."));
            return 0;
        }
        if (!checkCooldown(player, "NaDiCooldown", "/nadi")) return 0;
        long now = player.serverLevel().getGameTime();
        CultivationData.setCooldown(player, "NaDiCooldown", now + 90L * 20L);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 2, true, false, true));
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2D, player.getZ(), 30, 0.4D, 0.1D, 0.4D, 0.08D);
        player.sendSystemMessage(Component.literal("§bNa Di: tăng tốc 20 giây."));
        return 1;
    }

    private static int tutienHelp(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("━━━━━━━━ /tutien help ━━━━━━━━").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("/tuvi - xem tu vi | /tuvi top - bảng xếp hạng | /dotpha - đột phá").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("/tu - tọa tu tăng linh khí | /bay - bật/tắt bay từ Nguyên Anh | /nadi - tăng tốc từ Nguyên Anh").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("/aura on/off - bật tắt hào quang | /sucdiathanhthon <user> - Khuy Niết+ dịch tới người chơi").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Đặc quyền: Kim Đan no đói, Nguyên Anh bay/Na Di, Âm Hư nhìn đêm, Khuy Niết thuấn di, Đạp Thiên định thân, Tiên Đế miễn hiệu ứng xấu.").withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Quái tu tiên: 5-10 phút sinh 1-7 con gần player, tối đa Đạp Thiên 5, có drop đan/vật phẩm hiếm từ Khuy Niết+. ").withStyle(ChatFormatting.LIGHT_PURPLE));
        return 1;
    }

    private static int showTuVi(ServerPlayer player) {
        CultivationUtil.sendCultivationPanel(player);
        return 1;
    }

    private static int showTop(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("━━━━━━━━ TOP TU VI ━━━━━━━━").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.server.getPlayerList().getPlayers().stream()
                .sorted((a, b) -> {
                    CultivationRealm ar = CultivationData.getRealm(a);
                    CultivationRealm br = CultivationData.getRealm(b);
                    int realmCompare = Integer.compare(br.ordinal(), ar.ordinal());
                    if (realmCompare != 0) return realmCompare;
                    int stageCompare = Integer.compare(CultivationData.getStage(b), CultivationData.getStage(a));
                    if (stageCompare != 0) return stageCompare;
                    return Long.compare(CultivationData.getLinhKhi(b), CultivationData.getLinhKhi(a));
                })
                .limit(10)
                .forEach(p -> player.sendSystemMessage(
                        Component.literal("• ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(p.getName().getString()).withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                                .append(CultivationUtil.realmComponent(CultivationData.getRealm(p), CultivationData.getStage(p)))
                                .append(Component.literal(" | " + CultivationUtil.formatNumber(CultivationData.getLinhKhi(p)) + " linh khí").withStyle(ChatFormatting.AQUA))
                ));
        return 1;
    }

    private static int dotPha(ServerPlayer player) {
        if (CultivationTribulation.isActive(player)) {
            player.sendSystemMessage(Component.literal("§cNgươi đang độ kiếp rồi."));
            return 0;
        }
        CultivationRealm realm = CultivationData.getRealm(player);
        int stage = CultivationData.getStage(player);
        if (!CultivationUtil.canAdvance(realm, stage)) {
            player.sendSystemMessage(Component.literal("§eNgươi đã là Tiên Đế, không còn cảnh giới cao hơn."));
            return 0;
        }
        long cost = CultivationUtil.getBreakthroughCost(realm, stage);
        long current = CultivationData.getLinhKhi(player);
        if (current < cost) {
            player.sendSystemMessage(Component.literal("§cChưa đủ linh khí. Cần " + CultivationUtil.formatNumber(cost) + ", hiện có " + CultivationUtil.formatNumber(current) + "."));
            return 0;
        }
        double chance = CultivationUtil.getSuccessChance(realm, stage);
        player.sendSystemMessage(Component.literal("⚡ Bắt đầu độ kiếp: ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                .append(CultivationUtil.realmComponent(realm, stage))
                .append(Component.literal(" → " + CultivationUtil.nextRealmOrStageName(realm, stage)).withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.literal("Tỉ lệ thành công: " + CultivationUtil.formatChance(chance) + " | Thất bại: chết + tụt 1 bậc").withStyle(ChatFormatting.RED));
        CultivationTribulation.start(player, realm, stage, cost, chance);
        return 1;
    }

    private static int apChe(ServerPlayer player, ServerPlayer target) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.DUONG_THUC)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Dương Thực để dùng /apche."));
            return 0;
        }
        if (CultivationData.getRealm(target).isAtLeast(CultivationRealm.AM_HU)) {
            player.sendSystemMessage(Component.literal("§cChỉ áp chế được người có tu vi dưới Âm Hư."));
            return 0;
        }
        if (CultivationData.getRealm(target) == CultivationRealm.TIEN_DE) {
            player.sendSystemMessage(Component.literal("§cKhông thể áp chế Tiên Đế."));
            return 0;
        }
        if (!checkCooldown(player, CultivationData.apCheCooldownKey(), "/apche")) return 0;
        long now = player.serverLevel().getGameTime();
        CultivationData.setCooldown(player, CultivationData.apCheCooldownKey(), now + COOLDOWN_TICKS);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 3));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 10 * 20, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10 * 20, 2));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10 * 20, 0));
        player.server.getPlayerList().broadcastSystemMessage(Component.literal("✧ " + player.getName().getString() + " đã áp chế " + target.getName().getString() + "! ✧").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int dietSat(ServerPlayer player, ServerPlayer target) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.BUOC_3)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Khuy Niết để dùng /dietsat."));
            return 0;
        }
        if (CultivationData.getRealm(target).isAtLeast(CultivationRealm.AM_HU)) {
            player.sendSystemMessage(Component.literal("§cChỉ diệt sát được người có tu vi dưới Âm Hư."));
            return 0;
        }
        if (CultivationData.getRealm(target) == CultivationRealm.TIEN_DE) {
            player.sendSystemMessage(Component.literal("§cKhông thể diệt sát Tiên Đế."));
            return 0;
        }
        if (!checkCooldown(player, CultivationData.dietSatCooldownKey(), "/dietsat")) return 0;
        long now = player.serverLevel().getGameTime();
        CultivationData.setCooldown(player, CultivationData.dietSatCooldownKey(), now + COOLDOWN_TICKS);
        player.server.getPlayerList().broadcastSystemMessage(Component.literal("⚡ " + player.getName().getString() + " đã diệt sát " + target.getName().getString() + "! ⚡").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
        target.hurt(target.damageSources().genericKill(), Float.MAX_VALUE);
        return 1;
    }

    public static int useDinh(ServerPlayer player, ServerPlayer target) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.BUOC_4)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Đạp Thiên để dùng Định."));
            return 0;
        }
        if (CultivationData.getRealm(target) == CultivationRealm.TIEN_DE) {
            player.sendSystemMessage(Component.literal("§cKhông thể định thân Tiên Đế."));
            return 0;
        }
        if (!checkCooldown(player, CultivationData.dinhCooldownKey(), "Định")) return 0;
        long now = player.serverLevel().getGameTime();
        CultivationData.setCooldown(player, CultivationData.dinhCooldownKey(), now + COOLDOWN_TICKS);
        CultivationData.freeze(target, now + 5 * 20L);
        player.server.getPlayerList().broadcastSystemMessage(Component.literal("☯ Định! " + target.getName().getString() + " bị " + player.getName().getString() + " định thân 5 giây. ☯").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
        return 1;
    }

    private static int hoPhongHoanVu(ServerPlayer player, ServerPlayer target) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.BUOC_3)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Khuy Niết để dùng Hô Phong Hoán Vũ."));
            return 0;
        }
        if (target == player) {
            player.sendSystemMessage(Component.literal("§cKhông thể tự dùng Hô Phong Hoán Vũ lên bản thân."));
            return 0;
        }
        if (target.distanceTo(player) > 25.0F) {
            player.sendSystemMessage(Component.literal("§cMục tiêu quá xa. Tối đa 25 block."));
            return 0;
        }
        if (target.isCreative() || target.isSpectator()) {
            player.sendSystemMessage(Component.literal("§cKhông thể dùng lên Creative/Spectator."));
            return 0;
        }
        if (!checkCooldown(player, CultivationData.hoPhongCooldownKey(), "Hô Phong Hoán Vũ")) return 0;
        long now = player.serverLevel().getGameTime();
        CultivationData.setCooldown(player, CultivationData.hoPhongCooldownKey(), now + COOLDOWN_TICKS);

        target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 1.9D, 0.0D));
        target.hurtMarked = true;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 2));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 1));
        float damage = 8.0F + target.getMaxHealth() * 0.06F;
        target.hurt(player.damageSources().magic(), damage);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 1.2D, 1.0D, 1.2D, 0.08D);
        level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 1.2D, 1.0D, 1.2D, 0.12D);
        level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.8F, 1.6F);
        player.server.getPlayerList().broadcastSystemMessage(
                Component.literal("§b§lHô Phong Hoán Vũ! §f" + player.getName().getString() + " §7hất văng §f" + target.getName().getString() + " §7lên trời."),
                false
        );
        return 1;
    }

    private static int sucDiaThanhThon(ServerPlayer player, ServerPlayer target) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.BUOC_3)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Khuy Niết để dùng Súc Địa Thành Thốn."));
            return 0;
        }
        if (target == player) {
            player.sendSystemMessage(Component.literal("§cKhông thể dùng Súc Địa Thành Thốn lên bản thân."));
            return 0;
        }
        if (target.isSpectator()) {
            player.sendSystemMessage(Component.literal("§cKhông thể dịch chuyển tới người chơi đang Spectator."));
            return 0;
        }
        if (player.serverLevel() != target.serverLevel()) {
            player.sendSystemMessage(Component.literal("§cMục tiêu đang ở thế giới khác, chưa thể dùng Súc Địa Thành Thốn."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        double beforeX = player.getX();
        double beforeY = player.getY();
        double beforeZ = player.getZ();

        double angle = Math.toRadians(target.getYRot() + 180.0F);
        double x = target.getX() + Math.sin(angle) * 1.5D;
        double y = target.getY();
        double z = target.getZ() - Math.cos(angle) * 1.5D;

        level.sendParticles(ParticleTypes.PORTAL, beforeX, beforeY + 1.0D, beforeZ, 28, 0.45D, 0.75D, 0.45D, 0.18D);
        level.sendParticles(ParticleTypes.END_ROD, beforeX, beforeY + 1.0D, beforeZ, 8, 0.35D, 0.55D, 0.35D, 0.02D);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, 1.25F);

        player.teleportTo(x, y, z);
        player.fallDistance = 0.0F;

        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 36, 0.5D, 0.85D, 0.5D, 0.2D);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.35D, 0.65D, 0.35D, 0.02D);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.1F);

        player.sendSystemMessage(Component.literal("§bSúc Địa Thành Thốn! §7Đã dịch chuyển tới §f" + target.getName().getString() + "§7."));
        target.sendSystemMessage(Component.literal("§d" + player.getName().getString() + " §7đã dùng §bSúc Địa Thành Thốn §7dịch chuyển tới ngươi."));
        return 1;
    }

    private static int racDauThanhBinh(ServerPlayer player) {
        if (CultivationData.getRealm(player) != CultivationRealm.TIEN_DE) {
            player.sendSystemMessage(Component.literal("§cChỉ Tiên Đế mới có thể dùng Rắc Đậu Thành Binh."));
            return 0;
        }
        if (!checkCooldown(player, CultivationData.racDauCooldownKey(), "Rắc Đậu Thành Binh")) return 0;
        long now = player.serverLevel().getGameTime();
        CultivationData.setCooldown(player, CultivationData.racDauCooldownKey(), now + RAC_DAU_COOLDOWN_TICKS);
        ServerLevel level = player.serverLevel();
        removeOldSoldiers(player);
        for (int i = 0; i < 3; i++) {
            Wolf wolf = EntityType.WOLF.create(level);
            if (wolf == null) continue;
            double angle = Math.toRadians(player.getYRot() + i * 120.0F);
            wolf.moveTo(player.getX() + Math.cos(angle) * 2.0D, player.getY(), player.getZ() + Math.sin(angle) * 2.0D, player.getYRot(), 0.0F);
            wolf.setTame(true);
            wolf.setOwnerUUID(player.getUUID());
            wolf.setCustomName(Component.literal("Đậu Binh của " + player.getName().getString()).withStyle(ChatFormatting.GOLD));
            wolf.setCustomNameVisible(true);
            wolf.getPersistentData().putBoolean("MysticRacDauThanhBinh", true);
            wolf.getPersistentData().putUUID("MysticRacDauOwner", player.getUUID());
            applySoldierStats(player, wolf);
            level.addFreshEntity(wolf);
        }
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 60, 1.5D, 1.0D, 1.5D, 0.12D);
        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.server.getPlayerList().broadcastSystemMessage(Component.literal("§6§lRắc Đậu Thành Binh! §e" + player.getName().getString() + " triệu hồi 3 đậu binh hộ vệ."), false);
        return 1;
    }

    private static void removeOldSoldiers(ServerPlayer player) {
        player.serverLevel().getEntitiesOfClass(Wolf.class, player.getBoundingBox().inflate(96.0D), wolf ->
                wolf.getPersistentData().getBoolean("MysticRacDauThanhBinh")
                        && wolf.getPersistentData().hasUUID("MysticRacDauOwner")
                        && wolf.getPersistentData().getUUID("MysticRacDauOwner").equals(player.getUUID())
        ).forEach(Wolf::discard);
    }

    private static void applySoldierStats(ServerPlayer player, Wolf wolf) {
        double hp = Math.max(20.0D, player.getMaxHealth() * 0.8D);
        double damage = 4.0D;
        AttributeInstance playerAttack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (playerAttack != null) damage = Math.max(4.0D, playerAttack.getValue() * 0.8D);
        double armor = 0.0D;
        AttributeInstance playerArmor = player.getAttribute(Attributes.ARMOR);
        if (playerArmor != null) armor = Math.max(0.0D, playerArmor.getValue() * 0.8D);
        if (wolf.getAttribute(Attributes.MAX_HEALTH) != null) wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        if (wolf.getAttribute(Attributes.ATTACK_DAMAGE) != null) wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        if (wolf.getAttribute(Attributes.ARMOR) != null) wolf.getAttribute(Attributes.ARMOR).setBaseValue(armor);
        wolf.setHealth(wolf.getMaxHealth());
    }

    private static int tuBao(ServerPlayer player) {
        CultivationRealm realm = CultivationData.getRealm(player);
        if (!realm.isAtLeast(CultivationRealm.BUOC_3)) {
            player.sendSystemMessage(Component.literal("§cChỉ người đạt Khuy Niết trở lên mới có thể dùng /tubao."));
            return 0;
        }
        if (!checkCooldown(player, CultivationData.tuBaoCooldownKey(), "Tu Báo")) return 0;
        long now = player.serverLevel().getGameTime();
        CultivationData.setCooldown(player, CultivationData.tuBaoCooldownKey(), now + TU_BAO_COOLDOWN_TICKS);
        Component message = switch (realm) {
            case BUOC_3 -> Component.literal("§d§l[Tu Báo] §f" + player.getName().getString() + " §7khai mở linh thể §dKhuy Niết§7!");
            case BUOC_4 -> Component.literal("§b§l[Tu Báo] §f" + player.getName().getString() + " §7đạp phá thiên đạo, hiện thân §bĐạp Thiên§7!");
            case TIEN_DE -> Component.literal("§6§l[Tu Báo] §f" + player.getName().getString() + " §ehiển lộ đế uy §6Tiên Đế§e, chúng sinh kính phục!");
            default -> Component.literal("§d§l[Tu Báo] §f" + player.getName().getString() + " §7hiển lộ tu vi §d" + realm.getDisplayName() + "§7!");
        };
        player.server.getPlayerList().broadcastSystemMessage(message, false);
        CultivationEvents.spawnRealmAuraBurst(player);
        return 1;
    }


    private static int aura(ServerPlayer player, boolean enabled) {
        CultivationEvents.setAuraEnabled(player, enabled);
        if (enabled) {
            player.sendSystemMessage(Component.literal("§aĐã bật aura cảnh giới Khuy Niết / Đạp Thiên / Tiên Đế."));
        } else {
            player.sendSystemMessage(Component.literal("§cĐã tắt aura cảnh giới Khuy Niết / Đạp Thiên / Tiên Đế."));
        }
        return 1;
    }

    private static int thiennhan(ServerPlayer player, ServerPlayer target) {
        boolean isOp = player.server.getPlayerList().isOp(player.getGameProfile());
        if (!isOp && !CultivationData.getRealm(player).isAtLeast(CultivationRealm.VAN_DINH)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Vấn Đỉnh hoặc có quyền OP để dùng /thiennhan."));
            return 0;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, viewerInventory, viewer) -> new InventoryViewMenu(containerId, viewerInventory, target.getInventory()),
                Component.literal("§bThiên Nhãn: §f" + target.getName().getString())
        ));
        player.sendSystemMessage(Component.literal("§aĐã mở kho đồ của " + target.getName().getString() + " bằng Thiên Nhãn."));
        return 1;
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

    private static int adminAdd(ServerPlayer sender, ServerPlayer target, long amount) {
        CultivationData.addLinhKhi(target, amount);
        sender.sendSystemMessage(Component.literal("§aĐã cộng " + CultivationUtil.formatNumber(amount) + " linh khí cho " + target.getName().getString() + "."));
        return 1;
    }

    private static int adminSet(ServerPlayer sender, ServerPlayer target, String realmName, int stage) {
        CultivationRealm realm = CultivationRealm.safeValueOf(realmName.toUpperCase());
        int safeStage = Math.min(stage, realm.getMaxStage());
        CultivationData.setRealmStage(target, realm, safeStage);
        sender.sendSystemMessage(Component.literal("§aĐã set " + target.getName().getString() + " thành " + realm.formatStage(safeStage) + "."));
        if (realm.isAtLeast(CultivationRealm.BUOC_3)) {
            CultivationEvents.announceHighRealmBreakthrough(target, realm, safeStage);
        }
        return 1;
    }
}
