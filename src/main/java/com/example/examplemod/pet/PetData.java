package com.example.examplemod.pet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.UUID;

public class PetData {
    public static final int RESPAWN_COOLDOWN_SECONDS = 120;

    private final UUID petId;
    private PetRarity rarity;
    private int level;
    private int exp;
    private String customNameJson;
    private long deadUntilGameTime;
    private UUID activeEntityId;
    private String entityTypeId;
    private CompoundTag entityNbt;

    public PetData(PetRarity rarity) {
        this(UUID.randomUUID(), rarity, 1, 0, "", 0L, null, "", new CompoundTag());
    }

    public PetData(UUID petId, PetRarity rarity, int level, int exp, String customNameJson, long deadUntilGameTime, UUID activeEntityId, String entityTypeId, CompoundTag entityNbt) {
        this.petId = petId;
        this.rarity = rarity;
        this.level = Math.max(1, Math.min(level, rarity.getMaxLevel()));
        this.exp = Math.max(0, exp);
        this.customNameJson = customNameJson == null ? "" : customNameJson;
        this.deadUntilGameTime = Math.max(0L, deadUntilGameTime);
        this.activeEntityId = activeEntityId;
        this.entityTypeId = entityTypeId == null ? "" : entityTypeId;
        this.entityNbt = entityNbt == null ? new CompoundTag() : entityNbt.copy();
    }


    public String getEntityTypeId() {
        return entityTypeId;
    }

    public CompoundTag getEntityNbt() {
        return entityNbt.copy();
    }

    public boolean hasCapturedEntity() {
        return entityTypeId != null && !entityTypeId.isBlank();
    }

    public void setCapturedEntity(String entityTypeId, CompoundTag entityNbt) {
        this.entityTypeId = entityTypeId == null ? "" : entityTypeId;
        this.entityNbt = entityNbt == null ? new CompoundTag() : entityNbt.copy();
    }

    public UUID getPetId() {
        return petId;
    }

    public PetRarity getRarity() {
        return rarity;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public boolean isMaxLevel() {
        return level >= rarity.getMaxLevel();
    }

    public void addExp(int amount) {
        if (amount <= 0 || isMaxLevel()) return;
        exp += amount;
        while (!isMaxLevel() && exp >= rarity.getExpToNextLevel(level)) {
            exp -= rarity.getExpToNextLevel(level);
            level++;
        }
        if (isMaxLevel()) {
            exp = 0;
        }
    }

    public Optional<Component> getCustomName() {
        if (customNameJson == null || customNameJson.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(Component.Serializer.fromJson(customNameJson));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public void setCustomName(Component name) {
        this.customNameJson = Component.Serializer.toJson(name);
    }

    public long getDeadUntilGameTime() {
        return deadUntilGameTime;
    }

    public void setDeadUntilGameTime(long deadUntilGameTime) {
        this.deadUntilGameTime = Math.max(0L, deadUntilGameTime);
    }

    public boolean isDeadCooldown(long gameTime) {
        return deadUntilGameTime > gameTime;
    }

    public int getCooldownSecondsLeft(long gameTime) {
        if (!isDeadCooldown(gameTime)) return 0;
        return (int) Math.ceil((deadUntilGameTime - gameTime) / 20.0D);
    }

    public UUID getActiveEntityId() {
        return activeEntityId;
    }

    public void setActiveEntityId(UUID activeEntityId) {
        this.activeEntityId = activeEntityId;
    }

    public boolean isActive() {
        return activeEntityId != null;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PetId", petId);
        tag.putString("Rarity", rarity.getId());
        tag.putInt("Level", level);
        tag.putInt("Exp", exp);
        tag.putString("CustomName", customNameJson == null ? "" : customNameJson);
        tag.putLong("DeadUntilGameTime", deadUntilGameTime);
        if (activeEntityId != null) {
            tag.putUUID("ActiveEntityId", activeEntityId);
        }
        tag.putString("EntityType", entityTypeId == null ? "" : entityTypeId);
        tag.put("EntityNbt", entityNbt == null ? new CompoundTag() : entityNbt.copy());
        return tag;
    }

    public static PetData load(CompoundTag tag) {
        UUID petId = tag.hasUUID("PetId") ? tag.getUUID("PetId") : UUID.randomUUID();
        PetRarity rarity = PetRarity.byId(tag.getString("Rarity"));
        int level = tag.getInt("Level");
        int exp = tag.getInt("Exp");
        String customName = tag.getString("CustomName");
        long deadUntil = tag.getLong("DeadUntilGameTime");
        UUID activeEntityId = tag.hasUUID("ActiveEntityId") ? tag.getUUID("ActiveEntityId") : null;
        String entityType = tag.getString("EntityType");
        CompoundTag entityNbt = tag.contains("EntityNbt", 10) ? tag.getCompound("EntityNbt") : new CompoundTag();
        return new PetData(petId, rarity, level, exp, customName, deadUntil, activeEntityId, entityType, entityNbt);
    }
}
