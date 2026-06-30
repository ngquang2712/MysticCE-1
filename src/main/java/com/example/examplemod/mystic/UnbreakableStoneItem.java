package com.example.examplemod.mystic;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class UnbreakableStoneItem extends Item {
    public UnbreakableStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stone = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stone, true);

        ItemStack target = player.getOffhandItem();
        if (target.isEmpty() || target == stone) target = player.getMainHandItem();
        if (target == stone) target = ItemStack.EMPTY;

        if (target.isEmpty()) {
            player.sendSystemMessage(Component.literal("Cần cầm vật phẩm ở tay còn lại!").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stone);
        }

        CompoundTag tag = target.getOrCreateTag();
        if (tag.getBoolean("Unbreakable")) {
            player.sendSystemMessage(Component.literal("Vật phẩm đã là Không Thể Bị Phá Hủy!").withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(stone);
        }

        tag.putBoolean("Unbreakable", true);
        consume(player, stone);
        player.sendSystemMessage(Component.literal("✦ Vật phẩm đã trở nên Không Thể Bị Phá Hủy ✦").withStyle(ChatFormatting.GOLD));
        return InteractionResultHolder.success(stone);
    }

    private static void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Ép vào vật phẩm để làm nó không thể bị phá hủy").withStyle(ChatFormatting.GRAY));
    }
}
