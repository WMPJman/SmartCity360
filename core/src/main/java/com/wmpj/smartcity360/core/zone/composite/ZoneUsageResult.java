package com.wmpj.smartcity360.core.zone.composite;

import java.time.OffsetDateTime;

public record ZoneUsageResult(
        ZoneUsageReport report,
        OffsetDateTime intervalStart,
        OffsetDateTime intervalEnd
) {
}
