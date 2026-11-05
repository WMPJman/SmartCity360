package com.wmpj.smartcity360.core.zone.composite;

import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.ZoneType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompositeZone implements ZoneComponent {

    private final long id;
    private final String name;
    private final ZoneType zoneType;
    private final List<ZoneComponent> children = new ArrayList<>();
    private PowerStatus powerStatus;

    public CompositeZone(ZoneNodeData zone) {
        if (zone.zoneType() == ZoneType.BUILDING) {
            throw new IllegalArgumentException("BUILDING must be represented by BuildingLeaf");
        }

        id = zone.id();
        name = zone.name();
        zoneType = zone.zoneType();
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
        return zoneType;
    }

    @Override
    public PowerStatus getPowerStatus() {
        return powerStatus;
    }

    @Override
    public List<ZoneComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void addChild(ZoneComponent child) {
        ZoneType expectedType = expectedChildType();

        if (child.getZoneType() != expectedType) {
            throw new IllegalArgumentException(
                    zoneType + " cannot contain " + child.getZoneType()
            );
        }

        if (child.getId() == id || containsId(child, id)) {
            throw new IllegalArgumentException("Zone hierarchy contains a cycle");
        }

        for (ZoneComponent existingChild : children) {
            if (existingChild.getId() == child.getId()) {
                throw new IllegalArgumentException("Zone hierarchy contains a duplicate child");
            }
        }

        children.add(child);
    }

    @Override
    public PowerOperationResult shutdownPower() {
        return applyPowerStatus(PowerStatus.OFF);
    }

    @Override
    public PowerOperationResult restorePower() {
        return applyPowerStatus(PowerStatus.ON);
    }

    @Override
    public ZoneUsageReport reportUsage(Map<Long, BigDecimal> buildingUsage) {
        BigDecimal total = BigDecimal.ZERO;
        int contributingBuildings = 0;

        for (ZoneComponent child : children) {
            ZoneUsageReport childReport = child.reportUsage(buildingUsage);
            total = total.add(childReport.consumptionKwh());
            contributingBuildings += childReport.contributingBuildings();
        }

        return new ZoneUsageReport(id, total, contributingBuildings);
    }

    private PowerOperationResult applyPowerStatus(PowerStatus status) {
        powerStatus = status;
        PowerOperationResult result = new PowerOperationResult(
                status,
                Set.of(id),
                Set.of()
        );

        for (ZoneComponent child : children) {
            PowerOperationResult childResult;

            if (status == PowerStatus.OFF) {
                childResult = child.shutdownPower();
            } else {
                childResult = child.restorePower();
            }

            result = result.combine(childResult);
        }

        return result;
    }

    private ZoneType expectedChildType() {
        return switch (zoneType) {
            case CITY -> ZoneType.DISTRICT;
            case DISTRICT -> ZoneType.STREET;
            case STREET -> ZoneType.BUILDING;
            case BUILDING -> throw new IllegalStateException("BUILDING is not composite");
        };
    }

    private static boolean containsId(ZoneComponent component, long searchedId) {
        if (component.getId() == searchedId) {
            return true;
        }

        for (ZoneComponent child : component.getChildren()) {
            if (containsId(child, searchedId)) {
                return true;
            }
        }

        return false;
    }
}
