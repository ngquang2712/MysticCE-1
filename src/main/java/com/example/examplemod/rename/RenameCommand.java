package com.example.examplemod.rename;

import com.example.examplemod.ExampleMod;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RenameCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("renameitem")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrException();
                                    ItemStack target = player.getMainHandItem();

                                    if (target.isEmpty()) {
                                        player.sendSystemMessage(Component.literal("§cPhải cầm vật phẩm cần đổi tên trên tay chính!"));
                                        return 0;
                                    }

                                    String name = StringArgumentType.getString(context, "name");
                                    name = name.replace("\n", "").replace("\r", "");

                                    if (name.length() > 64) {
                                        player.sendSystemMessage(Component.literal("§cTên quá dài! Tối đa 64 ký tự."));
                                        return 0;
                                    }

                                    if (!player.isCreative()) {
                                        boolean consumed = consumeRenameScroll(player);

                                        if (!consumed) {
                                            player.sendSystemMessage(Component.literal("§cBạn cần có Thẻ Đổi Tên để dùng lệnh này!"));
                                            return 0;
                                        }
                                    }

                                    target.setHoverName(ColorTextUtil.parse(name));
                                    player.sendSystemMessage(Component.literal("§aĐã đổi tên vật phẩm!"));

                                    return 1;
                                })
                        )
        );
    }

    private static boolean consumeRenameScroll(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(ExampleMod.RENAME_SCROLL.get())) {
                stack.shrink(1);
                return true;
            }
        }

        return false;
    }
}