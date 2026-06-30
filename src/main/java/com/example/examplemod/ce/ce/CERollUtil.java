package com.example.examplemod.ce;

import java.util.Random;

public class CERollUtil {

    private static final Random RANDOM = new Random();

    public static boolean roll(int chance) {
        return RANDOM.nextInt(100) < chance;
    }
}