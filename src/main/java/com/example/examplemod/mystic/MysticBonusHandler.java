package com.example.examplemod.mystic;

import com.example.examplemod.ExampleMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class MysticBonusHandler {
    private static final UUID MYSTIC_SKULL_HEALTH_UUID = UUID.fromString("23d04fe5-339f-49d7-a196-9802401d7a73");
    private static final UUID MYSTIC_ARMOR_HEALTH_UUID = UUID.fromString("f897ee4d-4742-4e7e-82ce-fc82b34c8e43");
    private static final UUID MYSTIC_SHIELD_HEALTH_UUID = UUID.fromString("b1a6c1d9-3a8b-4f3c-9b2f-2d5f7b7c6e11");
    private static final UUID MYSTIC_SHIELD_ARMOR_UUID = UUID.fromString("d9f4a5b2-6c3e-4a1f-9e2b-8b7c5a6d4f22");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        MysticTier fullSetTier = getFullArmorSetTier(player);
        // Track last full-set tier in player persistent data to avoid spamming messages
        CompoundTag persistent = player.getPersistentData();
        final String KEY = "MysticLastFullSet";
        String previous = persistent.contains(KEY) ? persistent.getString(KEY) : "";
        String current = fullSetTier == null ? "" : fullSetTier.name();

        if (!current.isEmpty() && !current.equals(previous)) {
            player.sendSystemMessage(buildSetActivatedMessage(fullSetTier));
        }
        persistent.putString(KEY, current);

        if (fullSetTier != null) {
            applyFullSetEffects(player, fullSetTier);
        }

        double armorHealth = getMysticArmorHealthBonus(player);
        if (fullSetTier != null) {
            armorHealth += fullSetTier.getSetHealthBonus();
        }
        applyHealthModifier(player, MYSTIC_ARMOR_HEALTH_UUID, "Mystic armor health", armorHealth);

        double skullHealth = getHotbarSkullHealthBonus(player);
        applyHealthModifier(player, MYSTIC_SKULL_HEALTH_UUID, "Mystic skull health", skullHealth);

        double shieldHealth = getEquippedShieldHealthBonus(player);
        applyHealthModifier(player, MYSTIC_SHIELD_HEALTH_UUID, "Mystic shield health", shieldHealth);

        double shieldArmor = getEquippedShieldArmorBonus(player);
        applyArmorModifier(player, MYSTIC_SHIELD_ARMOR_UUID, "Mystic shield armor", shieldArmor);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        float amount = event.getAmount();

        MysticTier fullSetTier = getFullArmorSetTier(player);
        MysticTier weaponTier = getHeldMysticWeaponTier(player);

        if (fullSetTier != null && fullSetTier == weaponTier) {
            amount *= (1.0F + fullSetTier.getWeaponDamageMultiplier());
        }

        float skullDamage = getHotbarSkullDamageBonus(player);
        if (skullDamage > 0.0F) {
            amount += skullDamage;
        }

        event.setAmount(amount);
    }

    private static double getMysticArmorHealthBonus(Player player) {
        double total = 0.0D;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof MysticArmorItem armorItem) {
                total += switch (armorItem.getMysticTier()) {
                    case TRUYEN_THUYET -> 4.0D;
                    case TOI_CAO -> 10.0D;
                    case THUONG_CO -> 20.0D;
                    case THIEN_HA -> 25.0D;
                    case NHAM_DAN, HUYEN_THOAI, SIEU_SAIYAN, THO_MO -> 0.0D;
                };
            }
        }
        return total;
    }


    private static void applyFullSetEffects(Player player, MysticTier tier) {
        // 219 ticks để effect không bị nhấp nháy, mỗi tick sẽ được làm mới khi còn mặc full set.
        if (tier.getResistanceAmplifier() >= 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 219, tier.getResistanceAmplifier(), true, false, true));
        }

        switch (tier) {
            case HUYEN_THOAI -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 219, 1, true, false, true));
            case NHAM_DAN -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 219, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 219, 2, true, false, true));
            }
            case SIEU_SAIYAN -> {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 219, 4, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 219, 1, true, false, true));
            }
            case THO_MO -> {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 219, 4, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 219, 1, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 219, 2, true, false, true));
            }
            default -> {
            }
        }
    }

    private static MysticTier getHeldMysticWeaponTier(Player player) {
        ItemStack stack = player.getMainHandItem();
        Item item = stack.getItem();

        if (!(item instanceof SwordItem) && !(item instanceof AxeItem)) {
            return null;
        }

        if (item == ExampleMod.TRUYEN_THUYET_SWORD.get() || item == ExampleMod.TRUYEN_THUYET_AXE.get()) {
            return MysticTier.TRUYEN_THUYET;
        }
        if (item == ExampleMod.TOI_CAO_SWORD.get() || item == ExampleMod.TOI_CAO_AXE.get()) {
            return MysticTier.TOI_CAO;
        }
        if (item == ExampleMod.THUONG_CO_SWORD.get() || item == ExampleMod.THUONG_CO_AXE.get()) {
            return MysticTier.THUONG_CO;
        }
        if (item == ExampleMod.THIEN_HA_SWORD.get() || item == ExampleMod.THIEN_HA_AXE.get()) {
            return MysticTier.THIEN_HA;
        }
        if (item == ExampleMod.NHAM_DAN_SWORD.get() || item == ExampleMod.NHAM_DAN_AXE.get()) {
            return MysticTier.NHAM_DAN;
        }
        if (item == ExampleMod.HUYEN_THOAI_SWORD.get() || item == ExampleMod.HUYEN_THOAI_AXE.get()) {
            return MysticTier.HUYEN_THOAI;
        }
        if (item == ExampleMod.SIEU_SAIYAN_SWORD.get() || item == ExampleMod.SIEU_SAIYAN_AXE.get()) {
            return MysticTier.SIEU_SAIYAN;
        }
        if (item == ExampleMod.THO_MO_SWORD.get() || item == ExampleMod.THO_MO_AXE.get()) {
            return MysticTier.THO_MO;
        }

        return null;
    }

    private static MysticTier getFullArmorSetTier(Player player) {
        MysticTier tier = null;

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof MysticArmorItem armorItem)) {
                return null;
            }

            if (tier == null) {
                tier = armorItem.getMysticTier();
            } else if (tier != armorItem.getMysticTier()) {
                return null;
            }
        }

        return tier;
    }

    private static double getHotbarSkullHealthBonus(Player player) {
        double total = 0.0D;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof SkullItem) {
                ListTag fireList = SkullItem.getFireList(stack);
                for (int i = 0; i < fireList.size(); i++) {
                    MagicFireType fireType = readFireType(fireList.getCompound(i).getString("FIRE_ID"));
                    if (fireType != null) {
                        total += fireType.getHealthBonus();
                    }
                }
            }
        }
        return total;
    }

    private static float getHotbarSkullDamageBonus(Player player) {
        float total = 0.0F;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof SkullItem) {
                ListTag fireList = SkullItem.getFireList(stack);
                for (int i = 0; i < fireList.size(); i++) {
                    MagicFireType fireType = readFireType(fireList.getCompound(i).getString("FIRE_ID"));
                    if (fireType != null) {
                        total += fireType.getDamageBonus();
                    }
                }
            }
        }
        return total;
    }

    private static MagicFireType readFireType(String id) {
        try {
            return MagicFireType.valueOf(id);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static ShieldEnhanceType readShieldEnhanceType(String id) {
        try {
            return ShieldEnhanceType.valueOf(id);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static double getEquippedShieldHealthBonus(Player player) {
        double total = 0.0D;
        ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (off.getItem() instanceof net.minecraft.world.item.ShieldItem) {
            ListTag list = ShieldHelper.getEnhanceList(off);
            for (int i = 0; i < list.size(); i++) {
                ShieldEnhanceType t = readShieldEnhanceType(list.getCompound(i).getString("ENHANCE_ID"));
                if (t != null) total += t.getHealthBonus();
            }
        }
        return total;
    }

    private static double getEquippedShieldArmorBonus(Player player) {
        double total = 0.0D;
        ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (off.getItem() instanceof net.minecraft.world.item.ShieldItem) {
            ListTag list = ShieldHelper.getEnhanceList(off);
            for (int i = 0; i < list.size(); i++) {
                ShieldEnhanceType t = readShieldEnhanceType(list.getCompound(i).getString("ENHANCE_ID"));
                if (t != null) total += t.getArmorBonus();
            }
        }
        return total;
    }

    private static void applyArmorModifier(Player player, UUID uuid, String name, double amount) {
        AttributeInstance attribute = player.getAttribute(Attributes.ARMOR);
        if (attribute == null) return;

        AttributeModifier oldModifier = attribute.getModifier(uuid);
        if (oldModifier != null) attribute.removeModifier(uuid);

        if (amount > 0.0D) {
            attribute.addTransientModifier(new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void applyHealthModifier(Player player, UUID uuid, String name, double amount) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) {
            return;
        }

        AttributeModifier oldModifier = attribute.getModifier(uuid);
        if (oldModifier != null) {
            attribute.removeModifier(uuid);
        }

        if (amount > 0.0D) {
            attribute.addTransientModifier(new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.ADDITION));
        }
    }

    private static MutableComponent buildSetActivatedMessage(MysticTier tier) {
        ChatFormatting color = switch (tier) {
            case TRUYEN_THUYET -> ChatFormatting.DARK_PURPLE;
            case TOI_CAO -> ChatFormatting.RED;
            case THUONG_CO -> ChatFormatting.GREEN;
            case THIEN_HA -> ChatFormatting.AQUA;
            case NHAM_DAN -> ChatFormatting.GREEN;
            case HUYEN_THOAI -> ChatFormatting.GOLD;
            case SIEU_SAIYAN -> ChatFormatting.LIGHT_PURPLE;
            case THO_MO -> ChatFormatting.YELLOW;
        };

        String bodyText = switch (tier) {
            case TRUYEN_THUYET -> "Set truyền thuyết đã kích hoạt";
            case TOI_CAO -> "Set tối cao đã kích hoạt";
            case THUONG_CO -> "Set thượng cổ đã kích hoạt";
            case THIEN_HA -> "Set thiên hà đã kích hoạt";
            case NHAM_DAN -> "Set Nhâm Dần đã kích hoạt";
            case HUYEN_THOAI -> "Set Huyền Thoại đã kích hoạt";
            case SIEU_SAIYAN -> "Set Siêu Saiyan đã kích hoạt";
            case THO_MO -> "Set Thợ Mỏ đã kích hoạt";
        };

        MutableComponent left = Component.literal(" ✦ ").withStyle(color, ChatFormatting.OBFUSCATED);
        MutableComponent body = Component.literal(bodyText).withStyle(color, ChatFormatting.BOLD);
        MutableComponent right = Component.literal(" ✦").withStyle(color, ChatFormatting.OBFUSCATED);

        return left.append(body).append(right);
    }
}
