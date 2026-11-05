package com.wmpj.smartcity360.core.zone.composite;

import java.math.BigDecimal;

public record ZoneUsageReport(
        long zoneId,
        BigDecimal consumptionKwh,
        int contributingBuildings
) {

    public ZoneUsageReport {
        consumptionKwh = consumptionKwh.stripTrailingZeros();
    }
}
