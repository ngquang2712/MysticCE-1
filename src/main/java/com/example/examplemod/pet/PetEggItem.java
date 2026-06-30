package com.example.examplemod.pet;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PetEggItem extends Item {
    private final PetRarity rarity;

    public PetEggItem(PetRarity rarity, Properties properties) {
        super(properties);
        this.rarity = rarity;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (!CapturedPetUtil.canCapture(target)) {
            serverPlayer.sendSystemMessage(Component.literal("§cSinh vật này không thể thu phục."));
            return InteractionResult.FAIL;
        }

        LivingEntity living = target;

        // Check HP caps per rarity
        int maxAllowedHp = rarity.getMaxTargetHP();
        double targetMaxHp = living.getMaxHealth();
        if (maxAllowedHp > 0 && targetMaxHp >= maxAllowedHp) {
            serverPlayer.sendSystemMessage(Component.literal("§cTrứng " + rarity.getDisplayComponent().getString() + " chỉ thu phục được mục tiêu HP dưới " + maxAllowedHp + "."));
            return InteractionResult.FAIL;
        }

        // Chance to capture
        double chance = rarity.getCaptureChancePercent();
        double roll = serverPlayer.getRandom().nextDouble() * 100.0D;
        if (roll > chance) {
            if (!serverPlayer.isCreative()) {
                stack.shrink(1);
            }
            serverPlayer.sendSystemMessage(Component.literal("§cThu phục thất bại. Tỉ lệ thành công: " + (int)chance + "%"));
            return InteractionResult.CONSUME;
        }

        int slot = PetStorage.firstEmptySlot(serverPlayer);
        if (slot == -1) {
            serverPlayer.sendSystemMessage(Component.literal("§cKho pet đã đầy! Tối đa 3 slot."));
            return InteractionResult.FAIL;
        }

        PetData data = new PetData(rarity);
        data.setCapturedEntity(CapturedPetUtil.entityTypeId(target), CapturedPetUtil.snapshotEntity(target));
        if (target.hasCustomName()) {
            data.setCustomName(target.getCustomName());
        }
        PetStorage.set(serverPlayer, slot, data);
        target.discard();

        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }

        serverPlayer.sendSystemMessage(Component.literal("§aĐã thu phục §e" + data.getEntityTypeId() + " §avào pet slot " + slot + ". Dùng §f/callpet " + slot + " §ađể gọi ra."));
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.literal("Chuột phải vào sinh vật để thu phục làm pet.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Pet giữ nguyên ngoại hình, NBT và chỉ số gốc của sinh vật.").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Max level theo rarity: " + rarity.getMaxLevel()).withStyle(ChatFormatting.YELLOW));
    }
}
