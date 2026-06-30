package com.example.examplemod;

import com.example.examplemod.ce.CERarity;
import com.example.examplemod.ce.CEType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

public class RandomCEBookItem extends Item {

    private static final Random RANDOM = new Random();

    public RandomCEBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack randomBook = player.getItemInHand(hand);

        if (!level.isClientSide) {

            CEType[] ceTypes = CEType.values();

            CEType resultCE = ceTypes[
                    RANDOM.nextInt(ceTypes.length)
            ];

            int ceLevel = RANDOM.nextInt(resultCE.getMaxLevel()) + 1;
            int successRate = RANDOM.nextInt(41) + 50;
            int destroyRate = RANDOM.nextInt(21);

            ItemStack ceBook = new ItemStack(ExampleMod.CE_BOOK.get());

            ceBook.getOrCreateTag().putString("CE_ID", resultCE.name());
            ceBook.getOrCreateTag().putString("CE_RARITY", CERarity.THACH_DAU.name());
            ceBook.getOrCreateTag().putInt("CE_LEVEL", ceLevel);
            ceBook.getOrCreateTag().putInt("SUCCESS_RATE", successRate);
            ceBook.getOrCreateTag().putInt("DESTROY_RATE", destroyRate);

            if (!player.addItem(ceBook)) {
                player.drop(ceBook, false);
            }

            player.sendSystemMessage(
                    Component.literal(
                                    "Nhận được "
                                            + resultCE.getDisplayName()
                                            + " "
                                            + toRoman(ceLevel)
                            )
                            .withStyle(ChatFormatting.GREEN)
            );

            randomBook.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(
                randomBook,
                level.isClientSide()
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