package com.example.examplemod.boss;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MysticBossDrops {
    private static final Random RANDOM = new Random();

    private MysticBossDrops() {}

    public static List<Item> rollPills(MysticBossKind kind) {
        int min = kind.getGrade().getMinDrops();
        int max = kind.getGrade().getMaxDrops();
        int amount = min + RANDOM.nextInt(Math.max(1, max - min + 1));
        List<Item> result = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            result.add(rollOne(kind));
        }
        return result;
    }

    private static Item rollOne(MysticBossKind kind) {
        int r = RANDOM.nextInt(100);
        return switch (kind) {
            case HO_PHAP_THUONG_CO -> {
                if (r < 55) yield ExampleMod.TU_KHI_DAN.get();
                if (r < 85) yield ExampleMod.NGUNG_KHI_DAN.get();
                if (r < 97) yield ExampleMod.LINH_NGUYEN_DAN.get();
                yield ExampleMod.HUYEN_NGUYEN_DAN.get();
            }
            case THIEN_DAO_HOA_THAN -> {
                if (r < 20) yield ExampleMod.NGUNG_KHI_DAN.get();
                if (r < 60) yield ExampleMod.LINH_NGUYEN_DAN.get();
                if (r < 85) yield ExampleMod.HUYEN_NGUYEN_DAN.get();
                if (r < 97) yield ExampleMod.DIA_NGUYEN_DAN.get();
                yield ExampleMod.THIEN_NGUYEN_DAN.get();
            }
            case CUU_TINH_CO_THAN -> {
                if (r < 5) yield ExampleMod.LINH_NGUYEN_DAN.get();
                if (r < 25) yield ExampleMod.HUYEN_NGUYEN_DAN.get();
                if (r < 60) yield ExampleMod.DIA_NGUYEN_DAN.get();
                if (r < 85) yield ExampleMod.THIEN_NGUYEN_DAN.get();
                if (r < 97) yield ExampleMod.DAO_NGUYEN_DAN.get();
                yield ExampleMod.AM_DUONG_DAN.get();
            }
            case TIEN_DE_THANH_LAM -> {
                if (r < 5) yield ExampleMod.HUYEN_NGUYEN_DAN.get();
                if (r < 25) yield ExampleMod.DIA_NGUYEN_DAN.get();
                if (r < 60) yield ExampleMod.THIEN_NGUYEN_DAN.get();
                if (r < 90) yield ExampleMod.DAO_NGUYEN_DAN.get();
                yield ExampleMod.DAI_DAO_DAN.get();
            }
        };
    }
}
