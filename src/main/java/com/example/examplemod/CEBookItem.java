package com.example.examplemod;

import com.example.examplemod.ce.CERarity;
import com.example.examplemod.ce.CERollUtil;
import com.example.examplemod.ce.CEType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

public class CEBookItem extends Item {

    private static final int MAX_CE_PER_ITEM = 5;

    public CEBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack ceBook = player.getItemInHand(hand);

        if (!level.isClientSide) {

            if (!ceBook.hasTag() || !ceBook.getTag().contains("CE_ID")) {
                player.sendSystemMessage(
                        Component.literal("Sách CE không hợp lệ!")
                                .withStyle(ChatFormatting.RED)
                );

                return InteractionResultHolder.fail(ceBook);
            }

            ItemStack targetItem = player.getMainHandItem();

            if (targetItem == ceBook) {
                targetItem = player.getOffhandItem();
            }

            if (targetItem.isEmpty()) {
                player.sendSystemMessage(
                        Component.literal("Hãy cầm item cần ép ở tay còn lại!")
                                .withStyle(ChatFormatting.RED)
                );

                return InteractionResultHolder.fail(ceBook);
            }

            CompoundTag bookTag = ceBook.getTag();

            String ceId = bookTag.getString("CE_ID");
            int ceLevel = bookTag.getInt("CE_LEVEL");
            int successRate = bookTag.getInt("SUCCESS_RATE");
            int destroyRate = bookTag.getInt("DESTROY_RATE");

            CEType ceType;

            try {
                ceType = CEType.valueOf(ceId);
            } catch (IllegalArgumentException e) {
                player.sendSystemMessage(
                        Component.literal("Sách CE lỗi dữ liệu!")
                                .withStyle(ChatFormatting.RED)
                );

                return InteractionResultHolder.fail(ceBook);
            }

            if (!ceType.canApply(targetItem)) {
                player.sendSystemMessage(
                        Component.literal("CE này không thể ép lên item đó!")
                                .withStyle(ChatFormatting.RED)
                );

                return InteractionResultHolder.fail(ceBook);
            }

            CompoundTag targetTag = targetItem.getOrCreateTag();

            ListTag ceList;

            if (targetTag.contains("CE_LIST", Tag.TAG_LIST)) {
                ceList = targetTag.getList("CE_LIST", Tag.TAG_COMPOUND);
            } else {
                ceList = new ListTag();
            }

            boolean success = CERollUtil.roll(successRate);

            if (success) {

                boolean upgraded = false;

                for (int i = 0; i < ceList.size(); i++) {

                    CompoundTag existingCE = ceList.getCompound(i);

                    if (existingCE.getString("CE_ID").equals(ceId)) {

                        int currentLevel = existingCE.getInt("CE_LEVEL");

                        if (currentLevel >= ceType.getMaxLevel()) {
                            player.sendSystemMessage(
                                    Component.literal("CE đã đạt cấp tối đa!")
                                            .withStyle(ChatFormatting.YELLOW)
                            );

                            return InteractionResultHolder.fail(ceBook);
                        }

                        if (currentLevel != ceLevel) {
                            player.sendSystemMessage(
                                    Component.literal("Cần sách cùng cấp để nâng cấp CE!")
                                            .withStyle(ChatFormatting.RED)
                            );

                            return InteractionResultHolder.fail(ceBook);
                        }

                        int newLevel = currentLevel + 1;

                        existingCE.putInt("CE_LEVEL", newLevel);

                        player.sendSystemMessage(
                                Component.literal(
                                                "Nâng cấp thành công "
                                                        + ceType.getDisplayName()
                                                        + " "
                                                        + toRoman(newLevel)
                                        )
                                        .withStyle(ChatFormatting.GREEN)
                        );

                        upgraded = true;
                        break;
                    }
                }

                if (!upgraded) {

                    if (ceList.size() >= MAX_CE_PER_ITEM) {
                        player.sendSystemMessage(
                                Component.literal("Item đã đầy CE!")
                                        .withStyle(ChatFormatting.RED)
                        );

                        return InteractionResultHolder.fail(ceBook);
                    }

                    CompoundTag ceData = new CompoundTag();
                    ceData.putString("CE_ID", ceId);
                    ceData.putInt("CE_LEVEL", ceLevel);

                    ceList.add(ceData);

                    player.sendSystemMessage(
                            Component.literal(
                                            "Ép CE thành công: "
                                                    + ceType.getDisplayName()
                                                    + " "
                                                    + toRoman(ceLevel)
                                    )
                                    .withStyle(ChatFormatting.GREEN)
                    );
                }

                targetTag.put("CE_LIST", ceList);

            } else {

                player.sendSystemMessage(
                        Component.literal("Ép CE thất bại!")
                                .withStyle(ChatFormatting.RED)
                );

                boolean destroy = CERollUtil.roll(destroyRate);

                if (destroy) {
                    targetItem.shrink(1);

                    player.sendSystemMessage(
                            Component.literal("Vật phẩm đã bị phá hủy!")
                                    .withStyle(ChatFormatting.DARK_RED)
                    );
                }
            }

            ceBook.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(
                ceBook,
                level.isClientSide()
        );
    }

    @Override
    public Component getName(ItemStack stack) {

        if (stack.hasTag() && stack.getTag().contains("CE_ID")) {

            CEType ceType;

            try {
                ceType = CEType.valueOf(
                        stack.getTag().getString("CE_ID")
                );
            } catch (IllegalArgumentException e) {
                return Component.literal("CE Book lỗi")
                        .withStyle(ChatFormatting.RED);
            }

            int ceLevel = stack.getTag().getInt("CE_LEVEL");

            return Component.literal(
                            ceType.getDisplayName() + " " + toRoman(ceLevel)
                    )
                    .withStyle(ChatFormatting.GOLD);
        }

        return Component.literal("CE Book");
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {

        if (!stack.hasTag()) return;
        if (!stack.getTag().contains("CE_ID")) return;

        CEType ceType;

        try {
            ceType = CEType.valueOf(
                    stack.getTag().getString("CE_ID")
            );
        } catch (IllegalArgumentException e) {
            tooltip.add(
                    Component.literal("Sách CE lỗi dữ liệu!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        CERarity rarity = CERarity.valueOf(
                stack.getTag().getString("CE_RARITY")
        );

        int ceLevel = stack.getTag().getInt("CE_LEVEL");
        int successRate = stack.getTag().getInt("SUCCESS_RATE");
        int destroyRate = stack.getTag().getInt("DESTROY_RATE");

        tooltip.add(
                Component.literal("Loại: " + ceType.getDisplayName())
                        .withStyle(ChatFormatting.GRAY)
        );

        tooltip.add(
                Component.literal("Độ hiếm: " + rarity.getDisplayName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
        );

        tooltip.add(
                Component.literal("Cấp độ: " + toRoman(ceLevel))
                        .withStyle(ChatFormatting.AQUA)
        );

        tooltip.add(
                Component.literal("Tỉ lệ ép: " + successRate + "%")
                        .withStyle(ChatFormatting.GREEN)
        );

        tooltip.add(
                Component.literal("Tỉ lệ phá: " + destroyRate + "%")
                        .withStyle(ChatFormatting.RED)
        );

        tooltip.add(
                Component.literal("Nâng cấp: cần 2 CE cùng loại, cùng cấp")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    private static String toRoman(int level) {

        return switch (level) {

            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";

            default -> String.valueOf(level);
        };
    }
}