package com.wmpj.smartcity360.core.zone.composite;

import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.ZoneType;

import java.util.Objects;

public record ZoneNodeData(
        long id,
        Long parentId,
        String name,
        ZoneType zoneType,
        PowerStatus powerStatus
) {

    public ZoneNodeData {
        if (id <= 0L) {
            throw new IllegalArgumentException("zone id must be positive");
        }

        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(zoneType, "zoneType");
        Objects.requireNonNull(powerStatus, "powerStatus");
    }
}
