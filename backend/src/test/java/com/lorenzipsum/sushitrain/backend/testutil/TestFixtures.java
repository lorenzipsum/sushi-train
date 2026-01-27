package com.lorenzipsum.sushitrain.backend.testutil;

import java.time.Instant;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.testutil.UuidV5.uuidV5;

public class TestFixtures {
    // Must match the namespace UUID used in your Flyway seed script.
    private static final UUID NAMESPACE = UUID.fromString("b39e5310-6f6d-5608-83eb-d714e752abbd");

    public static final String MAIN_BELT_NAME = "Main Belt";
    public static final UUID MAIN_BELT_ID = uuidV5(TestFixtures.NAMESPACE, "belt:" + MAIN_BELT_NAME);

    public static final String SALMON_NIGIRI_NAME = "Salmon Nigiri";
    public static final UUID SALMON_NIGIRI_ID = uuidV5(NAMESPACE, "menu_item:" + SALMON_NIGIRI_NAME);

    public static final String SALMON_NIGIRI = "Salmon Nigiri";
    public static final String CHICKEN_KARAAGE = "Chicken Karaage";
    public static final String MAGURO_NIGIRI = "Maguro Nigiri";

    public static Instant inTwoHours() {
        return Instant.now().plusSeconds(7200);
    }

}
