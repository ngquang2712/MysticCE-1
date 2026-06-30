package com.example.examplemod.expansion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class TuLinhHelper {
    public static int findLevel(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        int best = 0;
        int radius = 50;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -20, -radius), center.offset(radius, 20, radius))) {
            if (level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon) {
                int lv = beacon.getPersistentData().getInt("MysticTuLinhLevel");
                if (lv > best) best = lv;
            }
        }
        return Math.max(0, Math.min(3, best));
    }

    public static long applyMultiplier(ServerPlayer player, long base) {
        int lv = findLevel(player);
        if (lv <= 0) return base;
        double mult = switch (lv) { case 1 -> 1.5D; case 2 -> 2.0D; default -> 3.0D; };
        return Math.max(base, Math.round(base * mult));
    }
}
