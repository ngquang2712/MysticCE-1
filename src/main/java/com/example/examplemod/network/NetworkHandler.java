package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ExampleMod.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static boolean registered = false;

    private NetworkHandler() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(packetId++, PacketCultivationTeleport.class,
                PacketCultivationTeleport::encode,
                PacketCultivationTeleport::decode,
                PacketCultivationTeleport::handle);
    }

    public static void sendTeleportRequest() {
        CHANNEL.sendToServer(new PacketCultivationTeleport());
    }
}
