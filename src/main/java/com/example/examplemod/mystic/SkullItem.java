package com.example.examplemod.mystic;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SkullItem extends BlockItem {
    public static final String FIRE_LIST = "MYSTIC_FIRE_LIST";

    private final MysticTier tier;
    private final int slots;

    public SkullItem(Block block, MysticTier tier, int slots, Properties properties) {
        super(block, properties);
        this.tier = tier;
        this.slots = slots;
    }

    public MysticTier getTier() {
        return tier;
    }

    public int getSlots() {
        return slots;
    }

    public boolean addFire(ItemStack skullStack, MagicFireType fireType) {
        CompoundTag tag = skullStack.getOrCreateTag();
        ListTag fireList = tag.getList(FIRE_LIST, Tag.TAG_COMPOUND);

        if (fireList.size() >= slots) {
            return false;
        }

        CompoundTag fireTag = new CompoundTag();
        fireTag.putString("FIRE_ID", fireType.name());
        fireList.add(fireTag);
        tag.put(FIRE_LIST, fireList);
        return true;
    }

    public static ListTag getFireList(ItemStack skullStack) {
        if (skullStack.isEmpty() || !skullStack.hasTag()) {
            return new ListTag();
        }
        return skullStack.getTag().getList(FIRE_LIST, Tag.TAG_COMPOUND);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Sọ " + tier.getDisplayName()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Kích hoạt khi đặt trên hotbar").withStyle(ChatFormatting.YELLOW));

        ListTag fireList = getFireList(stack);
        tooltip.add(Component.literal("Slot Lửa Phép: " + fireList.size() + "/" + slots).withStyle(ChatFormatting.AQUA));

        for (int i = 0; i < fireList.size(); i++) {
            String fireId = fireList.getCompound(i).getString("FIRE_ID");
            try {
                MagicFireType fireType = MagicFireType.valueOf(fireId);
                tooltip.add(Component.literal("- " + fireType.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE));
            } catch (IllegalArgumentException ignored) {
                tooltip.add(Component.literal("- Lửa Phép lỗi").withStyle(ChatFormatting.RED));
            }
        }
    }
}
