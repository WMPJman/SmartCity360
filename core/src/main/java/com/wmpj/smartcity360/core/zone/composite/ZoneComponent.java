package com.wmpj.smartcity360.core.zone.composite;

import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.ZoneType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ZoneComponent {

    long getId();

    String getName();

    ZoneType getZoneType();

    PowerStatus getPowerStatus();

    List<ZoneComponent> getChildren();

    default void addChild(ZoneComponent child) {
        throw new UnsupportedOperationException("Leaf zones cannot contain children");
    }

    PowerOperationResult shutdownPower();

    PowerOperationResult restorePower();

    ZoneUsageReport reportUsage(Map<Long, BigDecimal> buildingUsage);
}
