package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.ZoneType;

import java.util.List;

public record ZoneHierarchyNode(
        long id,
        String name,
        ZoneType zoneType,
        PowerStatus powerStatus,
        List<ZoneHierarchyNode> children
) {

    public ZoneHierarchyNode {
        children = List.copyOf(children);
    }
}
