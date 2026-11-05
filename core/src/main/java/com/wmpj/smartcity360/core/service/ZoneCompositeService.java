package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.dto.ZoneHierarchyNode;
import com.wmpj.smartcity360.core.zone.composite.PowerOperationResult;
import com.wmpj.smartcity360.core.zone.composite.ZoneUsageResult;

import java.time.OffsetDateTime;

public interface ZoneCompositeService {

    ZoneHierarchyNode getHierarchy(long zoneId);

    PowerOperationResult shutdownPower(long zoneId);

    PowerOperationResult restorePower(long zoneId);

    ZoneUsageResult getUsage(
            long zoneId,
            OffsetDateTime intervalStart,
            OffsetDateTime intervalEnd
    );
}
