package com.example.examplemod.mystic;

import com.example.examplemod.CEBookItem;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.ce.CEType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class WeaponStoneItem extends Item {
    public enum StoneType { KILL_TRACKER, BEHEADING, CE_REMOVER, FIRE_REMOVER }

    public static final String KILL_TRACKER = "MysticKillTracker";
    public static final String MOB_KILLS = "MysticMobKills";
    public static final String PLAYER_KILLS = "MysticPlayerKills";
    public static final String BEHEADING = "MysticBeheading";

    private final StoneType type;

    public WeaponStoneItem(StoneType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stone = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stone, true);

        if (type == StoneType.FIRE_REMOVER) {
            return removeFire(level, player, stone);
        }

        ItemStack target = player.getOffhandItem();
        if (target.isEmpty() || target == stone) target = player.getMainHandItem();
        if (target == stone) target = ItemStack.EMPTY;

        if (type == StoneType.CE_REMOVER) {
            return removeCE(level, player, stone, target);
        }

        if (!isWeapon(target)) {
            player.sendSystemMessage(Component.literal("Cần cầm kiếm/rìu/cung ở tay còn lại!").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stone);
        }

        CompoundTag tag = target.getOrCreateTag();
        if (type == StoneType.KILL_TRACKER) {
            if (tag.getBoolean(KILL_TRACKER)) {
                player.sendSystemMessage(Component.literal("Vũ khí đã có Ngọc Theo Dõi Kill!").withStyle(ChatFormatting.YELLOW));
                return InteractionResultHolder.fail(stone);
            }
            tag.putBoolean(KILL_TRACKER, true);
            tag.putInt(MOB_KILLS, 0);
            tag.putInt(PLAYER_KILLS, 0);
            consume(player, stone);
            player.sendSystemMessage(Component.literal("⊹⊱ Đã ép Ngọc Theo Dõi Kill ⊰⊹").withStyle(ChatFormatting.GOLD));
            return InteractionResultHolder.success(stone);
        }

        if (type == StoneType.BEHEADING) {
            if (tag.getBoolean(BEHEADING)) {
                player.sendSystemMessage(Component.literal("Vũ khí đã có Trảm!").withStyle(ChatFormatting.YELLOW));
                return InteractionResultHolder.fail(stone);
            }
            tag.putBoolean(BEHEADING, true);
            consume(player, stone);
            player.sendSystemMessage(Component.literal("✟ Đã ép Trảm ✟").withStyle(ChatFormatting.DARK_RED));
            return InteractionResultHolder.success(stone);
        }

        return InteractionResultHolder.fail(stone);
    }

    private InteractionResultHolder<ItemStack> removeCE(Level level, Player player, ItemStack stone, ItemStack target) {
        if (target.isEmpty() || !target.hasTag() || !target.getTag().contains("CE_LIST", Tag.TAG_LIST)) {
            player.sendSystemMessage(Component.literal("Vũ khí không có CE để gỡ!").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stone);
        }
        ListTag ceList = target.getTag().getList("CE_LIST", Tag.TAG_COMPOUND);
        if (ceList.isEmpty()) {
            player.sendSystemMessage(Component.literal("Vũ khí không có CE để gỡ!").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stone);
        }

        CompoundTag removed = ceList.getCompound(0).copy();
        ceList.remove(0);
        target.getTag().put("CE_LIST", ceList);
        if (ceList.isEmpty()) target.getTag().remove("CE_LIST");

        ItemStack ceBook = new ItemStack(ExampleMod.CE_BOOK.get());
        CompoundTag bookTag = ceBook.getOrCreateTag();
        String ceId = removed.getString("CE_ID");
        int ceLevel = removed.getInt("CE_LEVEL");
        bookTag.putString("CE_ID", ceId);
        bookTag.putInt("CE_LEVEL", ceLevel);
        try {
            CEType ceType = CEType.valueOf(ceId);
            bookTag.putString("CE_RARITY", ceType.getRarity().name());
        } catch (IllegalArgumentException e) {
            bookTag.putString("CE_RARITY", "THACH_DAU");
        }
        bookTag.putInt("SUCCESS_RATE", 100);
        bookTag.putInt("DESTROY_RATE", 0);
        giveOrDrop(player, ceBook);
        consume(player, stone);
        player.sendSystemMessage(Component.literal("☣ Đã gỡ CE và trả sách về túi đồ!").withStyle(ChatFormatting.GREEN));
        return InteractionResultHolder.success(stone);
    }

    private InteractionResultHolder<ItemStack> removeFire(Level level, Player player, ItemStack stone) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack skull = player.getInventory().getItem(slot);
            if (!(skull.getItem() instanceof SkullItem) || !skull.hasTag()) continue;
            ListTag fireList = skull.getTag().getList(SkullItem.FIRE_LIST, Tag.TAG_COMPOUND);
            if (fireList.isEmpty()) continue;
            CompoundTag removed = fireList.getCompound(0).copy();
            fireList.remove(0);
            skull.getTag().put(SkullItem.FIRE_LIST, fireList);
            if (fireList.isEmpty()) skull.getTag().remove(SkullItem.FIRE_LIST);
            MagicFireType fireType = MagicFireType.valueOf(removed.getString("FIRE_ID"));
            giveOrDrop(player, new ItemStack(itemFromFire(fireType)));
            consume(player, stone);
            player.sendSystemMessage(Component.literal("✦ Đã gỡ Lửa Phép và trả về túi đồ!").withStyle(ChatFormatting.AQUA));
            return InteractionResultHolder.success(stone);
        }
        player.sendSystemMessage(Component.literal("Cần đặt Sọ có Lửa Phép trên hotbar!").withStyle(ChatFormatting.RED));
        return InteractionResultHolder.fail(stone);
    }

    private Item itemFromFire(MagicFireType fireType) {
        return switch (fireType) {
            case HEART_I -> ExampleMod.FIRE_HEART_I.get();
            case HEART_II -> ExampleMod.FIRE_HEART_II.get();
            case HEART_III -> ExampleMod.FIRE_HEART_III.get();
            case DAMAGE_I -> ExampleMod.FIRE_DAMAGE_I.get();
            case DAMAGE_II -> ExampleMod.FIRE_DAMAGE_II.get();
            case DAMAGE_III -> ExampleMod.FIRE_DAMAGE_III.get();
        };
    }

    private static boolean isWeapon(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem);
    }

    private static void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        switch (type) {
            case KILL_TRACKER -> tooltip.add(Component.literal("Ép lên kiếm/rìu/cung để theo dõi số Kill và Mobs").withStyle(ChatFormatting.GRAY));
            case BEHEADING -> tooltip.add(Component.literal("Ép lên kiếm/rìu/cung để lấy đầu đối phương").withStyle(ChatFormatting.GRAY));
            case CE_REMOVER -> tooltip.add(Component.literal("Gỡ CE trên cùng và trả sách CE về túi đồ").withStyle(ChatFormatting.GRAY));
            case FIRE_REMOVER -> tooltip.add(Component.literal("Gỡ Lửa Phép trên cùng trong Sọ và trả về túi đồ").withStyle(ChatFormatting.GRAY));
        }
    }

}
