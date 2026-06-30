package com.example.examplemod.pet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public class PetStorage {
    public static final int SLOT_COUNT = 3;
    private static final String ROOT = "MysticPetStorage";

    public static Optional<PetData> get(ServerPlayer player, int slot) {
        if (!isValidSlot(slot)) return Optional.empty();
        CompoundTag root = getRoot(player);
        String key = key(slot);
        if (!root.contains(key, 10)) return Optional.empty();
        return Optional.of(PetData.load(root.getCompound(key)));
    }

    public static void set(ServerPlayer player, int slot, PetData data) {
        if (!isValidSlot(slot)) return;
        CompoundTag root = getRoot(player);
        root.put(key(slot), data.save());
    }

    public static void clear(ServerPlayer player, int slot) {
        if (!isValidSlot(slot)) return;
        getRoot(player).remove(key(slot));
    }

    public static int firstEmptySlot(ServerPlayer player) {
        for (int slot = 1; slot <= SLOT_COUNT; slot++) {
            if (get(player, slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    public static Optional<Integer> findByPetId(ServerPlayer player, UUID petId) {
        for (int slot = 1; slot <= SLOT_COUNT; slot++) {
            Optional<PetData> data = get(player, slot);
            if (data.isPresent() && data.get().getPetId().equals(petId)) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    public static Optional<Integer> findActiveSlot(ServerPlayer player) {
        for (int slot = 1; slot <= SLOT_COUNT; slot++) {
            Optional<PetData> data = get(player, slot);
            if (data.isPresent() && data.get().isActive()) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    public static void clearActive(ServerPlayer player, UUID entityId) {
        for (int slot = 1; slot <= SLOT_COUNT; slot++) {
            Optional<PetData> optional = get(player, slot);
            if (optional.isPresent()) {
                PetData data = optional.get();
                if (entityId.equals(data.getActiveEntityId())) {
                    data.setActiveEntityId(null);
                    set(player, slot, data);
                }
            }
        }
    }

    private static CompoundTag getRoot(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, 10)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    private static String key(int slot) {
        return "Slot" + slot;
    }

    public static boolean isValidSlot(int slot) {
        return slot >= 1 && slot <= SLOT_COUNT;
    }
}
