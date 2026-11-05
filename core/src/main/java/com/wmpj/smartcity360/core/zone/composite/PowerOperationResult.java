package com.wmpj.smartcity360.core.zone.composite;

import com.wmpj.smartcity360.core.entity.PowerStatus;

import java.util.Set;

public record PowerOperationResult(
        PowerStatus resultingStatus,
        Set<Long> affectedZoneIds,
        Set<Long> affectedBuildingIds
) {

    public PowerOperationResult {
        affectedZoneIds = Set.copyOf(affectedZoneIds);
        affectedBuildingIds = Set.copyOf(affectedBuildingIds);
    }

    public PowerOperationResult combine(PowerOperationResult other) {
        java.util.Set<Long> zones = new java.util.HashSet<>(affectedZoneIds);
        java.util.Set<Long> buildings = new java.util.HashSet<>(affectedBuildingIds);
        zones.addAll(other.affectedZoneIds());
        buildings.addAll(other.affectedBuildingIds());
        return new PowerOperationResult(resultingStatus, zones, buildings);
    }
}
