package com.example.examplemod.pet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.UUID;

public class CapturedPetUtil {
    public static final String TAG_ROOT = "MysticCapturedPet";
    public static final String TAG_OWNER = "Owner";
    public static final String TAG_PET_ID = "PetId";
    public static final String TAG_RARITY = "Rarity";
    public static final String TAG_LEVEL = "Level";

    public static boolean canCapture(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        if (entity instanceof Player) return false;
        EntityType<?> type = entity.getType();
        if (type == EntityType.ENDER_DRAGON || type == EntityType.WITHER || type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER) {
            return false;
        }
        return !isMysticPet(entity);
    }

    public static void markAsPet(Entity entity, ServerPlayer owner, PetData data) {
        CompoundTag root = entity.getPersistentData().getCompound(TAG_ROOT);
        root.putUUID(TAG_OWNER, owner.getUUID());
        root.putUUID(TAG_PET_ID, data.getPetId());
        root.putString(TAG_RARITY, data.getRarity().getId());
        root.putInt(TAG_LEVEL, data.getLevel());
        entity.getPersistentData().put(TAG_ROOT, root);
    }

    public static void neutralizeForOwner(Entity entity, ServerPlayer owner) {
        if (!(entity instanceof LivingEntity living)) return;
        living.setLastHurtByMob(null);
        living.setLastHurtMob(null);
        if (entity instanceof Mob mob) {
            if (mob.getTarget() != null && mob.getTarget().getUUID().equals(owner.getUUID())) {
                mob.setTarget(null);
            }
        }
        clearAngerByReflection(entity, owner);
    }

    private static void clearAngerByReflection(Entity entity, ServerPlayer owner) {
        try {
            for (java.lang.reflect.Method method : entity.getClass().getMethods()) {
                if ((method.getName().equals("clearAnger") || method.getName().equals("clearAngerAt"))
                        && method.getParameterCount() == 1
                        && Entity.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    method.invoke(entity, owner);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static boolean isMysticPet(Entity entity) {
        return entity.getPersistentData().contains(TAG_ROOT, 10)
                && entity.getPersistentData().getCompound(TAG_ROOT).hasUUID(TAG_OWNER)
                && entity.getPersistentData().getCompound(TAG_ROOT).hasUUID(TAG_PET_ID);
    }

    public static Optional<UUID> getOwner(Entity entity) {
        if (!isMysticPet(entity)) return Optional.empty();
        return Optional.of(entity.getPersistentData().getCompound(TAG_ROOT).getUUID(TAG_OWNER));
    }

    public static Optional<UUID> getPetId(Entity entity) {
        if (!isMysticPet(entity)) return Optional.empty();
        return Optional.of(entity.getPersistentData().getCompound(TAG_ROOT).getUUID(TAG_PET_ID));
    }

    public static String entityTypeId(Entity entity) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key == null ? EntityType.getKey(entity.getType()).toString() : key.toString();
    }

    public static CompoundTag snapshotEntity(Entity entity) {
        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        tag.remove("UUID");
        tag.remove("UUIDMost");
        tag.remove("UUIDLeast");
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Rotation");
        tag.remove("Leash");
        tag.remove("Passengers");
        return tag;
    }
}
