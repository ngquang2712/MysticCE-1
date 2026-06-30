package com.example.examplemod.expansion;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.cultivation.CultivationData;
import com.example.examplemod.cultivation.CultivationRealm;
import com.example.examplemod.cultivation.CultivationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class ExpansionEvents {
    private static final Random RANDOM = new Random();
    private static final String GUIDE_GIVEN = "MysticGuideBookGiven";

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getPersistentData().getBoolean(GUIDE_GIVEN)) {
            player.getPersistentData().putBoolean(GUIDE_GIVEN, true);
            ItemStack book = makeGuideBook();
            if (!player.getInventory().add(book)) player.drop(book, false);
            player.sendSystemMessage(Component.literal("✦ Đã nhận Cửu Thiên Tu Tiên Quyển. Mở sách để xem wiki tu tiên bằng Patchouli.").withStyle(ChatFormatting.GOLD));
        }
    }

    private static ItemStack makeGuideBook() {
        Item patchouliBookItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("patchouli", "guide_book"));
        if (patchouliBookItem != null && patchouliBookItem != Items.AIR) {
            ItemStack book = new ItemStack(patchouliBookItem);
            CompoundTag tag = book.getOrCreateTag();
            tag.putString("patchouli:book", ExampleMod.MODID + ":cuu_thien_tu_tien_kinh");
            book.setHoverName(Component.literal("📖 Cửu Thiên Tu Tiên Quyển").withStyle(ChatFormatting.GOLD));
            return book;
        }

        // Fallback nếu server/client chưa cài Patchouli: vẫn phát Written Book để không mất hướng dẫn.
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "Cửu Thiên Tu Tiên Quyển");
        tag.putString("author", "MysticCE");
        ListTag pages = new ListTag();
        addPage(pages, "§lCỬU THIÊN TU TIÊN QUYỂN§r\n\nNếu có cài Patchouli, sách này sẽ hiện dạng guidebook có icon/công thức. Hiện tại Patchouli chưa được load nên dùng bản chữ tạm.");
        addPage(pages, "§lCẢNH GIỚI§r\n\nLuyện Khí → Trúc Cơ → Kim Đan → Nguyên Anh → Hóa Thần → Anh Biến → Vấn Đỉnh → Âm Hư → Dương Thực → Khuy Niết → Đạp Thiên → Tiên Đế.");
        addPage(pages, "§lLUYỆN ĐAN§r\n\nDùng Dàn Pha Chế. Ô 0+1 có thể đặt 2 đan cấp thấp, ô 2 đặt linh thảo, ô trên cùng đặt xúc tác. Đan càng cao tỉ lệ càng thấp.");
        addPage(pages, "§lLÒ RÈN§r\n\nDùng Đe + Mảnh Cường Hóa. Sắc Bén/Power/Giáp/Hiệu Suất tối đa 10 lần. Dùng /i để xem tỉ lệ ép.");
        addPage(pages, "§lBÍ CẢNH§r\n\n20 phút spawn Kẻ Gác Cổng. Giết nó, tập hợp 60 giây, vượt 4 đợt quái và boss cuối để nhận thưởng.");
        tag.put("pages", pages);
        return book;
    }


    private static void addPage(ListTag pages, String text) {
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(text))));
    }


    @SubscribeEvent
    public static void onBeaconRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BlockPos pos = event.getPos();
        if (!(event.getLevel().getBlockEntity(pos) instanceof BeaconBlockEntity beacon)) return;
        ItemStack held = event.getItemStack();
        int level = 0;
        if (held.is(ModExpansion.TINH_THACH_HA_PHAM.get())) level = 1;
        else if (held.is(ModExpansion.TINH_THACH_TRUNG_PHAM.get())) level = 2;
        else if (held.is(ModExpansion.TINH_THACH_THUONG_PHAM.get())) level = 3;
        if (level <= 0) return;
        beacon.getPersistentData().putInt("MysticTuLinhLevel", level);
        beacon.setChanged();
        if (!player.getAbilities().instabuild) held.shrink(1);
        player.sendSystemMessage(Component.literal("☯ Tụ Linh Trận cấp " + level + " đã khai trận. Người chơi /tu trong phạm vi sẽ nhận thêm linh khí.").withStyle(ChatFormatting.AQUA));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        EnhancementHelper.Type type = EnhancementHelper.typeFromMaterial(event.getRight());
        if (type == null) return;
        if (!EnhancementHelper.canApply(event.getLeft(), type)) return;
        int count = EnhancementHelper.getCount(event.getLeft(), type);
        if (count >= EnhancementHelper.MAX) {
            if (event.getPlayer() != null) {
                event.getPlayer().displayClientMessage(Component.literal("Đã max cường hóa " + type.display + " (10/10)").withStyle(ChatFormatting.RED), true);
            }
            return;
        }
        // Chỉ hiển thị bản gốc trong output.
        // Khi người chơi lấy đồ ra, AnvilRepairEvent mới random thành công/thất bại.
        // Làm vậy tránh lỗi dupe: thất bại sẽ giữ nguyên vũ khí, chỉ mất mảnh.
        event.setOutput(event.getLeft().copy());
        event.setCost(1 + count);
        event.setMaterialCost(1);
    }

    @SubscribeEvent
    public static void onAnvilRepair(AnvilRepairEvent event) {
        EnhancementHelper.Type type = EnhancementHelper.typeFromMaterial(event.getRight());
        if (type == null) return;
        Player player = event.getEntity();
        int count = EnhancementHelper.getCount(event.getLeft(), type);
        int chance = EnhancementHelper.chanceForNext(count);
        if (RANDOM.nextInt(100) >= chance) {
            // Output đã là bản gốc, nên thất bại KHÔNG thêm/trả thêm vũ khí.
            // Vanilla đã tiêu hao 1 mảnh cường hóa theo materialCost.
            player.sendSystemMessage(Component.literal("✘ Cường hóa thất bại! Mảnh cường hóa đã bị tiêu hao, vũ khí giữ nguyên.").withStyle(ChatFormatting.RED));
        } else {
            // Mutate trực tiếp output của anvil thành bản đã cường hóa.
            // Không add thêm ItemStack mới để tránh mọi trường hợp dupe.
            EnhancementHelper.applySuccess(event.getOutput(), type);
            player.sendSystemMessage(Component.literal("✔ Cường hóa thành công! " + type.display + " " + (count + 1) + "/10").withStyle(ChatFormatting.GREEN));
        }
    }


    @SubscribeEvent
    public static void onLivingHurtSkill(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (CultivationData.root(player).getBoolean("KiemKhiHoaHinhReady")) {
            CultivationData.root(player).putBoolean("KiemKhiHoaHinhReady", false);
            event.setAmount(event.getAmount() * 2.0F);
            player.sendSystemMessage(Component.literal("✦ Kiếm Khí Hóa Hình kích hoạt: sát thương x200%.").withStyle(ChatFormatting.AQUA));
        }
    }

    @SubscribeEvent
    public static void onMobDeathDrops(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) return;
        if (!(killer.level() instanceof ServerLevel level)) return;

        double roll = RANDOM.nextDouble();
        ItemStack drop = ItemStack.EMPTY;
        double hp = victim.getMaxHealth();
        if (roll < 0.035) drop = new ItemStack(ModExpansion.LINH_THAO.get());
        else if (roll < 0.055) drop = new ItemStack(ModExpansion.YEU_DAN.get());
        else if (roll < 0.070 && hp >= 40) drop = new ItemStack(ModExpansion.MA_HACH.get());
        else if (roll < 0.080 && hp >= 80) drop = new ItemStack(ModExpansion.LINH_HON_KET_TINH.get());
        if (!drop.isEmpty()) victim.spawnAtLocation(drop);
    }
}
