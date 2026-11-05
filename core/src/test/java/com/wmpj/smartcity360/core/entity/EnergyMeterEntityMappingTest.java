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
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyMeterEntityMappingTest {

    @Test
    void mapsIdentityAndVerifiedMeterColumns() throws Exception {
        assertNotNull(EnergyMeter.class.getAnnotation(Entity.class));
        assertEquals("energy_meters", EnergyMeter.class.getAnnotation(Table.class).name());

        Field id = EnergyMeter.class.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("id", "id", false, 255);
        assertColumn("meterCode", "meter_code", false, 60);
        assertTrue(EnergyMeter.class.getDeclaredField("meterCode").getAnnotation(Column.class).unique());
        assertColumn("installationDate", "installation_date", true, 255);
        assertColumn("createdAt", "created_at", false, 255);
        assertEquals(LocalDate.class, EnergyMeter.class.getDeclaredField("installationDate").getType());
        assertEquals(OffsetDateTime.class, EnergyMeter.class.getDeclaredField("createdAt").getType());
    }

    @Test
    void mapsMeterStatusToTheExactDatabaseValues() throws Exception {
        Field status = EnergyMeter.class.getDeclaredField("status");
        assertEquals(EnumType.STRING, status.getAnnotation(Enumerated.class).value());
        assertColumn("status", "status", false, 20);
        assertArrayEquals(
                new String[]{"ACTIVE", "INACTIVE", "FAULTY"},
                Arrays.stream(EnergyMeterStatus.values()).map(Enum::name).toArray(String[]::new)
        );
    }

    @Test
    void mapsRequiredZoneLazilyWithoutCascade() throws Exception {
        Field zone = EnergyMeter.class.getDeclaredField("zone");
        ManyToOne relationship = zone.getAnnotation(ManyToOne.class);
        assertNotNull(relationship);
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertFalse(relationship.optional());
        assertArrayEquals(new CascadeType[0], relationship.cascade());
        JoinColumn joinColumn = zone.getAnnotation(JoinColumn.class);
        assertEquals("zone_id", joinColumn.name());
        assertFalse(joinColumn.nullable());
        assertEquals(Zone.class, zone.getType());
    }

    private static void assertColumn(String fieldName, String name, boolean nullable, int length)
            throws Exception {
        Column column = EnergyMeter.class.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(name, column.name());
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }
}
