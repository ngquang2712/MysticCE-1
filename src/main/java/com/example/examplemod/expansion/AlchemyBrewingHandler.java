package com.example.examplemod.expansion;

import com.example.examplemod.ExampleMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class AlchemyBrewingHandler {
    private static int tickCounter = 0;

    private record AlchemyRecipe(Item first, int firstCount, Item herb, Item catalyst, Item output, int successRate, String name) {}

    private static List<AlchemyRecipe> recipes() {
        return List.of(
                new AlchemyRecipe(ModExpansion.LINH_THAO.get(), 1, ModExpansion.LINH_TUYEN.get(), ModExpansion.YEU_DAN.get(), ExampleMod.TU_KHI_DAN.get(), 100, "Tụ Khí Đan"),
                new AlchemyRecipe(ExampleMod.TU_KHI_DAN.get(), 2, ModExpansion.HUYET_LINH_THAO.get(), ModExpansion.MA_HACH.get(), ExampleMod.NGUNG_KHI_DAN.get(), 95, "Ngưng Khí Đan"),
                new AlchemyRecipe(ExampleMod.NGUNG_KHI_DAN.get(), 2, ModExpansion.DIA_LINH_THAO.get(), ModExpansion.LINH_HON_KET_TINH.get(), ExampleMod.LINH_NGUYEN_DAN.get(), 90, "Linh Nguyên Đan"),
                new AlchemyRecipe(ExampleMod.LINH_NGUYEN_DAN.get(), 2, ModExpansion.HOA_LINH_THAO.get(), ModExpansion.HUYET_TINH.get(), ExampleMod.DIA_NGUYEN_DAN.get(), 85, "Địa Nguyên Đan"),
                new AlchemyRecipe(ExampleMod.DIA_NGUYEN_DAN.get(), 2, ModExpansion.THIEN_LINH_THAO.get(), ModExpansion.YEU_COT.get(), ExampleMod.THIEN_NGUYEN_DAN.get(), 80, "Thiên Nguyên Đan"),
                new AlchemyRecipe(ExampleMod.THIEN_NGUYEN_DAN.get(), 2, ModExpansion.TU_LINH_THAO.get(), ExampleMod.VAY_LONG_DE.get(), ExampleMod.DE_NGUYEN_DAN.get(), 75, "Đế Nguyên Đan"),
                new AlchemyRecipe(ExampleMod.DE_NGUYEN_DAN.get(), 2, ModExpansion.CUU_DIEP_LINH_CHI.get(), ExampleMod.SUNG_KI_LAN.get(), ExampleMod.DAO_NGUYEN_DAN.get(), 70, "Đạo Nguyên Đan"),
                new AlchemyRecipe(ExampleMod.DAO_NGUYEN_DAN.get(), 2, ModExpansion.THAT_THAI_LIEN.get(), ExampleMod.LONG_PHUONG_NGU_SAC.get(), ExampleMod.HUYEN_NGUYEN_DAN.get(), 65, "Huyền Nguyên Đan"),
                new AlchemyRecipe(ExampleMod.HUYEN_NGUYEN_DAN.get(), 2, ModExpansion.BANG_TAM_THAO.get(), ExampleMod.MANH_VO_THUONG_CO.get(), ExampleMod.TIEN_NGUYEN_DAN.get(), 60, "Tiên Nguyên Đan"),
                new AlchemyRecipe(ExampleMod.TIEN_NGUYEN_DAN.get(), 2, ModExpansion.THIEN_DAO_KET_TINH.get(), ModExpansion.THAN_HUYET.get(), ExampleMod.HON_NGUYEN_DAN.get(), 55, "Hỗn Nguyên Đan"),
                new AlchemyRecipe(ExampleMod.HON_NGUYEN_DAN.get(), 2, ModExpansion.THUONG_CO_CHI_TAM.get(), ModExpansion.HON_DON_TINH_THACH.get(), ExampleMod.DAI_DAO_DAN.get(), 50, "Đại Đạo Đan"),
                new AlchemyRecipe(ExampleMod.DAI_DAO_DAN.get(), 2, ModExpansion.BAN_NGUYEN_CHI_KHI.get(), ExampleMod.THIEN_MENH_DAN.get(), ExampleMod.AM_DUONG_DAN.get(), 45, "Âm Dương Đan"),
                new AlchemyRecipe(ExampleMod.AM_DUONG_DAN.get(), 2, ModExpansion.THAN_COT.get(), ExampleMod.THIEN_DAO_HOA_THAN_LENH_BAI.get(), ExampleMod.NIET_BAN_DAN.get(), 40, "Niết Bàn Đan"),
                new AlchemyRecipe(ExampleMod.NIET_BAN_DAN.get(), 2, ExampleMod.HO_PHAP_THUONG_CO_LENH_BAI.get(), ModExpansion.THAN_HUYET.get(), ExampleMod.DAI_NIET_BAN_DAN.get(), 35, "Đại Niết Bàn Đan"),
                new AlchemyRecipe(ExampleMod.DAI_NIET_BAN_DAN.get(), 2, ModExpansion.BAN_NGUYEN_CHI_KHI.get(), ModExpansion.THUONG_CO_CHI_TAM.get(), ExampleMod.THIEN_MENH_DAN.get(), 30, "Thiên Mệnh Đan"),
                new AlchemyRecipe(ExampleMod.THIEN_MENH_DAN.get(), 2, ModExpansion.BAN_NGUYEN_CHI_KHI.get(), ModExpansion.HON_DON_TINH_THACH.get(), ExampleMod.HO_MENH_DAN.get(), 25, "Hộ Mệnh Đan")
        );
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter % 40 != 0) return;
        Set<String> visited = new HashSet<>();
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                BlockPos center = player.blockPosition();
                for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -4, -8), center.offset(8, 4, 8))) {
                    String key = level.dimension().location() + ":" + pos.asLong();
                    if (!visited.add(key)) continue;
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof BrewingStandBlockEntity stand) {
                        tryAlchemy(level, pos.immutable(), stand);
                    }
                }
            }
        }
    }

    private static void tryAlchemy(ServerLevel level, BlockPos pos, BrewingStandBlockEntity stand) {
        ItemStack slot0 = stand.getItem(0);
        ItemStack slot1 = stand.getItem(1);
        ItemStack slot2 = stand.getItem(2);
        ItemStack catalyst = stand.getItem(3);
        if (slot0.isEmpty() || catalyst.isEmpty()) return;
        for (AlchemyRecipe recipe : recipes()) {
            if (!matches(recipe, slot0, slot1, slot2, catalyst)) continue;
            boolean success = ThreadLocalRandom.current().nextInt(100) < recipe.successRate();
            consumeRecipeItems(stand, recipe);
            if (success) {
                stand.setItem(0, new ItemStack(recipe.output()));
                notifyNearby(level, pos, Component.literal("✔ Luyện thành công " + recipe.name() + " (" + recipe.successRate() + "%).").withStyle(ChatFormatting.GREEN));
            } else {
                stand.setItem(0, ItemStack.EMPTY);
                notifyNearby(level, pos, Component.literal("✘ Luyện " + recipe.name() + " thất bại! Nguyên liệu đã bị tiêu hao. Tỉ lệ: " + recipe.successRate() + "%.").withStyle(ChatFormatting.RED));
            }
            stand.setChanged();
            level.sendBlockUpdated(pos, stand.getBlockState(), stand.getBlockState(), 3);
            return;
        }
    }

    private static boolean matches(AlchemyRecipe recipe, ItemStack slot0, ItemStack slot1, ItemStack slot2, ItemStack catalyst) {
        if (!catalyst.is(recipe.catalyst())) return false;
        if (recipe.firstCount() == 1) {
            return slot0.is(recipe.first()) && slot1.is(recipe.herb()) && slot2.isEmpty();
        }
        return slot0.is(recipe.first()) && slot1.is(recipe.first()) && slot2.is(recipe.herb());
    }

    private static void consumeRecipeItems(BrewingStandBlockEntity stand, AlchemyRecipe recipe) {
        stand.getItem(0).shrink(1);
        if (recipe.firstCount() == 1) {
            stand.getItem(1).shrink(1);
        } else {
            stand.getItem(1).shrink(1);
            stand.getItem(2).shrink(1);
        }
        stand.getItem(3).shrink(1);
        for (int i = 0; i <= 3; i++) {
            if (stand.getItem(i).isEmpty()) stand.setItem(i, ItemStack.EMPTY);
        }
    }

    private static void notifyNearby(ServerLevel level, BlockPos pos, Component message) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distSqr(pos) <= 16 * 16) {
                player.sendSystemMessage(message);
            }
        }
    }
}
