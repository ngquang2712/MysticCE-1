package com.example.examplemod.pet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class PetAIController {
    public static final String MODE_PASSIVE = "passive";
    public static final String MODE_ASSIST = "assist";
    public static final String MODE_AGGRESSIVE = "aggressive";
    public static final String MODE_STAY = "stay";

    private static final String TAG_MODE = "Mode";
    private static final String TAG_FORCED_TARGET = "ForcedTarget";
    private static final String TAG_STAY_X = "StayX";
    private static final String TAG_STAY_Y = "StayY";
    private static final String TAG_STAY_Z = "StayZ";

    private static final double FOLLOW_DISTANCE_SQR = 64.0D;
    private static final double TELEPORT_DISTANCE_SQR = 1600.0D;
    private static final double TARGET_RANGE = 36.0D;

    public static void tick(LivingEntity pet, ServerPlayer owner) {
        CapturedPetUtil.neutralizeForOwner(pet, owner);
        if (!(pet.level() instanceof ServerLevel level)) return;

        String mode = getMode(pet);
        LivingEntity target = chooseTarget(pet, owner, mode);

        if (pet instanceof Mob mob) {
            mob.setNoAi(false);

            if (mode.equals(MODE_STAY)) {
                handleStay(mob);
                if (target != null) {
                    setTarget(mob, owner, target);
                } else if (isProtectedTarget(mob, mob.getTarget())) {
                    mob.setTarget(null);
                }
                return;
            }

            if (target != null) {
                setTarget(mob, owner, target);
                return;
            }

            if (isProtectedTarget(mob, mob.getTarget()) || !isValidEnemy(owner, mob.getTarget())) {
                mob.setTarget(null);
            }

            followOwner(mob, owner);
        } else if (pet.distanceToSqr(owner) > TELEPORT_DISTANCE_SQR) {
            pet.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
    }

    public static String getMode(Entity entity) {
        CompoundTag root = getPetRoot(entity);
        String mode = root.getString(TAG_MODE);
        if (mode.equals(MODE_PASSIVE) || mode.equals(MODE_ASSIST) || mode.equals(MODE_AGGRESSIVE) || mode.equals(MODE_STAY)) {
            return mode;
        }
        return MODE_ASSIST;
    }

    public static void setMode(Entity entity, String mode) {
        CompoundTag root = getPetRoot(entity);
        if (!mode.equals(MODE_PASSIVE) && !mode.equals(MODE_ASSIST) && !mode.equals(MODE_AGGRESSIVE) && !mode.equals(MODE_STAY)) {
            mode = MODE_ASSIST;
        }
        root.putString(TAG_MODE, mode);
        entity.getPersistentData().put(CapturedPetUtil.TAG_ROOT, root);
    }

    public static void setForcedTarget(Entity entity, LivingEntity target) {
        CompoundTag root = getPetRoot(entity);
        if (target == null) {
            root.remove(TAG_FORCED_TARGET);
        } else {
            root.putUUID(TAG_FORCED_TARGET, target.getUUID());
        }
        entity.getPersistentData().put(CapturedPetUtil.TAG_ROOT, root);
    }

    public static void setStayHere(Entity entity) {
        CompoundTag root = getPetRoot(entity);
        root.putString(TAG_MODE, MODE_STAY);
        root.putDouble(TAG_STAY_X, entity.getX());
        root.putDouble(TAG_STAY_Y, entity.getY());
        root.putDouble(TAG_STAY_Z, entity.getZ());
        entity.getPersistentData().put(CapturedPetUtil.TAG_ROOT, root);
    }

    private static LivingEntity chooseTarget(LivingEntity pet, ServerPlayer owner, String mode) {
        if (mode.equals(MODE_PASSIVE)) return null;

        LivingEntity forced = getForcedTarget(pet, owner);
        if (forced != null) return forced;

        LivingEntity attacker = owner.getLastHurtByMob();
        if (isValidEnemy(owner, attacker)) return attacker;

        LivingEntity attacked = owner.getLastHurtMob();
        if (isValidEnemy(owner, attacked)) return attacked;

        if (mode.equals(MODE_AGGRESSIVE)) {
            return findNearestHostile(pet, owner).orElse(null);
        }
        return null;
    }

    private static LivingEntity getForcedTarget(LivingEntity pet, ServerPlayer owner) {
        CompoundTag root = getPetRoot(pet);
        if (!root.hasUUID(TAG_FORCED_TARGET) || !(pet.level() instanceof ServerLevel level)) return null;
        UUID id = root.getUUID(TAG_FORCED_TARGET);
        Entity entity = level.getEntity(id);
        if (entity instanceof LivingEntity target && isValidEnemy(owner, target)) {
            return target;
        }
        root.remove(TAG_FORCED_TARGET);
        pet.getPersistentData().put(CapturedPetUtil.TAG_ROOT, root);
        return null;
    }

    private static Optional<LivingEntity> findNearestHostile(LivingEntity pet, ServerPlayer owner) {
        AABB box = owner.getBoundingBox().inflate(TARGET_RANGE);
        return owner.level().getEntitiesOfClass(Monster.class, box, mob -> isValidEnemy(owner, mob))
                .stream()
                .map(mob -> (LivingEntity) mob)
                .min(Comparator.comparingDouble(mob -> mob.distanceToSqr(owner)));
    }

    private static void setTarget(Mob mob, ServerPlayer owner, LivingEntity target) {
        if (!isValidEnemy(owner, target)) {
            mob.setTarget(null);
            return;
        }
        mob.setTarget(target);
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (mob.distanceToSqr(target) > 4.0D) {
            mob.getNavigation().moveTo(target, 1.35D);
        }
    }

    private static void followOwner(Mob mob, ServerPlayer owner) {
        double distance = mob.distanceToSqr(owner);
        if (distance > TELEPORT_DISTANCE_SQR) {
            mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            mob.getNavigation().stop();
            return;
        }
        if (distance > FOLLOW_DISTANCE_SQR) {
            mob.getNavigation().moveTo(owner, 1.25D);
        } else if (mob.getTarget() == null) {
            mob.getNavigation().stop();
        }
    }

    private static void handleStay(Mob mob) {
        CompoundTag root = getPetRoot(mob);
        double x = root.contains(TAG_STAY_X) ? root.getDouble(TAG_STAY_X) : mob.getX();
        double y = root.contains(TAG_STAY_Y) ? root.getDouble(TAG_STAY_Y) : mob.getY();
        double z = root.contains(TAG_STAY_Z) ? root.getDouble(TAG_STAY_Z) : mob.getZ();
        if (mob.distanceToSqr(x, y, z) > 9.0D) {
            mob.getNavigation().moveTo(x, y, z, 1.1D);
        } else if (mob.getTarget() == null) {
            mob.getNavigation().stop();
        }
    }

    public static boolean isValidEnemy(ServerPlayer owner, LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (target.getUUID().equals(owner.getUUID())) return false;
        if (CapturedPetUtil.isMysticPet(target)) {
            Optional<UUID> targetOwner = CapturedPetUtil.getOwner(target);
            return targetOwner.isEmpty() || !targetOwner.get().equals(owner.getUUID());
        }
        return true;
    }

    private static boolean isProtectedTarget(Entity pet, LivingEntity target) {
        if (target == null) return false;
        Optional<UUID> owner = CapturedPetUtil.getOwner(pet);
        if (owner.isEmpty()) return false;
        if (target.getUUID().equals(owner.get())) return true;
        if (CapturedPetUtil.isMysticPet(target)) {
            Optional<UUID> targetOwner = CapturedPetUtil.getOwner(target);
            return targetOwner.isPresent() && targetOwner.get().equals(owner.get());
        }
        return false;
    }

    private static CompoundTag getPetRoot(Entity entity) {
        CompoundTag root = entity.getPersistentData().getCompound(CapturedPetUtil.TAG_ROOT);
        if (!root.contains(TAG_MODE)) {
            root.putString(TAG_MODE, MODE_ASSIST);
        }
        return root;
    }
}
