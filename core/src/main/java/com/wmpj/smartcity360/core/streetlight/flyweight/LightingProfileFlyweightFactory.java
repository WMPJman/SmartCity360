package com.wmpj.smartcity360.core.streetlight.flyweight;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class LightingProfileFlyweightFactory {

    public static final int DEFAULT_MAXIMUM_PROFILES = 256;

    private final int maximumProfiles;
    private final Map<LightingProfileDefinition, LightingProfileFlyweight> profiles;

    public LightingProfileFlyweightFactory() {
        this(DEFAULT_MAXIMUM_PROFILES);
    }

    public LightingProfileFlyweightFactory(int maximumProfiles) {
        if (maximumProfiles < 1) {
            throw new IllegalArgumentException("maximumProfiles must be positive");
        }

        this.maximumProfiles = maximumProfiles;
        this.profiles = new LinkedHashMap<>(16, 0.75F, true);
    }

    public synchronized LightingProfileFlyweight getOrCreate(
            LightingProfileDefinition definition
    ) {
        Objects.requireNonNull(definition, "definition");
        LightingProfileFlyweight existing = profiles.get(definition);

        if (existing != null) {
            return existing;
        }

        LightingProfileFlyweight created = new LightingProfileFlyweight(definition);
        profiles.put(definition, created);
        evictOldestIfNecessary();
        return created;
    }

    public synchronized int cachedProfileCount() {
        return profiles.size();
    }

    private void evictOldestIfNecessary() {
        if (profiles.size() <= maximumProfiles) {
            return;
        }

        LightingProfileDefinition oldestKey = profiles.keySet().iterator().next();
        profiles.remove(oldestKey);
    }
}
