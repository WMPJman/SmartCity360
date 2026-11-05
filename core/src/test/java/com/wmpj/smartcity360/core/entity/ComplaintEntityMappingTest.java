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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintEntityMappingTest {

    @Test
    void mapsIdentityTextAndTimestampColumns() throws Exception {
        assertNotNull(Complaint.class.getAnnotation(Entity.class));
        assertEquals("complaints", Complaint.class.getAnnotation(Table.class).name());

        Field id = Complaint.class.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("id", "id", false, 255);
        assertColumn("complaintNumber", "complaint_number", false, 40);
        assertTrue(Complaint.class.getDeclaredField("complaintNumber").getAnnotation(Column.class).unique());
        assertColumn("title", "title", false, 200);
        assertColumn("description", "description", false, 255);
        assertTimestamp("createdAt", "created_at", false);
        assertTimestamp("updatedAt", "updated_at", false);
        assertTimestamp("resolvedAt", "resolved_at", true);
    }

    @Test
    void mapsAllVerifiedRelationshipsLazilyWithoutCascade() throws Exception {
        assertRelationship("resident", User.class, "resident_id", false);
        assertRelationship("zone", Zone.class, "zone_id", false);
        assertRelationship("category", ComplaintCategory.class, "category_id", false);
        assertRelationship("assignedTo", User.class, "assigned_to", true);
    }

    @Test
    void mapsSeverityStatusAndHandlerLevelToExactDatabaseValues() throws Exception {
        assertEnum("severity", "severity", 20);
        assertArrayEquals(
                new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"},
                Arrays.stream(ComplaintSeverity.values()).map(Enum::name).toArray(String[]::new)
        );

        assertEnum("status", "status", 20);
        assertArrayEquals(
                new String[]{"SUBMITTED", "IN_PROGRESS", "ESCALATED", "RESOLVED", "REJECTED"},
                Arrays.stream(ComplaintStatus.values()).map(Enum::name).toArray(String[]::new)
        );

        assertEnum("currentHandlerLevel", "current_handler_level", 30);
        assertArrayEquals(
                new String[]{"LOCAL_STAFF", "SUPERVISOR", "CITY_MANAGER"},
                Arrays.stream(ComplaintHandlerLevel.values()).map(Enum::name).toArray(String[]::new)
        );

        Complaint complaint = new Complaint();
        assertEquals(ComplaintSeverity.LOW, complaint.getSeverity());
        assertEquals(ComplaintStatus.SUBMITTED, complaint.getStatus());
        assertEquals(ComplaintHandlerLevel.LOCAL_STAFF, complaint.getCurrentHandlerLevel());
    }

    private static void assertRelationship(
            String fieldName,
            Class<?> targetType,
            String joinColumnName,
            boolean optional
    ) throws Exception {
        Field field = Complaint.class.getDeclaredField(fieldName);
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

    private static void assertEnum(String fieldName, String columnName, int length) throws Exception {
        Field field = Complaint.class.getDeclaredField(fieldName);
        assertEquals(EnumType.STRING, field.getAnnotation(Enumerated.class).value());
        assertColumn(fieldName, columnName, false, length);
    }

    private static void assertTimestamp(String fieldName, String columnName, boolean nullable)
            throws Exception {
        assertEquals(OffsetDateTime.class, Complaint.class.getDeclaredField(fieldName).getType());
        assertColumn(fieldName, columnName, nullable, 255);
    }

    private static void assertColumn(String fieldName, String name, boolean nullable, int length)
            throws Exception {
        Column column = Complaint.class.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(name, column.name());
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }
}
