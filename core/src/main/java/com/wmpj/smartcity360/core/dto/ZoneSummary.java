package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.ZoneType;

public record ZoneSummary(
        Long id,
        Long parentId,
        String name,
        String zoneCode,
        ZoneType zoneType,
        PowerStatus powerStatus
) {
}
