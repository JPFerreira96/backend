package com.acme.card.utils;

public final class CardNumberGenerator {
    private static final java.util.Random RANDOM = new java.util.Random();

    private CardNumberGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static String cardNumberRandomGenerator() {
        int bloco1 = 10 + RANDOM.nextInt(89);
        int bloco2 = 1_000_0000 + RANDOM.nextInt(9_000_0000);
        int dv = RANDOM.nextInt(10);
        return String.format("90.%02d.%08d-%d", bloco1, bloco2, dv);
    }
}
