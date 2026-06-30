package com.example.examplemod.pet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class MysticPetEntity extends Wolf {
    private static final EntityDataAccessor<String> PET_RARITY = SynchedEntityData.defineId(MysticPetEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> PET_LEVEL = SynchedEntityData.defineId(MysticPetEntity.class, EntityDataSerializers.INT);
    private UUID petId;

    public MysticPetEntity(EntityType<? extends MysticPetEntity> type, Level level) {
        super(type, level);
        this.petId = UUID.randomUUID();
        this.setTame(true);
        this.setOrderedToSit(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.ATTACK_DAMAGE, 25.0D)
                .add(Attributes.ARMOR, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30000001192092896D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PET_RARITY, PetRarity.TRUYEN_THUYET.getId());
        this.entityData.define(PET_LEVEL, 1);
    }

    public void setupFromData(ServerPlayer owner, PetData data) {
        this.petId = data.getPetId();
        this.setOwnerUUID(owner.getUUID());
        this.setTame(true);
        this.setOrderedToSit(false);
        this.entityData.set(PET_RARITY, data.getRarity().getId());
        this.entityData.set(PET_LEVEL, data.getLevel());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getRarity().getHealthAtLevel(data.getLevel()));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getRarity().getAttackAtLevel(data.getLevel()));
        this.getAttribute(Attributes.ARMOR).setBaseValue(data.getRarity().getArmorAtLevel(data.getLevel()));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
        this.setHealth(this.getMaxHealth());
        data.getCustomName().ifPresent(name -> {
            this.setCustomName(name);
            this.setCustomNameVisible(true);
        });
    }

    public UUID getPetId() {
        return petId;
    }

    public PetRarity getPetRarity() {
        return PetRarity.byId(this.entityData.get(PET_RARITY));
    }

    public int getPetLevel() {
        return this.entityData.get(PET_LEVEL);
    }

    public void refreshStatsFromStorage() {
        if (this.level() instanceof ServerLevel serverLevel && this.getOwnerUUID() != null) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(this.getOwnerUUID());
            if (owner == null) return;
            Optional<Integer> slot = PetStorage.findByPetId(owner, petId);
            if (slot.isEmpty()) return;
            Optional<PetData> data = PetStorage.get(owner, slot.get());
            if (data.isEmpty()) return;
            setupFromData(owner, data.get());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount % 40 == 0 && this.getOwner() != null && this.distanceToSqr(this.getOwner()) > 900.0D) {
            this.teleportTo(this.getOwner().getX(), this.getOwner().getY(), this.getOwner().getZ());
        }
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel && this.getOwnerUUID() != null) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(this.getOwnerUUID());
            if (owner != null) {
                Optional<Integer> slot = PetStorage.findByPetId(owner, petId);
                if (slot.isPresent()) {
                    Optional<PetData> data = PetStorage.get(owner, slot.get());
                    if (data.isPresent()) {
                        PetData petData = data.get();
                        petData.setActiveEntityId(null);
                        petData.setDeadUntilGameTime(owner.serverLevel().getGameTime() + PetData.RESPAWN_COOLDOWN_SECONDS * 20L);
                        PetStorage.set(owner, slot.get(), petData);
                        owner.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cPet đã chết và sẽ hồi sau 120 giây."));
                    }
                }
            }
        }
        super.die(source);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putUUID("MysticPetId", petId);
        tag.putString("MysticPetRarity", this.entityData.get(PET_RARITY));
        tag.putInt("MysticPetLevel", this.entityData.get(PET_LEVEL));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("MysticPetId")) {
            this.petId = tag.getUUID("MysticPetId");
        }
        if (tag.contains("MysticPetRarity")) {
            this.entityData.set(PET_RARITY, tag.getString("MysticPetRarity"));
        }
        if (tag.contains("MysticPetLevel")) {
            this.entityData.set(PET_LEVEL, tag.getInt("MysticPetLevel"));
        }
    }
}
