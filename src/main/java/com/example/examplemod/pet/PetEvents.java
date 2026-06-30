package com.example.examplemod.pet;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.example.examplemod.ExampleMod;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
class PetModEvents {
    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ExampleMod.MYSTIC_PET.get(), MysticPetEntity.createAttributes().build());
    }
}

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class PetEvents {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer) || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }
        for (int slot = 1; slot <= PetStorage.SLOT_COUNT; slot++) {
            Optional<PetData> data = PetStorage.get(oldPlayer, slot);
            if (data.isPresent()) {
                PetData petData = data.get();
                petData.setActiveEntityId(null);
                PetStorage.set(newPlayer, slot, petData);
            }
        }
    }

    @SubscribeEvent
    public static void onPetTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide || !CapturedPetUtil.isMysticPet(living) || living.tickCount % 5 != 0) {
            return;
        }
        Optional<UUID> ownerId = CapturedPetUtil.getOwner(living);
        if (ownerId.isEmpty() || !(living.level() instanceof ServerLevel serverLevel)) return;
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId.get());
        if (owner == null) return;
        PetAIController.tick(living, owner);
    }


    @SubscribeEvent
    public static void onPetChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity living = event.getEntity();
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget == null) return;

        if (CapturedPetUtil.isMysticPet(living) && isProtectedTargetForPet(living, newTarget)) {
            event.setCanceled(true);
            event.setNewTarget(null);
            if (living instanceof Mob mob) {
                mob.setTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onPetOrOwnerAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity victim = event.getEntity();

        if (attacker instanceof ServerPlayer owner && !CapturedPetUtil.isMysticPet(victim)) {
            orderPetsToAttack(owner, victim);
        }

        if (victim instanceof ServerPlayer owner && attacker instanceof LivingEntity enemy && !CapturedPetUtil.isMysticPet(enemy)) {
            orderPetsToAttack(owner, enemy);
        }

        if (attacker != null && CapturedPetUtil.isMysticPet(attacker)) {
            if (isProtectedTargetForPet(attacker, victim)) {
                event.setCanceled(true);
                if (attacker instanceof Mob mob) {
                    mob.setTarget(null);
                }
            }
        }

        if (CapturedPetUtil.isMysticPet(victim) && attacker != null) {
            Optional<UUID> owner = CapturedPetUtil.getOwner(victim);
            if (owner.isPresent() && attacker.getUUID().equals(owner.get())) {
                event.setCanceled(true);
            } else if (attacker instanceof LivingEntity enemy && victim instanceof Mob petMob && !CapturedPetUtil.isMysticPet(enemy)) {
                PetAIController.setForcedTarget(petMob, enemy);
                petMob.setTarget(enemy);
            }
        }
    }

    private static void orderPetsToAttack(ServerPlayer owner, LivingEntity target) {
        if (target == null || !target.isAlive() || target.getUUID().equals(owner.getUUID())) return;
        if (CapturedPetUtil.isMysticPet(target)) {
            Optional<UUID> targetOwner = CapturedPetUtil.getOwner(target);
            if (targetOwner.isPresent() && targetOwner.get().equals(owner.getUUID())) {
                return;
            }
        }
        for (int slot = 1; slot <= PetStorage.SLOT_COUNT; slot++) {
            Optional<PetData> optional = PetStorage.get(owner, slot);
            if (optional.isEmpty()) continue;
            UUID activeId = optional.get().getActiveEntityId();
            if (activeId == null) continue;
            Entity entity = owner.serverLevel().getEntity(activeId);
            if (entity instanceof Mob petMob && entity.isAlive() && CapturedPetUtil.isMysticPet(entity)) {
                if (!isProtectedTargetForPet(entity, target)) {
                    PetAIController.setForcedTarget(petMob, target);
                    petMob.setTarget(target);
                }
            }
        }
    }

    private static boolean isProtectedTargetForPet(Entity pet, LivingEntity target) {
        Optional<UUID> owner = CapturedPetUtil.getOwner(pet);
        if (owner.isEmpty()) return false;
        if (target.getUUID().equals(owner.get())) return true;
        if (CapturedPetUtil.isMysticPet(target)) {
            Optional<UUID> targetOwner = CapturedPetUtil.getOwner(target);
            return targetOwner.isPresent() && targetOwner.get().equals(owner.get());
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (CapturedPetUtil.isMysticPet(dead)) {
            handlePetDeath(dead);
            return;
        }

        Entity killer = event.getSource().getEntity();
        if (killer != null && CapturedPetUtil.isMysticPet(killer)) {
            addExpForPetKill(killer, dead);
        }
    }

    private static void handlePetDeath(LivingEntity pet) {
        if (!(pet.level() instanceof ServerLevel serverLevel)) return;
        Optional<UUID> ownerId = CapturedPetUtil.getOwner(pet);
        Optional<UUID> petId = CapturedPetUtil.getPetId(pet);
        if (ownerId.isEmpty() || petId.isEmpty()) return;
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId.get());
        if (owner == null) return;
        Optional<Integer> slot = PetStorage.findByPetId(owner, petId.get());
        if (slot.isEmpty()) return;
        Optional<PetData> optional = PetStorage.get(owner, slot.get());
        if (optional.isEmpty()) return;
        PetData data = optional.get();
        data.setActiveEntityId(null);
        data.setDeadUntilGameTime(owner.serverLevel().getGameTime() + PetData.RESPAWN_COOLDOWN_SECONDS * 20L);
        PetStorage.set(owner, slot.get(), data);
        owner.sendSystemMessage(Component.literal("§cPet đã chết và sẽ hồi sau 120 giây."));
    }

    private static void addExpForPetKill(Entity pet, LivingEntity killed) {
        if (!(pet.level() instanceof ServerLevel serverLevel)) return;
        Optional<UUID> ownerId = CapturedPetUtil.getOwner(pet);
        Optional<UUID> petId = CapturedPetUtil.getPetId(pet);
        if (ownerId.isEmpty() || petId.isEmpty()) return;
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId.get());
        if (owner == null) return;
        Optional<Integer> slot = PetStorage.findByPetId(owner, petId.get());
        if (slot.isEmpty()) return;
        Optional<PetData> optional = PetStorage.get(owner, slot.get());
        if (optional.isEmpty()) return;
        PetData data = optional.get();
        int beforeLevel = data.getLevel();
        data.addExp(getExpForKilledEntity(killed));
        data.setCapturedEntity(CapturedPetUtil.entityTypeId(pet), CapturedPetUtil.snapshotEntity(pet));
        PetStorage.set(owner, slot.get(), data);
        if (data.getLevel() > beforeLevel) {
            CapturedPetUtil.markAsPet(pet, owner, data);
            owner.sendSystemMessage(Component.literal("§aPet đã lên cấp " + data.getLevel() + "/" + data.getRarity().getMaxLevel() + "!"));
        }
    }

    private static int getExpForKilledEntity(LivingEntity killed) {
        if (killed instanceof net.minecraft.world.entity.animal.Animal) return 10;
        if (killed instanceof net.minecraft.world.entity.monster.Monster) return 25;
        return 15;
    }
}
