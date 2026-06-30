package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CultivationKeyInputEvents {
    private CultivationKeyInputEvents() {}

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        while (CultivationKeyMappings.TELEPORT.consumeClick()) {
            NetworkHandler.sendTeleportRequest();
        }
    }
}
