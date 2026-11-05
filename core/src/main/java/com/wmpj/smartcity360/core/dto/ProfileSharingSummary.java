package com.wmpj.smartcity360.core.dto;

public record ProfileSharingSummary(
        int representedStreetlights,
        int distinctSharedProfiles,
        int reusedProfileReferences,
        int factoryCachedProfiles,
        int offset,
        int limit
) {
}
