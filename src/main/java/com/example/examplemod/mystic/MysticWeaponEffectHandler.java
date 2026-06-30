package com.example.examplemod.mystic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MysticWeaponEffectHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty() || !weapon.hasTag()) {
            return;
        }

        LivingEntity target = event.getEntity();
        CompoundTag tag = weapon.getOrCreateTag();

        if (tag.getBoolean(WeaponStoneItem.KILL_TRACKER)) {
            if (target instanceof Player) {
                tag.putInt(WeaponStoneItem.PLAYER_KILLS, tag.getInt(WeaponStoneItem.PLAYER_KILLS) + 1);
            } else {
                tag.putInt(WeaponStoneItem.MOB_KILLS, tag.getInt(WeaponStoneItem.MOB_KILLS) + 1);
            }
        }

        if (tag.getBoolean(WeaponStoneItem.BEHEADING) && target instanceof Player) {
            Player deadPlayer = (Player) target;
            ItemStack head = getHeadDrop(deadPlayer);
            if (!head.isEmpty()) {
                if (!player.getInventory().add(head)) {
                    player.drop(head, false);
                }
                player.displayClientMessage(Component.literal("✟ Trảm: Đã lấy đầu đối phương ✟"), true);
            }
        }
    }

    private static ItemStack getHeadDrop(LivingEntity target) {
        EntityType<?> type = target.getType();
        if (target instanceof Player player) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            CompoundTag skullOwner = new CompoundTag();
            skullOwner.putString("Name", player.getGameProfile().getName());
            head.getOrCreateTag().put("SkullOwner", skullOwner);
            return head;
        }
        if (type == EntityType.ZOMBIE) return new ItemStack(Items.ZOMBIE_HEAD);
        if (type == EntityType.SKELETON) return new ItemStack(Items.SKELETON_SKULL);
        if (type == EntityType.WITHER_SKELETON) return new ItemStack(Items.WITHER_SKELETON_SKULL);
        if (type == EntityType.CREEPER) return new ItemStack(Items.CREEPER_HEAD);
        if (type == EntityType.ENDER_DRAGON) return new ItemStack(Items.DRAGON_HEAD);
        return ItemStack.EMPTY;
    }
}
