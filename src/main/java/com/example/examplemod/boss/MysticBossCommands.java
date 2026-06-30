package com.example.examplemod.boss;

import com.example.examplemod.ExampleMod;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class MysticBossCommands {
    private MysticBossCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("summonboss")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("boss", StringArgumentType.word())
                                .executes(context -> summon(
                                        context.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(context, "boss")
                                )))
        );
    }

    private static int summon(ServerPlayer player, String bossId) {
        if (!(player.level() instanceof ServerLevel level)) return 0;
        MysticBossKind kind = MysticBossKind.byId(bossId);
        MysticBossEntity boss = ExampleMod.MYSTIC_BOSS.get().create(level);
        if (boss == null) return 0;
        boss.setBossKind(kind);
        boss.moveTo(player.getX() + 2.0D, player.getY(), player.getZ() + 2.0D, player.getYRot(), 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.COMMAND, null, null);
        level.addFreshEntity(boss);
        boss.setupBossStats();
        player.sendSystemMessage(Component.literal("§aĐã summon boss: ").append(Component.literal(kind.getDisplayName()).withStyle(kind.getColor(), ChatFormatting.BOLD)));
        return 1;
    }
}
