package com.example.examplemod.appearance;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class AppearanceCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("copyappearance")
                        .executes(context -> copy(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("changeappearance")
                        .executes(context -> applyCopied(context.getSource().getPlayerOrException()))
                        .then(slotLiteral("helmet", ArmorItem.Type.HELMET))
                        .then(slotLiteral("chestplate", ArmorItem.Type.CHESTPLATE))
                        .then(slotLiteral("leggings", ArmorItem.Type.LEGGINGS))
                        .then(slotLiteral("boots", ArmorItem.Type.BOOTS))
        );

        event.getDispatcher().register(
                Commands.literal("resetappearance")
                        .then(resetSlotLiteral("helmet", ArmorItem.Type.HELMET))
                        .then(resetSlotLiteral("chestplate", ArmorItem.Type.CHESTPLATE))
                        .then(resetSlotLiteral("leggings", ArmorItem.Type.LEGGINGS))
                        .then(resetSlotLiteral("boots", ArmorItem.Type.BOOTS))
                        .then(Commands.literal("all")
                                .executes(context -> resetAll(context.getSource().getPlayerOrException())))
        );

        event.getDispatcher().register(
                Commands.literal("copyweaponappearance")
                        .executes(context -> copyWeapon(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("changeweaponappearance")
                        .executes(context -> changeWeapon(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("resetweaponappearance")
                        .executes(context -> resetWeapon(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("weaponappearanceinfo")
                        .executes(context -> weaponInfo(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("appearanceinfo")
                        .executes(context -> info(context.getSource().getPlayerOrException()))
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> slotLiteral(String name, ArmorItem.Type type) {
        return Commands.literal(name)
                .then(Commands.argument("item_id", StringArgumentType.greedyString())
                        .executes(context -> change(
                                context.getSource().getPlayerOrException(),
                                type,
                                StringArgumentType.getString(context, "item_id")
                        )));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> resetSlotLiteral(String name, ArmorItem.Type type) {
        return Commands.literal(name)
                .executes(context -> reset(context.getSource().getPlayerOrException(), type));
    }

    private static int copy(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || !(held.getItem() instanceof ArmorItem armorItem)) {
            player.sendSystemMessage(Component.literal("§cHãy cầm một món giáp trên tay chính rồi dùng /copyappearance."));
            return 0;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(held.getItem());
        if (id == null) {
            player.sendSystemMessage(Component.literal("§cKhông lấy được item id của giáp đang cầm."));
            return 0;
        }

        AppearanceManager.copyAppearance(player, id, armorItem.getType(), AppearanceManager.getTrim(held));
        String status = AppearanceRegistry.getStatus(id, armorItem);
        player.sendSystemMessage(Component.literal("§aĐã copy ngoại hình §e" + id + " §7(" + armorItem.getType().getName() + ")."));
        player.sendSystemMessage(Component.literal("§7Kiểu hỗ trợ: " + status));
        if (AppearanceManager.getTrim(held) != null) {
            player.sendSystemMessage(Component.literal("§aĐã copy cả armor trim của món này."));
        }
        player.sendSystemMessage(Component.literal("§7Mặc đủ set Mystic rồi dùng §e/changeappearance §7để áp dụng."));
        return 1;
    }

    private static int applyCopied(Player player) {
        ResourceLocation copiedId = AppearanceManager.getCopiedAppearanceId(player);
        ArmorItem.Type copiedType = AppearanceManager.getCopiedAppearanceType(player);
        if (copiedId == null || copiedType == null) {
            player.sendSystemMessage(Component.literal("§cChưa copy ngoại hình nào. Cầm giáp rồi dùng /copyappearance trước."));
            return 0;
        }
        return change(player, copiedType, copiedId.toString());
    }

    private static int change(Player player, ArmorItem.Type targetType, String rawId) {
        if (!AppearanceManager.hasFullMysticSet(player)) {
            player.sendSystemMessage(Component.literal("§cBạn cần mặc đủ 4 món cùng một set Mystic để đổi ngoại hình."));
            return 0;
        }

        ItemStack targetStack = AppearanceManager.getEquippedArmor(player, targetType);
        if (!AppearanceManager.isMysticArmorOfType(targetStack, targetType)) {
            player.sendSystemMessage(Component.literal("§cSlot này không phải giáp Mystic hợp lệ."));
            return 0;
        }

        ResourceLocation id = ResourceLocation.tryParse(rawId);
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) {
            player.sendSystemMessage(Component.literal("§cItem id không tồn tại: " + rawId));
            return 0;
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (!(item instanceof ArmorItem armorItem)) {
            player.sendSystemMessage(Component.literal("§cItem này không phải giáp: " + rawId));
            return 0;
        }

        if (armorItem.getType() != targetType) {
            player.sendSystemMessage(Component.literal("§cKhông cùng loại giáp. Slot đang đổi cần " + targetType.getName() + "."));
            return 0;
        }

        AppearanceManager.setAppearance(targetStack, id, AppearanceManager.getCopiedTrim(player));
        player.sendSystemMessage(Component.literal("§aĐã đổi ngoại hình " + targetType.getName() + " thành §e" + id));
        if (AppearanceManager.getCopiedTrim(player) != null) {
            player.sendSystemMessage(Component.literal("§aĐã áp dụng armor trim đã copy."));
        }
        player.sendSystemMessage(Component.literal("§7Kiểu hỗ trợ: " + AppearanceRegistry.getStatus(id, armorItem)));
        if (AppearanceRegistry.getArmorTexture(targetStack, id, armorItem, AppearanceManager.slotForType(targetType), null) == null) {
            player.sendSystemMessage(Component.literal("§eLưu ý: giáp này có thể dùng model/renderer riêng, cần adapter riêng để hiện đủ model 3D."));
        }
        return 1;
    }

    private static int reset(Player player, ArmorItem.Type targetType) {
        if (!AppearanceManager.hasFullMysticSet(player)) {
            player.sendSystemMessage(Component.literal("§cBạn cần mặc đủ 4 món cùng một set Mystic để xóa ngoại hình."));
            return 0;
        }

        ItemStack targetStack = AppearanceManager.getEquippedArmor(player, targetType);
        if (!AppearanceManager.isMysticArmorOfType(targetStack, targetType)) {
            player.sendSystemMessage(Component.literal("§cSlot này không phải giáp Mystic hợp lệ."));
            return 0;
        }

        AppearanceManager.clearAppearance(targetStack);
        player.sendSystemMessage(Component.literal("§aĐã reset ngoại hình " + targetType.getName() + "."));
        return 1;
    }

    private static int resetAll(Player player) {
        if (!AppearanceManager.hasFullMysticSet(player)) {
            player.sendSystemMessage(Component.literal("§cBạn cần mặc đủ 4 món cùng một set Mystic để xóa ngoại hình."));
            return 0;
        }

        resetSilently(player, ArmorItem.Type.HELMET);
        resetSilently(player, ArmorItem.Type.CHESTPLATE);
        resetSilently(player, ArmorItem.Type.LEGGINGS);
        resetSilently(player, ArmorItem.Type.BOOTS);
        player.sendSystemMessage(Component.literal("§aĐã reset ngoại hình toàn bộ set."));
        return 1;
    }

    private static void resetSilently(Player player, ArmorItem.Type type) {
        ItemStack stack = AppearanceManager.getEquippedArmor(player, type);
        if (AppearanceManager.isMysticArmorOfType(stack, type)) {
            AppearanceManager.clearAppearance(stack);
        }
    }

    private static int info(Player player) {
        player.sendSystemMessage(Component.literal("§6=== Mystic Armor Appearance ==="));
        ResourceLocation copied = AppearanceManager.getCopiedAppearanceId(player);
        ArmorItem.Type copiedType = AppearanceManager.getCopiedAppearanceType(player);
        player.sendSystemMessage(Component.literal("§dĐã copy: " + (copied == null || copiedType == null ? "§7chưa có" : "§e" + copied + " §7(" + copiedType.getName() + ")")));
        sendSlotInfo(player, "Helmet", ArmorItem.Type.HELMET);
        sendSlotInfo(player, "Chestplate", ArmorItem.Type.CHESTPLATE);
        sendSlotInfo(player, "Leggings", ArmorItem.Type.LEGGINGS);
        sendSlotInfo(player, "Boots", ArmorItem.Type.BOOTS);
        return 1;
    }

    private static void sendSlotInfo(Player player, String name, ArmorItem.Type type) {
        ItemStack stack = AppearanceManager.getEquippedArmor(player, type);
        ResourceLocation appearance = AppearanceManager.getAppearanceId(stack);
        String value = appearance == null ? "§7mặc định" : "§e" + appearance + " §7[" + AppearanceManager.getAppearanceStatus(stack) + "]";
        player.sendSystemMessage(Component.literal("§b" + name + ": " + value));
    }


    private static int copyWeapon(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || !WeaponAppearanceManager.isWeapon(held)) {
            player.sendSystemMessage(Component.literal("§cHãy cầm kiếm hoặc rìu trên tay chính rồi dùng /copyweaponappearance."));
            return 0;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(held.getItem());
        String type = WeaponAppearanceManager.getWeaponType(held);
        if (id == null || type == null) {
            player.sendSystemMessage(Component.literal("§cKhông lấy được item id hoặc loại vũ khí."));
            return 0;
        }

        WeaponAppearanceManager.copyWeaponAppearance(player, id, type);
        player.sendSystemMessage(Component.literal("§aĐã copy ngoại hình vũ khí §e" + id + " §7(" + type + ")."));
        player.sendSystemMessage(Component.literal("§7Cầm kiếm/rìu Mystic cùng loại rồi dùng §e/changeweaponappearance §7để áp dụng."));
        return 1;
    }

    private static int changeWeapon(Player player) {
        ResourceLocation copiedId = WeaponAppearanceManager.getCopiedWeaponAppearanceId(player);
        String copiedType = WeaponAppearanceManager.getCopiedWeaponType(player);
        if (copiedId == null || copiedType == null) {
            player.sendSystemMessage(Component.literal("§cChưa copy vũ khí nào. Cầm kiếm/rìu rồi dùng /copyweaponappearance trước."));
            return 0;
        }

        ItemStack target = player.getMainHandItem();
        if (target.isEmpty() || !WeaponAppearanceManager.isMysticWeapon(target)) {
            player.sendSystemMessage(Component.literal("§cHãy cầm kiếm hoặc rìu Mystic trên tay chính để đổi ngoại hình."));
            return 0;
        }

        String targetType = WeaponAppearanceManager.getWeaponType(target);
        if (!copiedType.equals(targetType)) {
            player.sendSystemMessage(Component.literal("§cKhông cùng loại vũ khí. Đã copy " + copiedType + " nhưng đang cầm " + targetType + "."));
            return 0;
        }

        Item copiedItem = ForgeRegistries.ITEMS.getValue(copiedId);
        ItemStack copiedStack = copiedItem == null ? ItemStack.EMPTY : new ItemStack(copiedItem);
        if (copiedItem == null || !WeaponAppearanceManager.isWeapon(copiedStack)) {
            player.sendSystemMessage(Component.literal("§cVũ khí đã copy không còn tồn tại hoặc không hợp lệ: " + copiedId));
            return 0;
        }

        WeaponAppearanceManager.setWeaponAppearance(target, copiedId);
        player.sendSystemMessage(Component.literal("§aĐã đổi ngoại hình vũ khí thành §e" + copiedId));
        return 1;
    }

    private static int resetWeapon(Player player) {
        ItemStack target = player.getMainHandItem();
        if (target.isEmpty() || !WeaponAppearanceManager.isMysticWeapon(target)) {
            player.sendSystemMessage(Component.literal("§cHãy cầm kiếm hoặc rìu Mystic cần reset."));
            return 0;
        }
        WeaponAppearanceManager.clearWeaponAppearance(target);
        player.sendSystemMessage(Component.literal("§aĐã reset ngoại hình vũ khí."));
        return 1;
    }

    private static int weaponInfo(Player player) {
        ResourceLocation copied = WeaponAppearanceManager.getCopiedWeaponAppearanceId(player);
        String copiedType = WeaponAppearanceManager.getCopiedWeaponType(player);
        player.sendSystemMessage(Component.literal("§6=== Mystic Weapon Appearance ==="));
        player.sendSystemMessage(Component.literal("§dĐã copy: " + (copied == null || copiedType == null ? "§7chưa có" : "§e" + copied + " §7(" + copiedType + ")")));

        ItemStack held = player.getMainHandItem();
        ResourceLocation appearance = WeaponAppearanceManager.getWeaponAppearanceId(held);
        player.sendSystemMessage(Component.literal("§bVũ khí đang cầm: " + (appearance == null ? "§7mặc định" : "§e" + appearance)));
        return 1;
    }

}
