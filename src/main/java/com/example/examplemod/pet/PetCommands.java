package com.example.examplemod.pet;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rename.ColorTextUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber
public class PetCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("callpet")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, PetStorage.SLOT_COUNT))
                                .executes(context -> callPet(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "slot"))))
        );

        event.getDispatcher().register(
                Commands.literal("petstore")
                        .executes(context -> storeActivePet(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("renamepet")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> renameActivePet(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name").replace("\n", "").replace("\r", ""))))
        );

        event.getDispatcher().register(
                Commands.literal("deletepet")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, PetStorage.SLOT_COUNT))
                                .executes(context -> deletePet(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "slot"))))
        );

        event.getDispatcher().register(
                Commands.literal("petdelete")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, PetStorage.SLOT_COUNT))
                                .executes(context -> deletePet(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "slot"))))
        );

        event.getDispatcher().register(
                Commands.literal("petinfo")
                        .executes(context -> petInfo(context.getSource().getPlayerOrException()))
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, PetStorage.SLOT_COUNT))
                                .executes(context -> petInfo(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "slot"))))
        );

        event.getDispatcher().register(
                Commands.literal("petmode")
                        .then(Commands.literal("passive")
                                .executes(context -> setPetMode(context.getSource().getPlayerOrException(), PetAIController.MODE_PASSIVE)))
                        .then(Commands.literal("assist")
                                .executes(context -> setPetMode(context.getSource().getPlayerOrException(), PetAIController.MODE_ASSIST)))
                        .then(Commands.literal("aggressive")
                                .executes(context -> setPetMode(context.getSource().getPlayerOrException(), PetAIController.MODE_AGGRESSIVE)))
                        .then(Commands.literal("stay")
                                .executes(context -> setPetMode(context.getSource().getPlayerOrException(), PetAIController.MODE_STAY)))
        );

        event.getDispatcher().register(
                Commands.literal("petstay")
                        .executes(context -> petStay(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("petfollow")
                        .executes(context -> setPetMode(context.getSource().getPlayerOrException(), PetAIController.MODE_ASSIST))
        );

        event.getDispatcher().register(
                Commands.literal("petcome")
                        .executes(context -> petCome(context.getSource().getPlayerOrException()))
        );

        event.getDispatcher().register(
                Commands.literal("petattack")
                        .executes(context -> petAttack(context.getSource().getPlayerOrException()))
        );
    }

    private static int callPet(ServerPlayer player, int slot) {
        Optional<PetData> optional = PetStorage.get(player, slot);
        if (optional.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cSlot " + slot + " chưa có pet."));
            return 0;
        }
        PetData data = optional.get();
        long gameTime = player.serverLevel().getGameTime();
        if (data.isDeadCooldown(gameTime)) {
            player.sendSystemMessage(Component.literal("§cPet đang hồi sinh. Còn " + data.getCooldownSecondsLeft(gameTime) + " giây."));
            return 0;
        }
        if (!data.hasCapturedEntity()) {
            player.sendSystemMessage(Component.literal("§cPet slot này chưa có dữ liệu sinh vật."));
            return 0;
        }
        Entity oldActive = findEntity(player.serverLevel(), data.getActiveEntityId());
        if (oldActive != null && oldActive.isAlive()) {
            player.sendSystemMessage(Component.literal("§ePet slot " + slot + " đang ở ngoài rồi."));
            return 0;
        }

        Entity entity = createCapturedEntity(player.serverLevel(), data);
        if (entity == null) {
            player.sendSystemMessage(Component.literal("§cKhông thể tạo lại pet loại: " + data.getEntityTypeId()));
            return 0;
        }

        entity.moveTo(player.getX() + 1.0D, player.getY(), player.getZ() + 1.0D, player.getYRot(), 0.0F);
        CapturedPetUtil.markAsPet(entity, player, data);
        data.getCustomName().ifPresent(name -> {
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        });
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.setTarget(null);
        }
        CapturedPetUtil.neutralizeForOwner(entity, player);
        player.serverLevel().addFreshEntity(entity);
        data.setActiveEntityId(entity.getUUID());
        data.setDeadUntilGameTime(0L);
        PetStorage.set(player, slot, data);
        player.sendSystemMessage(Component.literal("§aĐã gọi pet từ slot " + slot + "."));
        return 1;
    }

    private static Entity createCapturedEntity(ServerLevel level, PetData data) {
        CompoundTag tag = data.getEntityNbt();
        tag.putString("id", data.getEntityTypeId());
        return EntityType.create(tag, level).orElse(null);
    }

    private static int storeActivePet(ServerPlayer player) {
        Optional<Integer> activeSlot = PetStorage.findActiveSlot(player);
        if (activeSlot.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cBạn không có pet nào đang được gọi ra."));
            return 0;
        }
        int slot = activeSlot.get();
        Optional<PetData> optional = PetStorage.get(player, slot);
        if (optional.isEmpty()) return 0;
        PetData data = optional.get();
        Entity entity = findEntity(player.serverLevel(), data.getActiveEntityId());
        if (entity != null && entity.isAlive()) {
            data.setCapturedEntity(CapturedPetUtil.entityTypeId(entity), CapturedPetUtil.snapshotEntity(entity));
            entity.discard();
        }
        data.setActiveEntityId(null);
        PetStorage.set(player, slot, data);
        player.sendSystemMessage(Component.literal("§aĐã cất pet slot " + slot + " vào kho."));
        return 1;
    }

    private static int renameActivePet(ServerPlayer player, String name) {
        if (name.length() > 64) {
            player.sendSystemMessage(Component.literal("§cTên quá dài! Tối đa 64 ký tự."));
            return 0;
        }
        Optional<Integer> activeSlot = PetStorage.findActiveSlot(player);
        if (activeSlot.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cBạn cần gọi pet ra trước khi đổi tên."));
            return 0;
        }
        int slot = activeSlot.get();
        Optional<PetData> optional = PetStorage.get(player, slot);
        if (optional.isEmpty()) return 0;
        PetData data = optional.get();
        if (!player.isCreative() && !consumeRenameScroll(player)) {
            player.sendSystemMessage(Component.literal("§cBạn cần có Thẻ Đổi Tên để đổi tên pet!"));
            return 0;
        }
        Component parsed = ColorTextUtil.parse(name);
        data.setCustomName(parsed);
        Entity entity = findEntity(player.serverLevel(), data.getActiveEntityId());
        if (entity != null) {
            entity.setCustomName(parsed);
            entity.setCustomNameVisible(true);
            data.setCapturedEntity(CapturedPetUtil.entityTypeId(entity), CapturedPetUtil.snapshotEntity(entity));
        }
        PetStorage.set(player, slot, data);
        player.sendSystemMessage(Component.literal("§aĐã đổi tên pet."));
        return 1;
    }

    private static int deletePet(ServerPlayer player, int slot) {
        Optional<PetData> optional = PetStorage.get(player, slot);
        if (optional.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cSlot " + slot + " chưa có pet."));
            return 0;
        }
        PetData data = optional.get();
        Entity entity = findEntity(player.serverLevel(), data.getActiveEntityId());
        if (entity != null) {
            entity.discard();
        }
        PetStorage.clear(player, slot);
        player.sendSystemMessage(Component.literal("§aĐã xóa pet ở slot " + slot + "."));
        return 1;
    }


    private static int petInfo(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§6===== PET STORAGE ====="));
        for (int slot = 1; slot <= PetStorage.SLOT_COUNT; slot++) {
            sendPetInfoLine(player, slot, PetStorage.get(player, slot).orElse(null), false);
        }
        return 1;
    }

    private static int petInfo(ServerPlayer player, int slot) {
        Optional<PetData> optional = PetStorage.get(player, slot);
        if (optional.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cSlot " + slot + " chưa có pet."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("§6===== PET INFO SLOT " + slot + " ====="));
        sendPetInfoLine(player, slot, optional.get(), true);
        return 1;
    }

    private static void sendPetInfoLine(ServerPlayer player, int slot, PetData data, boolean detail) {
        if (data == null) {
            player.sendSystemMessage(Component.literal("§7Slot " + slot + ": Trống"));
            return;
        }
        long gameTime = player.serverLevel().getGameTime();
        Entity active = findEntity(player.serverLevel(), data.getActiveEntityId());
        boolean isActive = active != null && active.isAlive();
        String status = data.isDeadCooldown(gameTime) ? "§cHồi sinh còn " + data.getCooldownSecondsLeft(gameTime) + "s" : (isActive ? "§aĐang gọi ra" : "§eĐang cất");
        String mode = isActive ? PetAIController.getMode(active) : "stored";
        String name = data.getCustomName().map(Component::getString).orElse(data.getEntityTypeId());
        int nextExp = data.isMaxLevel() ? 0 : data.getRarity().getExpToNextLevel(data.getLevel());
        player.sendSystemMessage(Component.literal("§eSlot " + slot + ": §f" + name + " §7[" + data.getEntityTypeId() + "]"));
        player.sendSystemMessage(Component.literal("  §7Rarity: §f" + data.getRarity().name() + " §7Level: §f" + data.getLevel() + "/" + data.getRarity().getMaxLevel() + " §7EXP: §f" + data.getExp() + (nextExp > 0 ? "/" + nextExp : " MAX")));
        player.sendSystemMessage(Component.literal("  §7Trạng thái: " + status + " §7Mode: §f" + mode));
        if (detail) {
            Entity sample = isActive ? active : createCapturedEntity(player.serverLevel(), data);
            if (sample instanceof LivingEntity living) {
                String hp = format(living.getMaxHealth());
                String dmg = format(living.getAttributeValue(Attributes.ATTACK_DAMAGE));
                String armor = format(living.getAttributeValue(Attributes.ARMOR));
                String speed = format(living.getAttributeValue(Attributes.MOVEMENT_SPEED));
                player.sendSystemMessage(Component.literal("  §7Chỉ số gốc: §cHP " + hp + " §6ATK " + dmg + " §bArmor " + armor + " §aSpeed " + speed));
            }
        }
    }

    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001D) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }


    private static int setPetMode(ServerPlayer player, String mode) {
        Entity pet = getActivePetEntity(player);
        if (pet == null) {
            player.sendSystemMessage(Component.literal("§cBạn cần gọi pet ra trước."));
            return 0;
        }
        PetAIController.setMode(pet, mode);
        if (pet instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        player.sendSystemMessage(Component.literal("§aĐã đổi chế độ pet thành: §f" + mode));
        return 1;
    }

    private static int petStay(ServerPlayer player) {
        Entity pet = getActivePetEntity(player);
        if (pet == null) {
            player.sendSystemMessage(Component.literal("§cBạn cần gọi pet ra trước."));
            return 0;
        }
        PetAIController.setStayHere(pet);
        if (pet instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        player.sendSystemMessage(Component.literal("§aPet sẽ đứng yên tại vị trí hiện tại."));
        return 1;
    }

    private static int petCome(ServerPlayer player) {
        Entity pet = getActivePetEntity(player);
        if (pet == null) {
            player.sendSystemMessage(Component.literal("§cBạn cần gọi pet ra trước."));
            return 0;
        }
        pet.teleportTo(player.getX() + 1.0D, player.getY(), player.getZ() + 1.0D);
        PetAIController.setMode(pet, PetAIController.MODE_ASSIST);
        if (pet instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        player.sendSystemMessage(Component.literal("§aĐã gọi pet về cạnh bạn."));
        return 1;
    }

    private static int petAttack(ServerPlayer player) {
        Entity pet = getActivePetEntity(player);
        if (pet == null) {
            player.sendSystemMessage(Component.literal("§cBạn cần gọi pet ra trước."));
            return 0;
        }
        LivingEntity target = findLookedTarget(player, 48.0D);
        if (target == null || !PetAIController.isValidEnemy(player, target)) {
            player.sendSystemMessage(Component.literal("§cKhông tìm thấy mục tiêu hợp lệ trước mặt bạn."));
            return 0;
        }
        PetAIController.setMode(pet, PetAIController.MODE_ASSIST);
        PetAIController.setForcedTarget(pet, target);
        if (pet instanceof Mob mob) {
            mob.setTarget(target);
            mob.getNavigation().moveTo(target, 1.35D);
        }
        player.sendSystemMessage(Component.literal("§aĐã ra lệnh pet tấn công: §f" + target.getDisplayName().getString()));
        return 1;
    }

    private static Entity getActivePetEntity(ServerPlayer player) {
        Optional<Integer> activeSlot = PetStorage.findActiveSlot(player);
        if (activeSlot.isEmpty()) return null;
        Optional<PetData> optional = PetStorage.get(player, activeSlot.get());
        if (optional.isEmpty()) return null;
        Entity entity = findEntity(player.serverLevel(), optional.get().getActiveEntityId());
        if (entity != null && entity.isAlive() && CapturedPetUtil.isMysticPet(entity)) {
            return entity;
        }
        return null;
    }

    private static LivingEntity findLookedTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0D);
        LivingEntity best = null;
        double bestScore = 0.96D;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity != player && entity.isAlive())) {
            Vec3 toEntity = entity.getEyePosition().subtract(eye);
            double distance = toEntity.length();
            if (distance <= 0.001D || distance > range) continue;
            double score = look.dot(toEntity.normalize());
            if (score > bestScore && player.hasLineOfSight(entity)) {
                bestScore = score;
                best = entity;
            }
        }
        return best;
    }

    private static boolean consumeRenameScroll(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ExampleMod.RENAME_SCROLL.get())) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static Entity findEntity(ServerLevel level, UUID uuid) {
        if (uuid == null) return null;
        return level.getEntity(uuid);
    }
}
