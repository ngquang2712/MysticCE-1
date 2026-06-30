package com.example.examplemod.expansion;

import com.example.examplemod.ExampleMod;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.registries.RegistryObject;

public class ModExpansion {
    public static void init() {}

    private static RegistryObject<Item> item(String id) {
        return ExampleMod.ITEMS.register(id, () -> new Item(new Item.Properties().stacksTo(64)));
    }

    private static RegistryObject<Item> material(String id, ColoredMaterialItem.Kind kind, String name) {
        return ExampleMod.ITEMS.register(id, () -> new ColoredMaterialItem(kind, name, new Item.Properties().stacksTo(64)));
    }

    private static RegistryObject<Item> material(String id, ColoredMaterialItem.Kind kind, String name, ChatFormatting color) {
        return ExampleMod.ITEMS.register(id, () -> new ColoredMaterialItem(kind, name, color, new Item.Properties().stacksTo(64)));
    }

    // 4 mảnh cường hóa
    public static final RegistryObject<Item> MANH_CUONG_HOA_SAC_BEN = material("manh_cuong_hoa_sac_ben", ColoredMaterialItem.Kind.ENHANCE, "Mảnh Cường Hóa Sắc Bén");
    public static final RegistryObject<Item> MANH_CUONG_HOA_POWER = material("manh_cuong_hoa_power", ColoredMaterialItem.Kind.ENHANCE, "Mảnh Cường Hóa Power");
    public static final RegistryObject<Item> MANH_CUONG_HOA_GIAP = material("manh_cuong_hoa_giap", ColoredMaterialItem.Kind.ENHANCE, "Mảnh Cường Hóa Giáp");
    public static final RegistryObject<Item> MANH_CUONG_HOA_HIEU_SUAT = material("manh_cuong_hoa_hieu_suat", ColoredMaterialItem.Kind.ENHANCE, "Mảnh Cường Hóa Hiệu Suất");

    // 10 linh thảo
    public static final RegistryObject<Item> LINH_THAO = material("linh_thao", ColoredMaterialItem.Kind.HERB, "Linh Thảo");
    public static final RegistryObject<Item> HUYET_LINH_THAO = material("huyet_linh_thao", ColoredMaterialItem.Kind.HERB, "Huyết Linh Thảo");
    public static final RegistryObject<Item> HOA_LINH_THAO = material("hoa_linh_thao", ColoredMaterialItem.Kind.HERB, "Hỏa Linh Thảo");
    public static final RegistryObject<Item> HAN_LINH_THAO = material("han_linh_thao", ColoredMaterialItem.Kind.HERB, "Hàn Linh Thảo");
    public static final RegistryObject<Item> THIEN_LINH_THAO = material("thien_linh_thao", ColoredMaterialItem.Kind.HERB, "Thiên Linh Thảo", ChatFormatting.AQUA);
    public static final RegistryObject<Item> DIA_LINH_THAO = material("dia_linh_thao", ColoredMaterialItem.Kind.HERB, "Địa Linh Thảo");
    public static final RegistryObject<Item> TU_LINH_THAO = material("tu_linh_thao", ColoredMaterialItem.Kind.HERB, "Tử Linh Thảo", ChatFormatting.DARK_PURPLE);
    public static final RegistryObject<Item> CUU_DIEP_LINH_CHI = material("cuu_diep_linh_chi", ColoredMaterialItem.Kind.HERB, "Cửu Diệp Linh Chi", ChatFormatting.LIGHT_PURPLE);
    public static final RegistryObject<Item> THAT_THAI_LIEN = material("that_thai_lien", ColoredMaterialItem.Kind.HERB, "Thất Thải Liên", ChatFormatting.LIGHT_PURPLE);
    public static final RegistryObject<Item> BANG_TAM_THAO = material("bang_tam_thao", ColoredMaterialItem.Kind.HERB, "Băng Tâm Thảo", ChatFormatting.AQUA);
    public static final RegistryObject<Item> LINH_TUYEN = material("linh_tuyen", ColoredMaterialItem.Kind.LIQUID, "Linh Tuyền");

    // 6 nguyên liệu quái
    public static final RegistryObject<Item> YEU_DAN = material("yeu_dan", ColoredMaterialItem.Kind.MOB, "Yêu Đan");
    public static final RegistryObject<Item> MA_HACH = material("ma_hach", ColoredMaterialItem.Kind.MOB, "Ma Hạch");
    public static final RegistryObject<Item> LINH_HON_KET_TINH = material("linh_hon_ket_tinh", ColoredMaterialItem.Kind.MOB, "Linh Hồn Kết Tinh");
    public static final RegistryObject<Item> HUYET_TINH = material("huyet_tinh", ColoredMaterialItem.Kind.MOB, "Huyết Tinh");
    public static final RegistryObject<Item> YEU_COT = material("yeu_cot", ColoredMaterialItem.Kind.MOB, "Yêu Cốt");
    public static final RegistryObject<Item> YEU_TINH_HOA = material("yeu_tinh_hoa", ColoredMaterialItem.Kind.MOB, "Yêu Tinh Hoa");

    // 6 nguyên liệu boss
    public static final RegistryObject<Item> THAN_COT = material("than_cot", ColoredMaterialItem.Kind.BOSS, "Thần Cốt");
    public static final RegistryObject<Item> THAN_HUYET = material("than_huyet", ColoredMaterialItem.Kind.BOSS, "Thần Huyết");
    public static final RegistryObject<Item> THIEN_DAO_KET_TINH = material("thien_dao_ket_tinh", ColoredMaterialItem.Kind.BOSS, "Thiên Đạo Kết Tinh");
    public static final RegistryObject<Item> THUONG_CO_CHI_TAM = material("thuong_co_chi_tam", ColoredMaterialItem.Kind.BOSS, "Thượng Cổ Chi Tâm");
    public static final RegistryObject<Item> HON_DON_TINH_THACH = material("hon_don_tinh_thach", ColoredMaterialItem.Kind.BOSS, "Hỗn Độn Tinh Thạch");
    public static final RegistryObject<Item> BAN_NGUYEN_CHI_KHI = material("ban_nguyen_chi_khi", ColoredMaterialItem.Kind.BOSS, "Bản Nguyên Chi Khí", ChatFormatting.RED);

    public static final RegistryObject<Item> TINH_THACH_HA_PHAM = material("tinh_thach_ha_pham", ColoredMaterialItem.Kind.ARRAY, "Tinh Thạch Hạ Phẩm");
    public static final RegistryObject<Item> TINH_THACH_TRUNG_PHAM = material("tinh_thach_trung_pham", ColoredMaterialItem.Kind.ARRAY, "Tinh Thạch Trung Phẩm");
    public static final RegistryObject<Item> TINH_THACH_THUONG_PHAM = material("tinh_thach_thuong_pham", ColoredMaterialItem.Kind.ARRAY, "Tinh Thạch Thượng Phẩm", ChatFormatting.GOLD);

    public static void registerBrewingRecipes() {
        // Luyện đan được xử lý bởi AlchemyBrewingHandler để hỗ trợ công thức nhiều ô + tỉ lệ thành công.
        // Recipe này chỉ giúp Brewing Stand cho phép đặt item tu tiên vào các ô hợp lệ, không tự sinh output vanilla.
        BrewingRecipeRegistry.addRecipe(new AlchemySlotUnlockRecipe());
    }

    private static class AlchemySlotUnlockRecipe implements IBrewingRecipe {
        @Override
        public boolean isInput(ItemStack stack) {
            if (stack.isEmpty()) return false;
            Item item = stack.getItem();
            return item == LINH_THAO.get()
                    || item == HUYET_LINH_THAO.get()
                    || item == HOA_LINH_THAO.get()
                    || item == HAN_LINH_THAO.get()
                    || item == THIEN_LINH_THAO.get()
                    || item == DIA_LINH_THAO.get()
                    || item == TU_LINH_THAO.get()
                    || item == CUU_DIEP_LINH_CHI.get()
                    || item == THAT_THAI_LIEN.get()
                    || item == BANG_TAM_THAO.get()
                    || item == LINH_TUYEN.get()
                    || item == YEU_DAN.get()
                    || item == MA_HACH.get()
                    || item == LINH_HON_KET_TINH.get()
                    || item == HUYET_TINH.get()
                    || item == YEU_COT.get()
                    || item == THAN_HUYET.get()
                    || item == THIEN_DAO_KET_TINH.get()
                    || item == THUONG_CO_CHI_TAM.get()
                    || item == HON_DON_TINH_THACH.get()
                    || item == BAN_NGUYEN_CHI_KHI.get()
                    || item == THAN_COT.get()
                    || item == ExampleMod.VAY_LONG_DE.get()
                    || item == ExampleMod.SUNG_KI_LAN.get()
                    || item == ExampleMod.LONG_PHUONG_NGU_SAC.get()
                    || item == ExampleMod.MANH_VO_THUONG_CO.get()
                    || item == ExampleMod.THIEN_DAO_HOA_THAN_LENH_BAI.get()
                    || item == ExampleMod.HO_PHAP_THUONG_CO_LENH_BAI.get()
                    || item == ExampleMod.TU_KHI_DAN.get()
                    || item == ExampleMod.NGUNG_KHI_DAN.get()
                    || item == ExampleMod.LINH_NGUYEN_DAN.get()
                    || item == ExampleMod.DIA_NGUYEN_DAN.get()
                    || item == ExampleMod.THIEN_NGUYEN_DAN.get()
                    || item == ExampleMod.DE_NGUYEN_DAN.get()
                    || item == ExampleMod.DAO_NGUYEN_DAN.get()
                    || item == ExampleMod.HUYEN_NGUYEN_DAN.get()
                    || item == ExampleMod.TIEN_NGUYEN_DAN.get()
                    || item == ExampleMod.HON_NGUYEN_DAN.get()
                    || item == ExampleMod.DAI_DAO_DAN.get()
                    || item == ExampleMod.AM_DUONG_DAN.get()
                    || item == ExampleMod.NIET_BAN_DAN.get()
                    || item == ExampleMod.DAI_NIET_BAN_DAN.get()
                    || item == ExampleMod.THIEN_MENH_DAN.get();
        }

        @Override
        public boolean isIngredient(ItemStack stack) {
            if (stack.isEmpty()) return false;
            Item item = stack.getItem();
            return item == YEU_DAN.get()
                    || item == MA_HACH.get()
                    || item == LINH_HON_KET_TINH.get()
                    || item == HUYET_TINH.get()
                    || item == YEU_COT.get()
                    || item == THAN_HUYET.get()
                    || item == THIEN_DAO_KET_TINH.get()
                    || item == THUONG_CO_CHI_TAM.get()
                    || item == HON_DON_TINH_THACH.get()
                    || item == BAN_NGUYEN_CHI_KHI.get()
                    || item == THAN_COT.get()
                    || item == ExampleMod.VAY_LONG_DE.get()
                    || item == ExampleMod.SUNG_KI_LAN.get()
                    || item == ExampleMod.LONG_PHUONG_NGU_SAC.get()
                    || item == ExampleMod.MANH_VO_THUONG_CO.get()
                    || item == ExampleMod.THIEN_DAO_HOA_THAN_LENH_BAI.get()
                    || item == ExampleMod.HO_PHAP_THUONG_CO_LENH_BAI.get();
        }

        @Override
        public ItemStack getOutput(ItemStack in, ItemStack ing) {
            return ItemStack.EMPTY;
        }
    }

    public static void addToCreative(CreativeModeTab.Output output) {
        output.accept(MANH_CUONG_HOA_SAC_BEN.get());
        output.accept(MANH_CUONG_HOA_POWER.get());
        output.accept(MANH_CUONG_HOA_GIAP.get());
        output.accept(MANH_CUONG_HOA_HIEU_SUAT.get());
        output.accept(LINH_THAO.get());
        output.accept(HUYET_LINH_THAO.get());
        output.accept(HOA_LINH_THAO.get());
        output.accept(HAN_LINH_THAO.get());
        output.accept(THIEN_LINH_THAO.get());
        output.accept(DIA_LINH_THAO.get());
        output.accept(TU_LINH_THAO.get());
        output.accept(CUU_DIEP_LINH_CHI.get());
        output.accept(THAT_THAI_LIEN.get());
        output.accept(BANG_TAM_THAO.get());
        output.accept(LINH_TUYEN.get());
        output.accept(YEU_DAN.get());
        output.accept(MA_HACH.get());
        output.accept(LINH_HON_KET_TINH.get());
        output.accept(HUYET_TINH.get());
        output.accept(YEU_COT.get());
        output.accept(YEU_TINH_HOA.get());
        output.accept(THAN_COT.get());
        output.accept(THAN_HUYET.get());
        output.accept(THIEN_DAO_KET_TINH.get());
        output.accept(THUONG_CO_CHI_TAM.get());
        output.accept(HON_DON_TINH_THACH.get());
        output.accept(BAN_NGUYEN_CHI_KHI.get());
        output.accept(TINH_THACH_HA_PHAM.get());
        output.accept(TINH_THACH_TRUNG_PHAM.get());
        output.accept(TINH_THACH_THUONG_PHAM.get());
    }
}
