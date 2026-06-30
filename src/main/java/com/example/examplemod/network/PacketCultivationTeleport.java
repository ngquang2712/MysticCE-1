package com.example.examplemod.network;

import com.example.examplemod.cultivation.CultivationEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class PacketCultivationTeleport {
    public PacketCultivationTeleport() {}

    public static void encode(PacketCultivationTeleport msg, FriendlyByteBuf buf) {
    }

    public static PacketCultivationTeleport decode(FriendlyByteBuf buf) {
        return new PacketCultivationTeleport();
    }

    public static void handle(PacketCultivationTeleport msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                CultivationEvents.performStepTeleport(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
