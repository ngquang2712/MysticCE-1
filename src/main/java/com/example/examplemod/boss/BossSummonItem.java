package com.example.examplemod.boss;

import com.example.examplemod.ExampleMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BossSummonItem extends Item {
    private final MysticBossKind kind;

    public BossSummonItem(MysticBossKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public MysticBossKind getKind() {
        return kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.PASS;
        }
        if (!kind.canSummonByItem()) {
            player.sendSystemMessage(Component.literal("§cBoss này không thể triệu hồi bằng lệnh bài."));
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        MysticBossEntity boss = ExampleMod.MYSTIC_BOSS.get().create(serverLevel);
        if (boss == null) {
            return InteractionResult.FAIL;
        }
        boss.setBossKind(kind);
        boss.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, player.getYRot(), 0.0F);
        boss.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        serverLevel.addFreshEntity(boss);
        boss.setupBossStats();
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("✦ ").withStyle(kind.getColor(), ChatFormatting.BOLD)
                        .append(Component.literal(kind.getDisplayName()).withStyle(kind.getColor(), ChatFormatting.BOLD))
                        .append(Component.literal(" đã được triệu hồi bởi ").withStyle(ChatFormatting.GRAY))
                        .append(player.getDisplayName())
                        .append(Component.literal("! ✦").withStyle(kind.getColor(), ChatFormatting.BOLD)),
                false
        );
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Lệnh bài triệu hồi boss").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Boss: ").withStyle(ChatFormatting.GRAY).append(Component.literal(kind.getDisplayName()).withStyle(kind.getColor(), ChatFormatting.BOLD)));
        tooltip.add(Component.literal("Chuột phải xuống đất để triệu hồi.").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
