package com.wmpj.smartcity360.core.streetlight.flyweight;

import com.wmpj.smartcity360.core.entity.StreetlightOperationalStatus;
import com.wmpj.smartcity360.core.entity.StreetlightType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class LightingProfileFlyweightFactoryTest {

    @Test
    void reusesOnlyEquivalentImmutableProfileConfigurations() {
        LightingProfileFlyweightFactory factory = new LightingProfileFlyweightFactory(16);
        LightingProfileDefinition original = definition("1.0", (short) 80);
        LightingProfileDefinition equivalent = definition("1.0", (short) 80);
        LightingProfileDefinition updated = definition("1.1", (short) 70);

        LightingProfileFlyweight first = factory.getOrCreate(original);
        LightingProfileFlyweight reused = factory.getOrCreate(equivalent);
        LightingProfileFlyweight refreshed = factory.getOrCreate(updated);

        assertSame(first, reused);
        assertNotSame(first, refreshed);
        assertEquals(2, factory.cachedProfileCount());
    }

    @Test
    void sharesOneInstanceUnderConcurrentAccess() throws Exception {
        LightingProfileFlyweightFactory factory = new LightingProfileFlyweightFactory(16);
        LightingProfileDefinition definition = definition("1.0", (short) 80);
        List<Callable<LightingProfileFlyweight>> tasks = new ArrayList<>();

        for (int index = 0; index < 20; index++) {
            tasks.add(() -> factory.getOrCreate(definition));
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            List<LightingProfileFlyweight> profiles = executor.invokeAll(tasks)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception exception) {
                            throw new IllegalStateException(exception);
                        }
                    })
                    .toList();

            for (LightingProfileFlyweight profile : profiles) {
                assertSame(profiles.get(0), profile);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void keepsStreetlightExtrinsicStateIndependent() {
        LightingProfileFlyweightFactory factory = new LightingProfileFlyweightFactory(16);
        LightingProfileFlyweight sharedProfile = factory.getOrCreate(
                definition("1.0", (short) 80)
        );
        StreetlightContext first = new StreetlightContext(
                10L,
                "LIGHT-10",
                4L,
                true,
                (short) 80,
                StreetlightOperationalStatus.ACTIVE,
                sharedProfile
        );
        StreetlightContext second = new StreetlightContext(
                11L,
                "LIGHT-11",
                4L,
                false,
                (short) 20,
                StreetlightOperationalStatus.MAINTENANCE,
                sharedProfile
        );

        assertSame(first.profile(), second.profile());
        assertEquals(true, first.on());
        assertEquals(false, second.on());
        assertEquals(80, first.brightness());
        assertEquals(20, second.brightness());
    }

    @Test
    void evictsLeastRecentlyUsedProfilesAtConfiguredBound() {
        LightingProfileFlyweightFactory factory = new LightingProfileFlyweightFactory(2);
        LightingProfileDefinition first = definition("1.0", (short) 80);
        LightingProfileDefinition second = definition("1.1", (short) 70);
        LightingProfileDefinition third = definition("1.2", (short) 60);
        LightingProfileFlyweight originalFirst = factory.getOrCreate(first);

        factory.getOrCreate(second);
        factory.getOrCreate(third);
        LightingProfileFlyweight reloadedFirst = factory.getOrCreate(first);

        assertEquals(2, factory.cachedProfileCount());
        assertNotSame(originalFirst, reloadedFirst);
    }

    private static LightingProfileDefinition definition(
            String firmwareVersion,
            short defaultBrightness
    ) {
        return new LightingProfileDefinition(
                1L,
                "CITY-LED",
                "City LED",
                "Lumina",
                "L-200",
                new BigDecimal("42.50"),
                StreetlightType.LED,
                defaultBrightness,
                firmwareVersion
        );
    }
}
