package com.example.examplemod.expansion;

import com.example.examplemod.ExampleMod;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class VirtualHealthManager {
    private static final String ENABLED = "MysticVirtualHealthEnabled";
    private static final String TOTAL_MAX = "MysticVirtualTotalMaxHealth";
    private static final String VIRTUAL = "MysticVirtualHealth";
    private static final double VANILLA_SAFE_MAX = 1000.0D;

    private VirtualHealthManager() {}

    public static void setMaxHealth(LivingEntity entity, double totalHealth) {
        if (entity == null || totalHealth <= 0.0D || entity.getAttribute(Attributes.MAX_HEALTH) == null) return;
        double vanillaHealth = Math.min(totalHealth, VANILLA_SAFE_MAX);
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(vanillaHealth);
        entity.setHealth((float) vanillaHealth);

        CompoundTag tag = entity.getPersistentData();
        tag.putDouble(TOTAL_MAX, totalHealth);
        if (totalHealth > vanillaHealth) {
            tag.putBoolean(ENABLED, true);
            tag.putDouble(VIRTUAL, totalHealth - vanillaHealth);
        } else {
            tag.remove(ENABLED);
            tag.remove(VIRTUAL);
        }
    }

    public static boolean hasVirtualHealth(LivingEntity entity) {
        return entity != null && entity.getPersistentData().getBoolean(ENABLED);
    }

    public static double getTotalMaxHealth(LivingEntity entity) {
        if (entity == null) return 0.0D;
        CompoundTag tag = entity.getPersistentData();
        if (tag.contains(TOTAL_MAX)) return tag.getDouble(TOTAL_MAX);
        return entity.getMaxHealth();
    }

    public static double getCurrentTotalHealth(LivingEntity entity) {
        if (entity == null) return 0.0D;
        return Math.max(0.0D, entity.getPersistentData().getDouble(VIRTUAL)) + Math.max(0.0D, entity.getHealth());
    }

    public static double getVirtualHealth(LivingEntity entity) {
        if (entity == null) return 0.0D;
        return Math.max(0.0D, entity.getPersistentData().getDouble(VIRTUAL));
    }

    public static void setVirtualHealth(LivingEntity entity, double amount) {
        if (entity == null) return;
        CompoundTag tag = entity.getPersistentData();
        if (amount <= 0.0D) {
            tag.putDouble(VIRTUAL, 0.0D);
        } else {
            tag.putBoolean(ENABLED, true);
            tag.putDouble(VIRTUAL, amount);
        }
    }

    public static Component formatHealthName(Component baseName, LivingEntity entity) {
        int current = (int)Math.ceil(getCurrentTotalHealth(entity));
        int max = (int)Math.ceil(getTotalMaxHealth(entity));
        return Component.empty().append(baseName).append(Component.literal(" §7[" + current + "/" + max + " HP]").withStyle(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !hasVirtualHealth(entity)) return;
        float damage = event.getAmount();
        if (damage <= 0.0F) return;

        double virtual = getVirtualHealth(entity);
        if (virtual <= 0.0D) return;

        if (damage < virtual) {
            setVirtualHealth(entity, virtual - damage);
            event.setCanceled(true);
            entity.setHealth(entity.getMaxHealth());
        } else {
            setVirtualHealth(entity, 0.0D);
            float leftover = (float)Math.max(0.0D, damage - virtual);
            if (leftover <= 0.0F) {
                event.setCanceled(true);
                entity.setHealth(entity.getMaxHealth());
            } else {
                event.setAmount(leftover);
            }
        }
    }

    public static void applyDirectDamage(LivingEntity entity, DamageSource source, float amount) {
        if (entity == null || amount <= 0.0F) return;
        double virtual = getVirtualHealth(entity);
        if (virtual > 0.0D) {
            if (amount < virtual) {
                setVirtualHealth(entity, virtual - amount);
                entity.setHealth(entity.getMaxHealth());
                return;
            }
            setVirtualHealth(entity, 0.0D);
            amount = (float)Math.max(0.0D, amount - virtual);
        }
        if (amount > 0.0F) entity.hurt(source, amount);
    }
}
