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
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnergyReadingEntityMappingTest {

    @Test
    void mapsIdentityIntervalAndDecimalPrecision() throws Exception {
        assertNotNull(EnergyReading.class.getAnnotation(Entity.class));
        Table table = EnergyReading.class.getAnnotation(Table.class);
        assertEquals("energy_readings", table.name());
        assertArrayEquals(
                new String[]{"meter_id", "interval_start"},
                Arrays.stream(table.uniqueConstraints())
                        .filter(constraint -> "uq_energy_interval".equals(constraint.name()))
                        .findFirst()
                        .map(UniqueConstraint::columnNames)
                        .orElseThrow()
        );

        Field id = EnergyReading.class.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());

        assertTimestamp("intervalStart", "interval_start");
        assertTimestamp("intervalEnd", "interval_end");
        assertTimestamp("recordedAt", "recorded_at");

        Field consumption = EnergyReading.class.getDeclaredField("consumptionKwh");
        assertEquals(BigDecimal.class, consumption.getType());
        Column column = consumption.getAnnotation(Column.class);
        assertEquals("consumption_kwh", column.name());
        assertFalse(column.nullable());
        assertEquals(14, column.precision());
        assertEquals(4, column.scale());
    }

    @Test
    void mapsRequiredMeterLazilyWithoutCascade() throws Exception {
        Field meter = EnergyReading.class.getDeclaredField("meter");
        ManyToOne relationship = meter.getAnnotation(ManyToOne.class);
        assertNotNull(relationship);
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertFalse(relationship.optional());
        assertArrayEquals(new CascadeType[0], relationship.cascade());
        JoinColumn joinColumn = meter.getAnnotation(JoinColumn.class);
        assertEquals("meter_id", joinColumn.name());
        assertFalse(joinColumn.nullable());
        assertEquals(EnergyMeter.class, meter.getType());
    }

    @Test
    void mapsReadingSourceToTheExactDatabaseValues() throws Exception {
        Field source = EnergyReading.class.getDeclaredField("source");
        assertEquals(EnumType.STRING, source.getAnnotation(Enumerated.class).value());
        Column column = source.getAnnotation(Column.class);
        assertEquals("source", column.name());
        assertFalse(column.nullable());
        assertEquals(20, column.length());
        assertArrayEquals(
                new String[]{"SIMULATED", "IMPORTED"},
                Arrays.stream(EnergyReadingSource.values()).map(Enum::name).toArray(String[]::new)
        );
    }

    private static void assertTimestamp(String fieldName, String columnName) throws Exception {
        Field field = EnergyReading.class.getDeclaredField(fieldName);
        assertEquals(OffsetDateTime.class, field.getType());
        Column column = field.getAnnotation(Column.class);
        assertEquals(columnName, column.name());
        assertFalse(column.nullable());
    }
}
