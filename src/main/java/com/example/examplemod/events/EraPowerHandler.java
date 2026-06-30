package com.example.examplemod.events;

import com.example.examplemod.expansion.VirtualHealthManager;
import com.example.examplemod.mystic.MysticArmorItem;
import com.example.examplemod.mystic.MysticTier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class EraPowerHandler {

    private static final UUID ERA_HEALTH_UUID = UUID.fromString("79761dc8-f70c-4ce0-8bb5-401c78b669c1");
    private static final UUID ERA_ATTACK_UUID = UUID.fromString("bf3b9c4b-4b24-4886-8563-962d29d7fd13");
    private static final String ERA_APPLIED_MULTIPLIER = "MysticEraMultiplierApplied";
    private static Era CURRENT_ERA = Era.ORIGIN;
    private static long LAST_SCAN_TICK = 0L;
    private static boolean HAS_NETHER = false;
    private static boolean HAS_END = false;
    private static boolean HAS_ANCIENT = false;
    private static boolean HAS_CHAOS = false;
    private static boolean HAS_GALAXY = false;

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            recalculateEra(player.serverLevel());
            sendEraMessage(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        long gameTime = event.player.level().getGameTime();
        if (gameTime - LAST_SCAN_TICK < 100L) return;
        LAST_SCAN_TICK = gameTime;
        Era before = CURRENT_ERA;
        recalculateEra((Level) event.player.level());
        if (before != CURRENT_ERA && event.player.level().getServer() != null) {
            for (ServerPlayer player : event.player.level().getServer().getPlayerList().getPlayers()) {
                sendEraMessage(player);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        applyEraStats(mob);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (attacker instanceof Player) return;
        if (attacker instanceof Mob mob) {
            applyEraStats(mob);
        }

        Era era = CURRENT_ERA;
        float amount = event.getAmount();

        if (isSkillDamage(event)) {
            amount *= era.multiplier;
        }

        float percentBonus = player.getMaxHealth() * era.playerHpDamagePercent;
        if (percentBonus > 0.0F) {
            amount += percentBonus;
        }

        event.setAmount(amount);
    }

    private static boolean isSkillDamage(LivingHurtEvent event) {
        return event.getSource().getDirectEntity() != null
                && event.getSource().getEntity() != null
                && event.getSource().getDirectEntity() != event.getSource().getEntity();
    }

    private static void applyEraStats(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        double multiplier = CURRENT_ERA.multiplier;
        double oldMultiplier = data.contains(ERA_APPLIED_MULTIPLIER) ? data.getDouble(ERA_APPLIED_MULTIPLIER) : 0.0D;
        if (Math.abs(oldMultiplier - multiplier) < 0.001D) return;
        data.putDouble(ERA_APPLIED_MULTIPLIER, multiplier);

        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            double baseTotal = mob.getPersistentData().contains("MysticVirtualTotalMaxHealth")
                    ? mob.getPersistentData().getDouble("MysticVirtualTotalMaxHealth")
                    : health.getBaseValue();
            double desiredTotal = Math.max(1.0D, baseTotal * multiplier);
            VirtualHealthManager.setMaxHealth(mob, desiredTotal);
        }

        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            AttributeModifier old = attack.getModifier(ERA_ATTACK_UUID);
            if (old != null) attack.removeModifier(ERA_ATTACK_UUID);
            if (multiplier > 1.0D) {
                attack.addPermanentModifier(new AttributeModifier(
                        ERA_ATTACK_UUID,
                        "Mystic era attack",
                        multiplier - 1.0D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }
    }

    private static void recalculateEra(Level level) {
        if (level.getServer() == null) return;

        int ancientSetPlayers = 0;
        boolean galaxySet = false;
        boolean hasEnd = false;
        boolean hasNether = false;

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension() == Level.END) {
                hasEnd = true;
                HAS_END = true;
            }
            if (player.level().dimension() == Level.NETHER) {
                hasNether = true;
                HAS_NETHER = true;
            }

            MysticTier fullSet = getFullSet(player);
            if (fullSet == MysticTier.THIEN_HA) {
                galaxySet = true;
                HAS_GALAXY = true;
            }
            if (fullSet == MysticTier.THUONG_CO) ancientSetPlayers++;
        }

        if (ancientSetPlayers >= 1) HAS_ANCIENT = true;
        if (ancientSetPlayers >= 2) HAS_CHAOS = true;

        if (galaxySet || HAS_GALAXY) CURRENT_ERA = Era.GALAXY;
        else if (ancientSetPlayers >= 2 || HAS_CHAOS) CURRENT_ERA = Era.CHAOS;
        else if (ancientSetPlayers >= 1 || HAS_ANCIENT) CURRENT_ERA = Era.ANCIENT;
        else if (hasEnd || HAS_END) CURRENT_ERA = Era.END;
        else if (hasNether || HAS_NETHER) CURRENT_ERA = Era.NETHER;
        else CURRENT_ERA = Era.ORIGIN;
    }

    private static MysticTier getFullSet(Player player) {
        MysticTier tier = null;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof MysticArmorItem armor)) return null;
            if (tier == null) tier = armor.getMysticTier();
            else if (tier != armor.getMysticTier()) return null;
        }
        return tier;
    }

    private static void sendEraMessage(ServerPlayer player) {
        Era era = CURRENT_ERA;
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(
                Component.literal("✦ ").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.OBFUSCATED)
                        .append(Component.literal("[" + era.displayName + "]").withStyle(era.color, ChatFormatting.BOLD))
                        .append(Component.literal(" ✦").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.OBFUSCATED))
        );
        player.sendSystemMessage(line("HP quái/boss", era.multiplierText));
        player.sendSystemMessage(line("Attack Damage", era.multiplierText));
        player.sendSystemMessage(line("Skill Damage", era.multiplierText));
        player.sendSystemMessage(line("Sát thương theo máu người chơi", "+" + (int) (era.playerHpDamagePercent * 100) + "% HP tối đa"));
        player.sendSystemMessage(Component.empty());
    }

    private static MutableComponent line(String name, String value) {
        return Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(name + ": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
    }


    public static boolean isChaosEraOrHigher() {
        return CURRENT_ERA.ordinal() >= Era.CHAOS.ordinal();
    }

    public static boolean isGalaxyEraOrHigher() {
        return CURRENT_ERA.ordinal() >= Era.GALAXY.ordinal();
    }

    public static String getCurrentEraDisplayName() {
        return CURRENT_ERA.displayName;
    }

    private enum Era {
        ORIGIN("THỜI KỲ KHỞI NGUYÊN", "x1", 1.0F, 0.00F, ChatFormatting.GOLD),
        NETHER("THỜI KỲ NETHER", "x1.5", 1.5F, 0.01F, ChatFormatting.RED),
        END("THỜI KỲ THE END", "x2", 2.0F, 0.02F, ChatFormatting.DARK_PURPLE),
        ANCIENT("THỜI KỲ THƯỢNG CỔ", "x2.5", 2.5F, 0.03F, ChatFormatting.GREEN),
        CHAOS("THỜI KỲ HỖN ĐỘN", "x3", 3.0F, 0.04F, ChatFormatting.DARK_RED),
        GALAXY("THỜI KỲ THIÊN HÀ", "x4", 4.0F, 0.05F, ChatFormatting.AQUA);

        private final String displayName;
        private final String multiplierText;
        private final float multiplier;
        private final float playerHpDamagePercent;
        private final ChatFormatting color;

        Era(String displayName, String multiplierText, float multiplier, float playerHpDamagePercent, ChatFormatting color) {
            this.displayName = displayName;
            this.multiplierText = multiplierText;
            this.multiplier = multiplier;
            this.playerHpDamagePercent = playerHpDamagePercent;
            this.color = color;
        }
    }
}
