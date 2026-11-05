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
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ComplaintHistoryEntityMappingTest {

    @Test
    void mapsIdentityNotesAndCreatedTimestamp() throws Exception {
        assertNotNull(ComplaintHistory.class.getAnnotation(Entity.class));
        assertEquals("complaint_history", ComplaintHistory.class.getAnnotation(Table.class).name());

        Field id = ComplaintHistory.class.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("id", "id", false, 255);
        assertColumn("notes", "notes", true, 255);
        Field createdAt = ComplaintHistory.class.getDeclaredField("createdAt");
        assertEquals(OffsetDateTime.class, createdAt.getType());
        assertColumn("createdAt", "created_at", false, 255);
    }

    @Test
    void mapsComplaintAndOptionalActorLazilyWithoutCascade() throws Exception {
        assertRelationship("complaint", Complaint.class, "complaint_id", false);
        assertRelationship("actor", User.class, "actor_id", true);
    }

    @Test
    void mapsHistoryEnumsToExactDatabaseValues() throws Exception {
        assertEnum("action", "action", false, 40);
        assertArrayEquals(
                new String[]{"SUBMITTED", "ASSIGNED", "ESCALATED", "STATUS_CHANGED", "RESOLVED", "REJECTED"},
                Arrays.stream(ComplaintHistoryAction.values()).map(Enum::name).toArray(String[]::new)
        );

        assertEnum("fromHandlerLevel", "from_handler_level", true, 30);
        assertEnum("toHandlerLevel", "to_handler_level", true, 30);
        assertEnum("fromStatus", "from_status", true, 20);
        assertEnum("toStatus", "to_status", true, 20);
    }

    private static void assertRelationship(
            String fieldName,
            Class<?> targetType,
            String joinColumnName,
            boolean optional
    ) throws Exception {
        Field field = ComplaintHistory.class.getDeclaredField(fieldName);
        assertEquals(targetType, field.getType());
        ManyToOne relationship = field.getAnnotation(ManyToOne.class);
        assertNotNull(relationship);
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertEquals(optional, relationship.optional());
        assertArrayEquals(new CascadeType[0], relationship.cascade());
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        assertEquals(joinColumnName, joinColumn.name());
        assertEquals(optional, joinColumn.nullable());
    }

    private static void assertEnum(
            String fieldName,
            String columnName,
            boolean nullable,
            int length
    ) throws Exception {
        Field field = ComplaintHistory.class.getDeclaredField(fieldName);
        assertEquals(EnumType.STRING, field.getAnnotation(Enumerated.class).value());
        assertColumn(fieldName, columnName, nullable, length);
    }

    private static void assertColumn(String fieldName, String name, boolean nullable, int length)
            throws Exception {
        Column column = ComplaintHistory.class.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(name, column.name());
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }
}
