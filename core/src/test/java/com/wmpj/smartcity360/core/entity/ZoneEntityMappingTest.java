package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneEntityMappingTest {

    @Test
    void mapsVerifiedZoneColumnsAndIdentityKey() throws Exception {
        assertNotNull(Zone.class.getAnnotation(Entity.class));
        assertEquals("zones", Zone.class.getAnnotation(Table.class).name());

        Field id = Zone.class.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("id", "id", false, 255);
        assertColumn("name", "name", false, 150);
        assertColumn("zoneCode", "zone_code", false, 50);
        assertTrue(Zone.class.getDeclaredField("zoneCode").getAnnotation(Column.class).unique());
        assertColumn("description", "description", true, 255);
        assertColumn("createdAt", "created_at", false, 255);
        assertColumn("updatedAt", "updated_at", false, 255);
        assertEquals(OffsetDateTime.class, Zone.class.getDeclaredField("createdAt").getType());
        assertEquals(OffsetDateTime.class, Zone.class.getDeclaredField("updatedAt").getType());
    }

    @Test
    void mapsZoneEnumsAsDatabaseStrings() throws Exception {
        Field zoneType = Zone.class.getDeclaredField("zoneType");
        assertEquals(EnumType.STRING, zoneType.getAnnotation(Enumerated.class).value());
        assertColumn("zoneType", "zone_type", false, 20);
        assertArrayEquals(
                new String[]{"CITY", "DISTRICT", "STREET", "BUILDING"},
                Arrays.stream(ZoneType.values()).map(Enum::name).toArray(String[]::new)
        );

        Field powerStatus = Zone.class.getDeclaredField("powerStatus");
        assertEquals(EnumType.STRING, powerStatus.getAnnotation(Enumerated.class).value());
        assertColumn("powerStatus", "power_status", false, 20);
        assertArrayEquals(
                new String[]{"ON", "OFF"},
                Arrays.stream(PowerStatus.values()).map(Enum::name).toArray(String[]::new)
        );
    }

    @Test
    void parentIsOptionalLazyAndDoesNotCascade() throws Exception {
        Field parent = Zone.class.getDeclaredField("parent");
        ManyToOne relationship = parent.getAnnotation(ManyToOne.class);
        assertNotNull(relationship);
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertTrue(relationship.optional());
        assertArrayEquals(new CascadeType[0], relationship.cascade());

        JoinColumn joinColumn = parent.getAnnotation(JoinColumn.class);
        assertEquals("parent_zone_id", joinColumn.name());
        assertTrue(joinColumn.nullable());
    }

    @Test
    void childrenAreLazyEncapsulatedAndNeverRemovedByCascade() throws Exception {
        Field children = Zone.class.getDeclaredField("children");
        assertEquals(List.class, children.getType());
        OneToMany relationship = children.getAnnotation(OneToMany.class);
        assertNotNull(relationship);
        assertEquals("parent", relationship.mappedBy());
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertFalse(relationship.orphanRemoval());
        assertArrayEquals(new CascadeType[0], relationship.cascade());

        Zone zone = new Zone();
        assertNotNull(zone.getChildren());
        assertTrue(zone.getChildren().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> zone.getChildren().add(new Zone()));
    }

    @Test
    void newZonesUseObjectIdentityWithoutRecursiveEquality() {
        assertNotEquals(new Zone(), new Zone());
    }

    private static void assertColumn(
            String fieldName,
            String columnName,
            boolean nullable,
            int length
    ) throws Exception {
        Column column = Zone.class.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(columnName, column.name());
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }
}
