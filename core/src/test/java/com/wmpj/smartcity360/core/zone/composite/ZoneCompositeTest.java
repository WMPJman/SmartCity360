package com.wmpj.smartcity360.core.zone.composite;

import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.ZoneType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZoneCompositeTest {

    @Test
    void buildsRequiredHierarchyAndRejectsInvalidStructures() {
        ZoneComponent city = hierarchy();

        assertEquals(ZoneType.CITY, city.getZoneType());
        assertEquals(ZoneType.DISTRICT, city.getChildren().get(0).getZoneType());
        assertEquals(
                ZoneType.BUILDING,
                city.getChildren().get(0).getChildren().get(0).getChildren().get(0)
                        .getZoneType()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> city.getChildren().get(0).getChildren().get(0).getChildren().get(0)
                        .addChild(city)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ZoneHierarchyFactory().build(
                        1L,
                        List.of(
                                node(1L, 2L, ZoneType.CITY),
                                node(2L, 1L, ZoneType.DISTRICT)
                        )
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ZoneHierarchyFactory().build(
                        1L,
                        List.of(
                                node(1L, null, ZoneType.CITY),
                                node(2L, 1L, ZoneType.BUILDING)
                        )
                )
        );
    }

    @Test
    void shutdownAndRestoreAffectOnlyTheSelectedSubtree() {
        ZoneComponent city = hierarchyWithTwoStreets();
        ZoneComponent firstStreet = city.getChildren().get(0).getChildren().get(0);
        ZoneComponent unrelatedStreet = city.getChildren().get(0).getChildren().get(1);

        PowerOperationResult shutdown = firstStreet.shutdownPower();

        assertEquals(java.util.Set.of(4L), shutdown.affectedBuildingIds());
        assertEquals(PowerStatus.OFF, firstStreet.getPowerStatus());
        assertEquals(PowerStatus.ON, unrelatedStreet.getPowerStatus());

        PowerOperationResult restore = firstStreet.restorePower();

        assertEquals(java.util.Set.of(4L), restore.affectedBuildingIds());
        assertEquals(PowerStatus.ON, firstStreet.getPowerStatus());
    }

    @Test
    void usageAggregatesPreciselyWithoutDoubleCountingAndSupportsEmptyData() {
        ZoneComponent city = hierarchyWithTwoStreets();
        ZoneUsageReport report = city.reportUsage(
                Map.of(
                        4L, new BigDecimal("10.1250"),
                        6L, new BigDecimal("3.0005")
                )
        );

        assertEquals(0, new BigDecimal("13.1255").compareTo(report.consumptionKwh()));
        assertEquals(2, report.contributingBuildings());

        ZoneUsageReport emptyReport = city.reportUsage(Map.of());

        assertEquals(0, BigDecimal.ZERO.compareTo(emptyReport.consumptionKwh()));
        assertEquals(0, emptyReport.contributingBuildings());
    }

    private static ZoneComponent hierarchy() {
        return new ZoneHierarchyFactory().build(
                1L,
                List.of(
                        node(1L, null, ZoneType.CITY),
                        node(2L, 1L, ZoneType.DISTRICT),
                        node(3L, 2L, ZoneType.STREET),
                        node(4L, 3L, ZoneType.BUILDING)
                )
        );
    }

    private static ZoneComponent hierarchyWithTwoStreets() {
        return new ZoneHierarchyFactory().build(
                1L,
                List.of(
                        node(1L, null, ZoneType.CITY),
                        node(2L, 1L, ZoneType.DISTRICT),
                        node(3L, 2L, ZoneType.STREET),
                        node(4L, 3L, ZoneType.BUILDING),
                        node(5L, 2L, ZoneType.STREET),
                        node(6L, 5L, ZoneType.BUILDING)
                )
        );
    }

    private static ZoneNodeData node(long id, Long parentId, ZoneType type) {
        return new ZoneNodeData(id, parentId, "Zone " + id, type, PowerStatus.ON);
    }
}
