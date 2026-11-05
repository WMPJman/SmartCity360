package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamConfigurationEntityMappingTest {

    @Test
    void mapsIdentityFlagsJsonbAndTimestamp() throws Exception {
        assertNotNull(StreamConfiguration.class.getAnnotation(Entity.class));
        assertEquals("stream_configurations", StreamConfiguration.class.getAnnotation(Table.class).name());

        Field id = StreamConfiguration.class.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());

        assertRequiredBoolean("enableEncryption", "enable_encryption");
        assertRequiredBoolean("enableAuthentication", "enable_authentication");
        assertRequiredBoolean("enableCompression", "enable_compression");

        Field layerOrder = StreamConfiguration.class.getDeclaredField("layerOrder");
        assertEquals(String.class, layerOrder.getType());
        Column jsonb = layerOrder.getAnnotation(Column.class);
        assertEquals("layer_order", jsonb.name());
        assertFalse(jsonb.nullable());
        assertEquals("jsonb", jsonb.columnDefinition());

        Field updatedAt = StreamConfiguration.class.getDeclaredField("updatedAt");
        assertEquals(OffsetDateTime.class, updatedAt.getType());
        assertEquals("updated_at", updatedAt.getAnnotation(Column.class).name());
        assertFalse(updatedAt.getAnnotation(Column.class).nullable());
    }

    @Test
    void mapsOneConfigurationPerMeterLazilyWithoutCascade() throws Exception {
        Field meter = StreamConfiguration.class.getDeclaredField("meter");
        OneToOne relationship = meter.getAnnotation(OneToOne.class);
        assertNotNull(relationship);
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertFalse(relationship.optional());
        assertArrayEquals(new CascadeType[0], relationship.cascade());
        JoinColumn joinColumn = meter.getAnnotation(JoinColumn.class);
        assertEquals("meter_id", joinColumn.name());
        assertFalse(joinColumn.nullable());
        assertTrue(joinColumn.unique());
        assertEquals(EnergyMeter.class, meter.getType());
    }

    @Test
    void mapsOptionalUpdaterLazilyWithoutCascade() throws Exception {
        Field updatedBy = StreamConfiguration.class.getDeclaredField("updatedBy");
        ManyToOne relationship = updatedBy.getAnnotation(ManyToOne.class);
        assertNotNull(relationship);
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertTrue(relationship.optional());
        assertArrayEquals(new CascadeType[0], relationship.cascade());
        JoinColumn joinColumn = updatedBy.getAnnotation(JoinColumn.class);
        assertEquals("updated_by", joinColumn.name());
        assertTrue(joinColumn.nullable());
        assertEquals(User.class, updatedBy.getType());
    }

    private static void assertRequiredBoolean(String fieldName, String columnName) throws Exception {
        Field field = StreamConfiguration.class.getDeclaredField(fieldName);
        assertEquals(boolean.class, field.getType());
        Column column = field.getAnnotation(Column.class);
        assertEquals(columnName, column.name());
        assertFalse(column.nullable());
    }
}
