package com.example.examplemod.rename;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class ColorTextUtil {

    public static Component parse(String input) {
        MutableComponent result = Component.empty();

        ChatFormatting currentColor = ChatFormatting.WHITE;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strike = false;
        boolean obfuscated = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '&' && i + 1 < input.length()) {
                ChatFormatting fmt = getFormat(input.charAt(i + 1));

                if (fmt != null) {
                    if (fmt == ChatFormatting.RESET) {
                        currentColor = ChatFormatting.WHITE;
                        bold = false;
                        italic = false;
                        underline = false;
                        strike = false;
                        obfuscated = false;
                    } else if (fmt.isColor()) {
                        currentColor = fmt;
                    } else {
                        switch (fmt) {
                            case BOLD -> bold = true;
                            case ITALIC -> italic = true;
                            case UNDERLINE -> underline = true;
                            case STRIKETHROUGH -> strike = true;
                            case OBFUSCATED -> obfuscated = true;
                            default -> {
                            }
                        }
                    }

                    i++;
                    continue;
                }
            }

            Style style = Style.EMPTY.withColor(currentColor);

            if (bold) style = style.withBold(true);
            if (italic) style = style.withItalic(true);
            if (underline) style = style.withUnderlined(true);
            if (strike) style = style.withStrikethrough(true);
            if (obfuscated) style = style.withObfuscated(true);

            result.append(Component.literal(String.valueOf(c)).setStyle(style));
        }

        return result;
    }

    private static ChatFormatting getFormat(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> ChatFormatting.BLACK;
            case '1' -> ChatFormatting.DARK_BLUE;
            case '2' -> ChatFormatting.DARK_GREEN;
            case '3' -> ChatFormatting.DARK_AQUA;
            case '4' -> ChatFormatting.DARK_RED;
            case '5' -> ChatFormatting.DARK_PURPLE;
            case '6' -> ChatFormatting.GOLD;
            case '7' -> ChatFormatting.GRAY;
            case '8' -> ChatFormatting.DARK_GRAY;
            case '9' -> ChatFormatting.BLUE;
            case 'a' -> ChatFormatting.GREEN;
            case 'b' -> ChatFormatting.AQUA;
            case 'c' -> ChatFormatting.RED;
            case 'd' -> ChatFormatting.LIGHT_PURPLE;
            case 'e' -> ChatFormatting.YELLOW;
            case 'f' -> ChatFormatting.WHITE;

            case 'l' -> ChatFormatting.BOLD;
            case 'o' -> ChatFormatting.ITALIC;
            case 'n' -> ChatFormatting.UNDERLINE;
            case 'm' -> ChatFormatting.STRIKETHROUGH;
            case 'k' -> ChatFormatting.OBFUSCATED;
            case 'r' -> ChatFormatting.RESET;

            default -> null;
        };
    }
}