package com.example.examplemod.cultivation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CultivationPillItem extends Item {
    private final CultivationPillType type;

    public CultivationPillItem(CultivationPillType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public CultivationPillType getType() {
        return type;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("◈ " + type.getDisplayName() + " ◈").withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (type.isProtection()) {
            CultivationData.setProtection(player, type.getDisplayName());
            player.sendSystemMessage(Component.literal("✦ Đã kích hoạt " + type.getDisplayName() + " cho lần độ kiếp tiếp theo.").withStyle(ChatFormatting.GOLD));
        } else {
            CultivationData.addLinhKhi(player, type.getLinhKhi());
            player.displayClientMessage(Component.literal("+" + CultivationUtil.formatNumber(type.getLinhKhi()) + " linh khí từ " + type.getDisplayName()).withStyle(ChatFormatting.AQUA), true);
            player.sendSystemMessage(Component.literal("✦ Dùng " + type.getDisplayName() + ": +" + CultivationUtil.formatNumber(type.getLinhKhi()) + " linh khí.").withStyle(ChatFormatting.AQUA));
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Đan dược tu tiên").withStyle(ChatFormatting.DARK_PURPLE));
        if (type.isProtection()) {
            tooltip.add(Component.literal("Công dụng khi độ kiếp thất bại:").withStyle(ChatFormatting.GRAY));
            switch (type) {
                case HO_MENH_DAN -> {
                    tooltip.add(Component.literal("- Vẫn chết").withStyle(ChatFormatting.RED));
                    tooltip.add(Component.literal("- Không tụt bậc").withStyle(ChatFormatting.GREEN));
                }
                case NIET_BAN_DAN -> {
                    tooltip.add(Component.literal("- Không chết, còn 1 tim").withStyle(ChatFormatting.GREEN));
                    tooltip.add(Component.literal("- Vẫn tụt 1 bậc").withStyle(ChatFormatting.YELLOW));
                }
                case DAI_NIET_BAN_DAN -> {
                    tooltip.add(Component.literal("- Không chết").withStyle(ChatFormatting.GREEN));
                    tooltip.add(Component.literal("- Không tụt bậc").withStyle(ChatFormatting.GREEN));
                }
                case THIEN_MENH_DAN -> {
                    tooltip.add(Component.literal("- 30% biến thất bại thành thành công").withStyle(ChatFormatting.LIGHT_PURPLE));
                    tooltip.add(Component.literal("- Nếu không kích hoạt: thất bại bình thường").withStyle(ChatFormatting.GRAY));
                }
                default -> {}
            }
            tooltip.add(Component.literal("Hiệu lực: 1 lần độ kiếp tiếp theo").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.literal("Công dụng: +" + CultivationUtil.formatNumber(type.getLinhKhi()) + " linh khí").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("Chuột phải để sử dụng").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
