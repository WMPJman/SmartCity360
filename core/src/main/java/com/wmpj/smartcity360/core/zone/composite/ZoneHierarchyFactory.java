package com.wmpj.smartcity360.core.zone.composite;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ZoneHierarchyFactory {

    public ZoneComponent build(long rootZoneId, Collection<ZoneNodeData> zones) {
        Map<Long, ZoneNodeData> dataById = new HashMap<>();
        Map<Long, ZoneComponent> componentsById = new HashMap<>();

        for (ZoneNodeData zone : zones) {
            if (dataById.put(zone.id(), zone) != null) {
                throw new IllegalArgumentException("Zone hierarchy contains duplicate IDs");
            }

            ZoneComponent component;

            if (zone.zoneType() == com.wmpj.smartcity360.core.entity.ZoneType.BUILDING) {
                component = new BuildingLeaf(zone);
            } else {
                component = new CompositeZone(zone);
            }

            componentsById.put(zone.id(), component);
        }

        ZoneComponent root = componentsById.get(rootZoneId);

        if (root == null) {
            throw new IllegalArgumentException("Root zone is missing from the hierarchy");
        }

        for (ZoneNodeData zone : zones) {
            detectParentCycle(zone.id(), dataById);

            if (zone.id() == rootZoneId) {
                continue;
            }

            Long parentId = zone.parentId();
            ZoneComponent parent = componentsById.get(parentId);

            if (parent == null) {
                throw new IllegalArgumentException("Zone hierarchy contains a missing parent");
            }

            parent.addChild(componentsById.get(zone.id()));
        }

        return root;
    }

    private static void detectParentCycle(
            long zoneId,
            Map<Long, ZoneNodeData> dataById
    ) {
        Set<Long> visited = new HashSet<>();
        Long currentId = zoneId;

        while (currentId != null) {
            if (!visited.add(currentId)) {
                throw new IllegalArgumentException("Zone hierarchy contains a cycle");
            }

            ZoneNodeData current = dataById.get(currentId);

            if (current == null) {
                return;
            }

            currentId = current.parentId();
        }
    }
}
