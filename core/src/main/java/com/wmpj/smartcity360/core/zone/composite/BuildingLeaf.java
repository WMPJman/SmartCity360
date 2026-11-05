package com.wmpj.smartcity360.core.zone.composite;

import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.ZoneType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BuildingLeaf implements ZoneComponent {

    private final long id;
    private final String name;
    private PowerStatus powerStatus;

    public BuildingLeaf(ZoneNodeData zone) {
        if (zone.zoneType() != ZoneType.BUILDING) {
            throw new IllegalArgumentException("BuildingLeaf requires a BUILDING zone");
        }

        id = zone.id();
        name = zone.name();
        powerStatus = zone.powerStatus();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ZoneType getZoneType() {
        return ZoneType.BUILDING;
    }

    @Override
    public PowerStatus getPowerStatus() {
        return powerStatus;
    }

    @Override
    public List<ZoneComponent> getChildren() {
        return List.of();
    }

    @Override
    public PowerOperationResult shutdownPower() {
        powerStatus = PowerStatus.OFF;
        return result(PowerStatus.OFF);
    }

    @Override
    public PowerOperationResult restorePower() {
        powerStatus = PowerStatus.ON;
        return result(PowerStatus.ON);
    }

    @Override
    public ZoneUsageReport reportUsage(Map<Long, BigDecimal> buildingUsage) {
        BigDecimal usage = buildingUsage.getOrDefault(id, BigDecimal.ZERO);
        int contributors = 0;

        if (usage.compareTo(BigDecimal.ZERO) != 0) {
            contributors = 1;
        }

        return new ZoneUsageReport(id, usage, contributors);
    }

    private PowerOperationResult result(PowerStatus status) {
        return new PowerOperationResult(status, Set.of(id), Set.of(id));
    }
}
