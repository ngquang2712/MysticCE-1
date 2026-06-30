package com.example.examplemod.events;

import com.example.examplemod.ce.CEType;
import com.example.examplemod.mystic.WeaponStoneItem;
import com.example.examplemod.mystic.ShieldHelper;
import com.example.examplemod.mystic.ShieldEnhanceType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CETooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.hasTag()) return;

        boolean hasKillTracker = stack.getTag().getBoolean(WeaponStoneItem.KILL_TRACKER);
        boolean hasBeheading = stack.getTag().getBoolean(WeaponStoneItem.BEHEADING);
        boolean hasCE = stack.getTag().contains("CE_LIST", Tag.TAG_LIST)
                && !stack.getTag().getList("CE_LIST", Tag.TAG_COMPOUND).isEmpty();

        if (hasCE) {
            ListTag ceList = stack.getTag().getList("CE_LIST", Tag.TAG_COMPOUND);
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("✦ ✦ ✦ ").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal("MYSTIC CE").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .append(Component.literal(" ✦ ✦ ✦").withStyle(ChatFormatting.LIGHT_PURPLE)));
            event.getToolTip().add(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_GRAY));

            for (int i = 0; i < ceList.size(); i++) {
                CompoundTag ceData = ceList.getCompound(i);
                try {
                    CEType ceType = CEType.valueOf(ceData.getString("CE_ID"));
                    int level = ceData.getInt("CE_LEVEL");
                    event.getToolTip().add(getCELine(ceType, level));
                    event.getToolTip().add(getCEDescription(ceType, level));
                } catch (IllegalArgumentException ignored) {
                    event.getToolTip().add(Component.literal("- CE lỗi dữ liệu").withStyle(ChatFormatting.RED));
                }
            }

            event.getToolTip().add(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_GRAY));
            event.getToolTip().add(Component.literal("✦ " + ceList.size() + "/5 CE Đang Hoạt Động ✦").withStyle(ChatFormatting.GRAY));
        }

        if (hasBeheading) {
            if (hasCE) event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("✟ Trảm ✟").withStyle(ChatFormatting.RED));
        }

        ListTag shieldList = ShieldHelper.getEnhanceList(stack);
        if (!shieldList.isEmpty()) {
            if (hasCE || hasBeheading || hasKillTracker) event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("✦ ✦ ✦ ").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal("MẢNH CƯỜNG HÓA KHIÊN").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .append(Component.literal(" ✦ ✦ ✦").withStyle(ChatFormatting.LIGHT_PURPLE)));
            event.getToolTip().add(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_GRAY));

            for (int i = 0; i < shieldList.size(); i++) {
                String id = shieldList.getCompound(i).getString("ENHANCE_ID");
                try {
                    ShieldEnhanceType t = ShieldEnhanceType.valueOf(id);
                    event.getToolTip().add(Component.literal("✦ ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal("🛡 ").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(t.getDisplayName()).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)));
                    event.getToolTip().add(Component.literal("  +" + (int) t.getArmorBonus() + " giáp | +" + (int) (t.getHealthBonus() / 2.0D) + " tim").withStyle(ChatFormatting.GRAY));
                } catch (IllegalArgumentException ignored) {
                    event.getToolTip().add(Component.literal("- Mảnh lỗi").withStyle(ChatFormatting.RED));
                }
            }
            event.getToolTip().add(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (hasKillTracker) {
            int mobKills = stack.getTag().getInt(WeaponStoneItem.MOB_KILLS);
            int playerKills = stack.getTag().getInt(WeaponStoneItem.PLAYER_KILLS);
            int totalKills = mobKills + playerKills;
            if (hasCE || hasBeheading) event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("══════ ✦ ══════").withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.literal("⊹⊱ ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("Số Kill: ").withStyle(ChatFormatting.RED))
                    .append(Component.literal(String.valueOf(totalKills)).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" ⊰⊹").withStyle(ChatFormatting.YELLOW)));
            event.getToolTip().add(Component.literal("⊰⊹ ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("Số Mobs: ").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(String.valueOf(mobKills)).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" ⊰⊹").withStyle(ChatFormatting.YELLOW)));
            event.getToolTip().add(Component.literal("⊰⊹ ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("Số Player: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(Component.literal(String.valueOf(playerKills)).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" ⊰⊹").withStyle(ChatFormatting.YELLOW)));
            event.getToolTip().add(Component.literal("══════ ✦ ══════").withStyle(ChatFormatting.GOLD));
        }
    }

    private static Component getCELine(CEType ceType, int level) {
        String roman = toRoman(level);
        return switch (ceType) {
            case BONG_LANH -> ceLine("❄", "BỎNG LẠNH", roman, ChatFormatting.AQUA, ChatFormatting.WHITE);
            case HO_DAU -> ceLine("✘", "HỔ ĐẤU", roman, ChatFormatting.GOLD, ChatFormatting.YELLOW);
            case CO_DOC -> ceLine("☠", "CÔ ĐỘC", roman, ChatFormatting.DARK_GRAY, ChatFormatting.GRAY);
            case MA_SOI -> ceLine("☽", "MA SÓI", roman, ChatFormatting.DARK_RED, ChatFormatting.RED);
            case BAN_TIA -> ceLine("🎯", "BẮN TỈA", roman, ChatFormatting.YELLOW, ChatFormatting.GOLD);
            case AN_KHANG -> ceLine("🛡", "AN KHANG", roman, ChatFormatting.DARK_GREEN, ChatFormatting.GREEN);
            case BUA_YEU -> ceLine("💘", "BÙA YÊU", roman, ChatFormatting.LIGHT_PURPLE, ChatFormatting.LIGHT_PURPLE);
            case VAY_RONG -> ceLine("🐉", "VẢY RỒNG", roman, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE);
            case THIEN_MENH -> ceLine("👑", "THIÊN MỆNH", roman, ChatFormatting.GOLD, ChatFormatting.YELLOW);
            case QUY_CHAN -> ceLine("✦", "QUY CHÂN", roman, ChatFormatting.AQUA, ChatFormatting.WHITE);
            case VAN_KIEM -> ceLine("⚔", "VẠN KIẾM", roman, ChatFormatting.LIGHT_PURPLE, ChatFormatting.WHITE);
            case NHAT_NGUYET -> ceLine("☀", "NHẬT NGUYỆT", roman, ChatFormatting.YELLOW, ChatFormatting.GOLD);
            case PHA_KHONG -> ceLine("◎", "PHÁ KHÔNG", roman, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE);
            case KHAI_THIEN -> ceLine("🪓", "KHAI THIÊN", roman, ChatFormatting.RED, ChatFormatting.GOLD);
            case XA_KICH -> ceLine("🏹", "XẠ KÍCH", roman, ChatFormatting.AQUA, ChatFormatting.WHITE);
            case HOANG_BAO -> ceLine("♛", "HOÀNG BÀO", roman, ChatFormatting.GOLD, ChatFormatting.YELLOW);
        };
    }

    private static Component ceLine(String icon, String name, String roman, ChatFormatting iconColor, ChatFormatting nameColor) {
        return Component.literal("✦ ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(icon + " ").withStyle(iconColor))
                .append(Component.literal(name).withStyle(nameColor, ChatFormatting.BOLD))
                .append(Component.literal(" " + icon + " ").withStyle(iconColor))
                .append(Component.literal(roman).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
    }

    private static Component getCEDescription(CEType ceType, int level) {
        return switch (ceType) {
            case BONG_LANH -> Component.literal("  ❄ Gây sát thương chuẩn").withStyle(ChatFormatting.GRAY);
            case HO_DAU -> Component.literal("  🐯 Phản sát thương và cường hóa đòn sau").withStyle(ChatFormatting.GRAY);
            case CO_DOC -> Component.literal("  ☠ Giảm hồi phục của mục tiêu").withStyle(ChatFormatting.GRAY);
            case MA_SOI -> Component.literal("  🌙 Tăng sát thương vào ban đêm").withStyle(ChatFormatting.GRAY);
            case BAN_TIA -> Component.literal("  🎯 Xuyên " + getBanTiaArmor(level) + "% giáp | CD " + getBanTiaCooldown(level) + "s").withStyle(ChatFormatting.GRAY);
            case AN_KHANG -> Component.literal("  🛡 " + getAnKhangChance(level) + "% miễn nhiễm " + getAnKhangDuration(level) + "s | CD " + getAnKhangCooldown(level) + "s").withStyle(ChatFormatting.GRAY);
            case BUA_YEU -> Component.literal("  💘 " + getBuaYeuChance(level) + "% gây Mù + Chậm + Hoa mắt | CD " + getBuaYeuCooldown(level) + "s").withStyle(ChatFormatting.GRAY);
            case VAY_RONG -> Component.literal("  🐉 Dưới 50% HP: " + getVayRongChance(level) + "% +" + getVayRongPercent(level) + "% máu vàng, hồi máu | CD " + getVayRongCooldown(level) + "s").withStyle(ChatFormatting.GRAY);
            case THIEN_MENH -> Component.literal("  👑 Chí tử: máu vàng 100% HP, Speed III, Strength III | CD 200s").withStyle(ChatFormatting.GRAY);
            case QUY_CHAN -> Component.literal("  ✦ Dưới 20% HP: hồi đầy, xóa hiệu ứng xấu, đòn sau +50% | CD 120s").withStyle(ChatFormatting.GRAY);
            case VAN_KIEM -> Component.literal("  ⚔ 30% kích hoạt: gây thêm " + level + " nhịp sát thương, mỗi nhịp bằng sát thương vừa gây | CD 120s").withStyle(ChatFormatting.GRAY);
            case NHAT_NGUYET -> Component.literal("  ☀ 30% khi bị đánh: toàn bộ đòn đánh chí mạng " + getNhatNguyetDuration(level) + "s | CD 120s").withStyle(ChatFormatting.GRAY);
            case PHA_KHONG -> Component.literal("  ◎ Né hoàn toàn 1 đòn, dịch sau mục tiêu, đòn sau gây 300% | CD 120s").withStyle(ChatFormatting.GRAY);
            case KHAI_THIEN -> Component.literal("  🪓 Mỗi đòn gây thêm " + getKhaiThienPercent(level) + "% máu hiện tại của mục tiêu").withStyle(ChatFormatting.GRAY);
            case XA_KICH -> Component.literal("  🏹 Gây thêm " + getXaKichPercent(level) + "% máu tối đa của mục tiêu").withStyle(ChatFormatting.GRAY);
            case HOANG_BAO -> Component.literal("  ♛ " + getHoangBaoChance(level) + "% bỏ qua Đề Kháng của mục tiêu").withStyle(ChatFormatting.GRAY);
        };
    }

    private static int getBanTiaArmor(int level) { return switch (level) { case 1 -> 10; case 2 -> 20; case 3 -> 30; case 4 -> 40; case 5 -> 50; default -> 0; }; }
    private static int getBanTiaCooldown(int level) { return switch (level) { case 1 -> 60; case 2 -> 50; case 3 -> 40; case 4 -> 30; case 5 -> 20; default -> 0; }; }
    private static int getAnKhangChance(int level) { return switch (level) { case 1 -> 15; case 2 -> 20; case 3 -> 25; case 4 -> 30; case 5 -> 40; default -> 0; }; }
    private static int getAnKhangDuration(int level) { return switch (level) { case 1 -> 2; case 2 -> 3; case 3, 4 -> 4; case 5 -> 5; default -> 0; }; }
    private static int getAnKhangCooldown(int level) { return switch (level) { case 1, 2, 3 -> 120; case 4 -> 90; case 5 -> 60; default -> 0; }; }
    private static int getBuaYeuChance(int level) { return switch (level) { case 1 -> 15; case 2 -> 20; case 3 -> 30; default -> 0; }; }
    private static int getBuaYeuCooldown(int level) { return switch (level) { case 1 -> 120; case 2 -> 90; case 3 -> 60; default -> 0; }; }
    private static int getVayRongChance(int level) { return switch (level) { case 1 -> 15; case 2 -> 20; case 3 -> 30; default -> 0; }; }
    private static int getVayRongPercent(int level) { return switch (level) { case 1 -> 20; case 2 -> 30; case 3 -> 40; default -> 0; }; }
    private static int getVayRongCooldown(int level) { return switch (level) { case 1 -> 120; case 2 -> 90; case 3 -> 60; default -> 0; }; }
    private static int getNhatNguyetDuration(int level) { return switch (level) { case 1 -> 2; case 2 -> 3; case 3 -> 4; case 4 -> 5; case 5 -> 6; default -> 0; }; }
    private static int getKhaiThienPercent(int level) { return switch (level) { case 1 -> 2; case 2 -> 5; case 3 -> 10; default -> 0; }; }
    private static int getXaKichPercent(int level) { return switch (level) { case 1 -> 3; case 2 -> 5; case 3 -> 7; case 4 -> 9; case 5 -> 15; default -> 0; }; }
    private static int getHoangBaoChance(int level) { return switch (level) { case 1 -> 40; case 2 -> 50; case 3 -> 60; default -> 0; }; }

    private static String toRoman(int level) {
        return switch (level) { case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X"; default -> String.valueOf(level); };
    }
}
