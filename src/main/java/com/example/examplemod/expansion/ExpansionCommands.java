package com.example.examplemod.expansion;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.cultivation.CultivationData;
import com.example.examplemod.cultivation.CultivationRealm;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class ExpansionCommands {
    private static final String SECT = "MysticSect";
    private static final Map<UUID, SectInvite> INVITES = new HashMap<>();

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("i").executes(ctx -> showInfo(ctx.getSource().getPlayerOrException())));
        event.getDispatcher().register(Commands.literal("info").executes(ctx -> showInfo(ctx.getSource().getPlayerOrException())));

        event.getDispatcher().register(Commands.literal("monphai")
                .executes(ctx -> sectInfo(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("tao")
                        .then(Commands.argument("ten", StringArgumentType.word())
                                .then(Commands.argument("loai", StringArgumentType.word())
                                        .executes(ctx -> createSect(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "ten"), StringArgumentType.getString(ctx, "loai"))))))
                .then(Commands.literal("create")
                        .then(Commands.argument("ten", StringArgumentType.word())
                                .then(Commands.argument("loai", StringArgumentType.word())
                                        .executes(ctx -> createSect(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "ten"), StringArgumentType.getString(ctx, "loai"))))))
                .then(Commands.literal("info")
                        .executes(ctx -> sectInfo(ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("ten", StringArgumentType.word())
                                .executes(ctx -> sectInfoByName(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "ten")))))
                .then(Commands.literal("list").executes(ctx -> listSects(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("danhsach").executes(ctx -> listSects(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("top").executes(ctx -> topSects(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("members").executes(ctx -> members(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("thanhvien").executes(ctx -> members(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> invite(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("moi")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> invite(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("accept").executes(ctx -> acceptInvite(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("chapnhan").executes(ctx -> acceptInvite(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("deny").executes(ctx -> denyInvite(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("tuchoi").executes(ctx -> denyInvite(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("leave").executes(ctx -> leave(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("roi").executes(ctx -> leave(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("kick")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> kick(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("duoi")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> kick(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("disband").executes(ctx -> disband(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("giaitan").executes(ctx -> disband(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("sethome").executes(ctx -> setHome(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("home").executes(ctx -> home(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("promote")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> promote(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("thangchuc")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> promote(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("demote")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> demote(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("hachuc")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> demote(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("transfer")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> transfer(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("chuyenchu")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> transfer(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("chat")
                        .then(Commands.argument("noidung", StringArgumentType.greedyString())
                                .executes(ctx -> sectChat(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "noidung")))))
        );

        event.getDispatcher().register(Commands.literal("vanthitoc").executes(ctx -> vanThiToc(ctx.getSource().getPlayerOrException())));
        event.getDispatcher().register(Commands.literal("thonhondaitran").executes(ctx -> thonHonDaiTran(ctx.getSource().getPlayerOrException())));
        event.getDispatcher().register(Commands.literal("cuonghuyet").executes(ctx -> cuongHuyet(ctx.getSource().getPlayerOrException())));
        event.getDispatcher().register(Commands.literal("kiemkhihoahinh").executes(ctx -> kiemKhiHoaHinh(ctx.getSource().getPlayerOrException())));
        event.getDispatcher().register(Commands.literal("hoalienhoa").executes(ctx -> hoaLienHoa(ctx.getSource().getPlayerOrException())));
    }

    private static int showInfo(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cHãy cầm vật phẩm cần xem trên tay."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("══════ Thông tin vật phẩm ══════").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(stack.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        for (EnhancementHelper.Type type : EnhancementHelper.Type.values()) {
            if (EnhancementHelper.canApply(stack, type)) player.sendSystemMessage(EnhancementHelper.infoLine(stack, type));
        }
        return 1;
    }

    private static CompoundTag sectRoot(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(SECT)) tag.put(SECT, new CompoundTag());
        return tag.getCompound(SECT);
    }

    private static boolean hasSect(ServerPlayer player) {
        return sectRoot(player).contains("Name");
    }

    private static String sectName(ServerPlayer player) {
        return sectRoot(player).getString("Name");
    }

    private static boolean sameSect(ServerPlayer a, ServerPlayer b) {
        return hasSect(a) && hasSect(b) && sectName(a).equals(sectName(b));
    }

    private static int createSect(ServerPlayer player, String name, String type) {
        if (!CultivationData.getRealm(player).isAtLeast(CultivationRealm.KIM_DAN)) {
            player.sendSystemMessage(Component.literal("§cCần đạt Kim Đan trở lên để tạo môn phái."));
            return 0;
        }
        type = type.toLowerCase();
        if (!type.equals("luyenthi") && !type.equals("luyenhon") && !type.equals("luyenthe") && !type.equals("luyenkiem") && !type.equals("tudao")) {
            player.sendSystemMessage(Component.literal("§cLoại môn phái: luyenthi, luyenhon, luyenthe, luyenkiem, tudao"));
            return 0;
        }
        CompoundTag sect = sectRoot(player);
        if (sect.contains("Name")) {
            player.sendSystemMessage(Component.literal("§cBạn đã có môn phái."));
            return 0;
        }
        sect.putString("Name", name);
        sect.putString("Type", type);
        sect.putString("Role", "Tông Chủ");
        sect.putInt("Level", 1);
        sect.putUUID("Owner", player.getUUID());
        player.server.getPlayerList().broadcastSystemMessage(Component.literal("§6✦ " + player.getName().getString() + " đã khai lập môn phái §e" + name + " §7(" + type + ")"), false);
        return 1;
    }

    private static int sectInfo(ServerPlayer player) {
        CompoundTag sect = sectRoot(player);
        if (!sect.contains("Name")) {
            player.sendSystemMessage(Component.literal("§eBạn chưa thuộc môn phái nào. Tạo: /monphai tao <tên> <luyenthi|luyenhon|luyenthe|luyenkiem|tudao>"));
            player.sendSystemMessage(Component.literal("§7Lệnh khác: /monphai list, /monphai top, /monphai accept, /monphai deny"));
            return 0;
        }
        showSectInfo(player, sect.getString("Name"));
        return 1;
    }

    private static int sectInfoByName(ServerPlayer player, String name) {
        if (!sectExistsOnline(player.server, name)) {
            player.sendSystemMessage(Component.literal("§cKhông tìm thấy môn phái online tên: " + name));
            return 0;
        }
        showSectInfo(player, name);
        return 1;
    }

    private static void showSectInfo(ServerPlayer viewer, String name) {
        List<ServerPlayer> members = onlineMembers(viewer.server, name);
        CompoundTag sample = members.isEmpty() ? sectRoot(viewer) : sectRoot(members.get(0));
        long totalLinhKhi = 0L;
        for (ServerPlayer member : members) totalLinhKhi += CultivationData.getLinhKhi(member);

        viewer.sendSystemMessage(Component.literal("══════ Môn Phái ══════").withStyle(ChatFormatting.GOLD));
        viewer.sendSystemMessage(Component.literal("Tên: " + name).withStyle(ChatFormatting.YELLOW));
        viewer.sendSystemMessage(Component.literal("Loại: " + sample.getString("Type")).withStyle(ChatFormatting.AQUA));
        viewer.sendSystemMessage(Component.literal("Cấp: " + sample.getInt("Level")).withStyle(ChatFormatting.LIGHT_PURPLE));
        viewer.sendSystemMessage(Component.literal("Thành viên online: " + members.size() + "/" + memberLimit(sample.getInt("Level"))).withStyle(ChatFormatting.GREEN));
        viewer.sendSystemMessage(Component.literal("Tổng linh khí online: " + totalLinhKhi).withStyle(ChatFormatting.AQUA));
        if (hasHome(sample)) {
            viewer.sendSystemMessage(Component.literal("Home: đã đặt").withStyle(ChatFormatting.GREEN));
        } else {
            viewer.sendSystemMessage(Component.literal("Home: chưa đặt").withStyle(ChatFormatting.GRAY));
        }
        if (hasSect(viewer) && sectName(viewer).equals(name)) {
            viewer.sendSystemMessage(Component.literal("Chức vụ của bạn: " + sectRoot(viewer).getString("Role")).withStyle(ChatFormatting.GREEN));
        }
    }

    private static int listSects(ServerPlayer player) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ServerPlayer p : player.server.getPlayerList().getPlayers()) {
            if (hasSect(p)) counts.merge(sectName(p), 1, Integer::sum);
        }
        player.sendSystemMessage(Component.literal("══════ Danh sách Môn Phái online ══════").withStyle(ChatFormatting.GOLD));
        if (counts.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Chưa có môn phái nào có thành viên online."));
            return 0;
        }
        counts.forEach((name, count) -> player.sendSystemMessage(Component.literal("§e" + name + " §7- online: §a" + count)));
        return counts.size();
    }

    private static int topSects(ServerPlayer player) {
        Map<String, Long> totals = new HashMap<>();
        for (ServerPlayer p : player.server.getPlayerList().getPlayers()) {
            if (hasSect(p)) totals.merge(sectName(p), CultivationData.getLinhKhi(p), Long::sum);
        }
        player.sendSystemMessage(Component.literal("══════ Top Môn Phái theo Linh Khí online ══════").withStyle(ChatFormatting.GOLD));
        if (totals.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Chưa có dữ liệu môn phái online."));
            return 0;
        }
        List<Map.Entry<String, Long>> entries = new ArrayList<>(totals.entrySet());
        entries.sort(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()));
        int rank = 1;
        for (Map.Entry<String, Long> entry : entries) {
            player.sendSystemMessage(Component.literal("§6#" + rank + " §e" + entry.getKey() + " §7- §b" + entry.getValue() + " linh khí"));
            if (++rank > 10) break;
        }
        return entries.size();
    }

    private static int members(ServerPlayer player) {
        if (!hasSect(player)) {
            player.sendSystemMessage(Component.literal("§cBạn chưa có môn phái."));
            return 0;
        }
        List<ServerPlayer> members = onlineMembers(player.server, sectName(player));
        player.sendSystemMessage(Component.literal("══════ Thành viên online: " + sectName(player) + " ══════").withStyle(ChatFormatting.GOLD));
        for (ServerPlayer member : members) {
            CompoundTag sect = sectRoot(member);
            player.sendSystemMessage(Component.literal("§e" + member.getName().getString() + " §7- §a" + sect.getString("Role") + " §7- Linh khí: §b" + CultivationData.getLinhKhi(member)));
        }
        return members.size();
    }

    private static int invite(ServerPlayer actor, ServerPlayer target) {
        if (!hasSect(actor)) {
            actor.sendSystemMessage(Component.literal("§cBạn chưa có môn phái."));
            return 0;
        }
        if (!canInvite(actor)) {
            actor.sendSystemMessage(Component.literal("§cChức vụ của bạn không có quyền mời thành viên."));
            return 0;
        }
        if (hasSect(target)) {
            actor.sendSystemMessage(Component.literal("§cNgười chơi này đã có môn phái."));
            return 0;
        }
        CompoundTag actorSect = sectRoot(actor);
        int currentMembers = onlineMembers(actor.server, sectName(actor)).size();
        if (currentMembers >= memberLimit(actorSect.getInt("Level"))) {
            actor.sendSystemMessage(Component.literal("§cMôn phái đã đạt giới hạn thành viên online của cấp hiện tại."));
            return 0;
        }
        INVITES.put(target.getUUID(), new SectInvite(actor.getUUID(), sectName(actor), actorSect.getString("Type"), actorSect.getInt("Level"), actor.level().getGameTime() + 1200L));
        actor.sendSystemMessage(Component.literal("§aĐã mời " + target.getName().getString() + " vào môn phái."));
        target.sendSystemMessage(Component.literal("§6Bạn được mời vào môn phái §e" + sectName(actor) + "§6. Dùng §a/monphai accept §6hoặc §c/monphai deny§6."));
        return 1;
    }

    private static int acceptInvite(ServerPlayer player) {
        if (hasSect(player)) {
            player.sendSystemMessage(Component.literal("§cBạn đã có môn phái."));
            return 0;
        }
        SectInvite invite = INVITES.get(player.getUUID());
        if (invite == null || invite.expireTick < player.level().getGameTime()) {
            INVITES.remove(player.getUUID());
            player.sendSystemMessage(Component.literal("§cKhông có lời mời môn phái hợp lệ."));
            return 0;
        }
        ServerPlayer owner = player.server.getPlayerList().getPlayer(invite.inviter);
        CompoundTag sect = sectRoot(player);
        sect.putString("Name", invite.name);
        sect.putString("Type", invite.type);
        sect.putString("Role", "Đệ Tử");
        sect.putInt("Level", invite.level);
        sect.putUUID("Owner", invite.inviter);
        if (owner != null && hasHome(sectRoot(owner))) copyHome(sectRoot(owner), sect);
        INVITES.remove(player.getUUID());
        broadcastSect(player.server, invite.name, Component.literal("§a" + player.getName().getString() + " đã gia nhập môn phái."));
        return 1;
    }

    private static int denyInvite(ServerPlayer player) {
        INVITES.remove(player.getUUID());
        player.sendSystemMessage(Component.literal("§7Đã từ chối lời mời môn phái."));
        return 1;
    }

    private static int leave(ServerPlayer player) {
        if (!hasSect(player)) {
            player.sendSystemMessage(Component.literal("§cBạn chưa có môn phái."));
            return 0;
        }
        String name = sectName(player);
        if (isLeader(player) && onlineMembers(player.server, name).size() > 1) {
            player.sendSystemMessage(Component.literal("§cTông Chủ cần /monphai transfer <player> trước khi rời môn phái."));
            return 0;
        }
        player.getPersistentData().remove(SECT);
        broadcastSect(player.server, name, Component.literal("§e" + player.getName().getString() + " đã rời môn phái."));
        player.sendSystemMessage(Component.literal("§7Bạn đã rời môn phái."));
        return 1;
    }

    private static int kick(ServerPlayer actor, ServerPlayer target) {
        if (!sameSect(actor, target)) {
            actor.sendSystemMessage(Component.literal("§cNgười chơi này không cùng môn phái hoặc chưa có môn phái."));
            return 0;
        }
        if (actor == target) {
            actor.sendSystemMessage(Component.literal("§cKhông thể tự đá chính mình."));
            return 0;
        }
        if (!canKick(actor, target)) {
            actor.sendSystemMessage(Component.literal("§cChức vụ của bạn không đủ quyền để đá người này."));
            return 0;
        }
        String name = sectName(actor);
        target.getPersistentData().remove(SECT);
        target.sendSystemMessage(Component.literal("§cBạn đã bị đá khỏi môn phái " + name + "."));
        broadcastSect(actor.server, name, Component.literal("§c" + target.getName().getString() + " đã bị đá khỏi môn phái."));
        return 1;
    }

    private static int disband(ServerPlayer player) {
        if (!hasSect(player)) {
            player.sendSystemMessage(Component.literal("§cBạn chưa có môn phái."));
            return 0;
        }
        if (!isLeader(player)) {
            player.sendSystemMessage(Component.literal("§cChỉ Tông Chủ mới có thể giải tán môn phái."));
            return 0;
        }
        String name = sectName(player);
        for (ServerPlayer member : onlineMembers(player.server, name)) {
            member.getPersistentData().remove(SECT);
            member.sendSystemMessage(Component.literal("§cMôn phái " + name + " đã giải tán."));
        }
        return 1;
    }

    private static int setHome(ServerPlayer player) {
        if (!hasSect(player)) {
            player.sendSystemMessage(Component.literal("§cBạn chưa có môn phái."));
            return 0;
        }
        if (!canSetHome(player)) {
            player.sendSystemMessage(Component.literal("§cChức vụ của bạn không có quyền đặt home môn phái."));
            return 0;
        }
        String name = sectName(player);
        for (ServerPlayer member : onlineMembers(player.server, name)) {
            writeHome(sectRoot(member), player.serverLevel(), player.blockPosition());
        }
        broadcastSect(player.server, name, Component.literal("§aHome môn phái đã được đặt tại vị trí của " + player.getName().getString() + "."));
        return 1;
    }

    private static int home(ServerPlayer player) {
        if (!hasSect(player)) {
            player.sendSystemMessage(Component.literal("§cBạn chưa có môn phái."));
            return 0;
        }
        CompoundTag sect = sectRoot(player);
        if (!hasHome(sect)) {
            player.sendSystemMessage(Component.literal("§cMôn phái chưa đặt home."));
            return 0;
        }
        ResourceLocation dimensionId = ResourceLocation.tryParse(sect.getString("HomeWorld"));
        if (dimensionId == null) {
            player.sendSystemMessage(Component.literal("§cHome môn phái bị lỗi world."));
            return 0;
        }
        ResourceKey<net.minecraft.world.level.Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel targetLevel = player.server.getLevel(dimensionKey);
        if (targetLevel == null) {
            player.sendSystemMessage(Component.literal("§cKhông tìm thấy world của home môn phái."));
            return 0;
        }
        double x = sect.getDouble("HomeX") + 0.5D;
        double y = sect.getDouble("HomeY");
        double z = sect.getDouble("HomeZ") + 0.5D;
        player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.literal("§aĐã dịch chuyển về home môn phái."));
        return 1;
    }

    private static int promote(ServerPlayer actor, ServerPlayer target) {
        if (!sameSect(actor, target)) {
            actor.sendSystemMessage(Component.literal("§cNgười chơi này không cùng môn phái."));
            return 0;
        }
        if (!isLeader(actor) && !isViceLeader(actor)) {
            actor.sendSystemMessage(Component.literal("§cBạn không có quyền thăng chức."));
            return 0;
        }
        String role = sectRoot(target).getString("Role");
        String next = switch (role) {
            case "Đệ Tử" -> "Trưởng Lão";
            case "Trưởng Lão" -> "Phó Tông Chủ";
            default -> role;
        };
        if (next.equals(role) || (!isLeader(actor) && next.equals("Phó Tông Chủ"))) {
            actor.sendSystemMessage(Component.literal("§cKhông thể thăng chức thêm."));
            return 0;
        }
        sectRoot(target).putString("Role", next);
        broadcastSect(actor.server, sectName(actor), Component.literal("§a" + target.getName().getString() + " được thăng thành " + next + "."));
        return 1;
    }

    private static int demote(ServerPlayer actor, ServerPlayer target) {
        if (!sameSect(actor, target)) {
            actor.sendSystemMessage(Component.literal("§cNgười chơi này không cùng môn phái."));
            return 0;
        }
        if (!isLeader(actor) && !isViceLeader(actor)) {
            actor.sendSystemMessage(Component.literal("§cBạn không có quyền hạ chức."));
            return 0;
        }
        if (roleRank(actor) <= roleRank(target)) {
            actor.sendSystemMessage(Component.literal("§cKhông thể hạ chức người ngang/cao quyền hơn."));
            return 0;
        }
        String role = sectRoot(target).getString("Role");
        String next = switch (role) {
            case "Phó Tông Chủ" -> "Trưởng Lão";
            case "Trưởng Lão" -> "Đệ Tử";
            default -> role;
        };
        if (next.equals(role)) {
            actor.sendSystemMessage(Component.literal("§cKhông thể hạ chức thêm."));
            return 0;
        }
        sectRoot(target).putString("Role", next);
        broadcastSect(actor.server, sectName(actor), Component.literal("§e" + target.getName().getString() + " bị hạ xuống " + next + "."));
        return 1;
    }

    private static int transfer(ServerPlayer actor, ServerPlayer target) {
        if (!sameSect(actor, target)) {
            actor.sendSystemMessage(Component.literal("§cNgười chơi này không cùng môn phái."));
            return 0;
        }
        if (!isLeader(actor)) {
            actor.sendSystemMessage(Component.literal("§cChỉ Tông Chủ mới chuyển chức Tông Chủ được."));
            return 0;
        }
        if (actor == target) {
            actor.sendSystemMessage(Component.literal("§cBạn đã là Tông Chủ."));
            return 0;
        }
        String name = sectName(actor);
        sectRoot(actor).putString("Role", "Phó Tông Chủ");
        sectRoot(target).putString("Role", "Tông Chủ");
        for (ServerPlayer member : onlineMembers(actor.server, name)) sectRoot(member).putUUID("Owner", target.getUUID());
        broadcastSect(actor.server, name, Component.literal("§6" + target.getName().getString() + " đã trở thành Tông Chủ mới."));
        return 1;
    }

    private static int sectChat(ServerPlayer player, String message) {
        if (!hasSect(player)) {
            player.sendSystemMessage(Component.literal("§cBạn chưa có môn phái."));
            return 0;
        }
        Component line = Component.literal("§8[§6Môn Phái§8] §e" + player.getName().getString() + "§7: §f" + message);
        broadcastSect(player.server, sectName(player), line);
        return 1;
    }

    private static boolean sectExistsOnline(MinecraftServer server, String name) {
        return !onlineMembers(server, name).isEmpty();
    }

    private static List<ServerPlayer> onlineMembers(MinecraftServer server, String name) {
        List<ServerPlayer> members = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (hasSect(p) && sectName(p).equals(name)) members.add(p);
        }
        return members;
    }

    private static void broadcastSect(MinecraftServer server, String name, Component message) {
        for (ServerPlayer member : onlineMembers(server, name)) member.sendSystemMessage(message);
    }

    private static boolean canInvite(ServerPlayer player) {
        return roleRank(player) >= 2;
    }

    private static boolean canKick(ServerPlayer actor, ServerPlayer target) {
        return roleRank(actor) > roleRank(target) && roleRank(actor) >= 3;
    }

    private static boolean canSetHome(ServerPlayer player) {
        return roleRank(player) >= 3;
    }

    private static boolean isLeader(ServerPlayer player) {
        return roleRank(player) >= 4;
    }

    private static boolean isViceLeader(ServerPlayer player) {
        return roleRank(player) == 3;
    }

    private static int roleRank(ServerPlayer player) {
        String role = sectRoot(player).getString("Role");
        return switch (role) {
            case "Tông Chủ" -> 4;
            case "Phó Tông Chủ" -> 3;
            case "Trưởng Lão" -> 2;
            default -> 1;
        };
    }

    private static int memberLimit(int level) {
        return switch (Math.max(1, Math.min(level, 5))) {
            case 1 -> 5;
            case 2 -> 10;
            case 3 -> 15;
            case 4 -> 20;
            default -> 30;
        };
    }

    private static boolean hasHome(CompoundTag sect) {
        return sect.contains("HomeWorld") && sect.contains("HomeX") && sect.contains("HomeY") && sect.contains("HomeZ");
    }

    private static void writeHome(CompoundTag sect, ServerLevel level, BlockPos pos) {
        sect.putString("HomeWorld", level.dimension().location().toString());
        sect.putDouble("HomeX", pos.getX());
        sect.putDouble("HomeY", pos.getY());
        sect.putDouble("HomeZ", pos.getZ());
    }

    private static void copyHome(CompoundTag from, CompoundTag to) {
        if (!hasHome(from)) return;
        to.putString("HomeWorld", from.getString("HomeWorld"));
        to.putDouble("HomeX", from.getDouble("HomeX"));
        to.putDouble("HomeY", from.getDouble("HomeY"));
        to.putDouble("HomeZ", from.getDouble("HomeZ"));
    }

    private static boolean skillAllowed(ServerPlayer player, String type) {
        CompoundTag sect = sectRoot(player);
        if (!sect.getString("Type").equals(type)) {
            player.sendSystemMessage(Component.literal("§cThần thông này không thuộc loại môn phái của bạn."));
            return false;
        }
        if (sect.getInt("Level") < 5) {
            player.sendSystemMessage(Component.literal("§cMôn phái cần đạt cấp 5 để dùng thần thông."));
            return false;
        }
        return true;
    }

    private static int vanThiToc(ServerPlayer player) {
        if (!skillAllowed(player, "luyenthi")) return 0;
        for (int i = 0; i < 3; i++) {
            Zombie z = EntityType.ZOMBIE.create(player.serverLevel());
            if (z == null) continue;
            z.moveTo(player.getX() + i - 1, player.getY(), player.getZ() + 2, 0, 0);
            z.setCustomName(Component.literal("§2Vạn Thi Tộc"));
            z.setCustomNameVisible(true);
            z.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
            z.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20.0D);
            z.setHealth(100.0F);
            player.serverLevel().addFreshEntity(z);
        }
        player.sendSystemMessage(Component.literal("§aĐã triệu hồi Vạn Thi Tộc."));
        return 1;
    }

    private static int thonHonDaiTran(ServerPlayer player) {
        if (!skillAllowed(player, "luyenhon")) return 0;
        WitherBoss w = EntityType.WITHER.create(player.serverLevel());
        if (w != null) {
            w.moveTo(player.getX(), player.getY(), player.getZ() + 4, 0, 0);
            w.setCustomName(Component.literal("§5Thôn Hồn"));
            w.setCustomNameVisible(true);
            w.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300.0D);
            w.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(30.0D);
            w.setHealth(300.0F);
            player.serverLevel().addFreshEntity(w);
        }
        return 1;
    }

    private static int cuongHuyet(ServerPlayer player) {
        if (!skillAllowed(player, "luyenthe")) return 0;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 2));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 2));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 1));
        return 1;
    }

    private static int kiemKhiHoaHinh(ServerPlayer player) {
        if (!skillAllowed(player, "luyenkiem")) return 0;
        CultivationData.root(player).putBoolean("KiemKhiHoaHinhReady", true);
        player.sendSystemMessage(Component.literal("§bĐòn đánh tiếp theo sẽ được cường hóa 200%."));
        return 1;
    }

    private static int hoaLienHoa(ServerPlayer player) {
        if (!skillAllowed(player, "tudao")) return 0;
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : player.serverLevel().getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(32.0D), e -> e.isAlive() && e != player)) {
            double d = e.distanceToSqr(player);
            if (d < best) { best = d; target = e; }
        }
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cKhông tìm thấy mục tiêu trong 32 block."));
            return 0;
        }
        float total = 0.0F;
        for (int i = 0; i < 5; i++) {
            total += Math.max(1.0F, target.getHealth() * 0.05F);
        }
        target.setSecondsOnFire(5);
        target.hurt(player.damageSources().magic(), total);
        player.sendSystemMessage(Component.literal("§cHỏa Liên Hoa thiêu đốt mục tiêu 5 giây."));
        return 1;
    }

    private static class SectInvite {
        private final UUID inviter;
        private final String name;
        private final String type;
        private final int level;
        private final long expireTick;

        private SectInvite(UUID inviter, String name, String type, int level, long expireTick) {
            this.inviter = inviter;
            this.name = name;
            this.type = type;
            this.level = Math.max(1, level);
            this.expireTick = expireTick;
        }
    }
}
