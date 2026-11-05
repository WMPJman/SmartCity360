package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintCategoryEntityMappingTest {

    @Test
    void mapsIdentityAndVerifiedCategoryColumns() throws Exception {
        assertNotNull(ComplaintCategory.class.getAnnotation(Entity.class));
        assertEquals("complaint_categories", ComplaintCategory.class.getAnnotation(Table.class).name());

        Field id = ComplaintCategory.class.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("id", "id", false, 255);
        assertColumn("code", "code", false, 50);
        assertTrue(ComplaintCategory.class.getDeclaredField("code").getAnnotation(Column.class).unique());
        assertColumn("name", "name", false, 100);
        assertTrue(ComplaintCategory.class.getDeclaredField("name").getAnnotation(Column.class).unique());
        assertColumn("description", "description", true, 255);
    }

    @Test
    void mapsRequiredActiveFlagWithoutInventingCategoryEnums() throws Exception {
        Field active = ComplaintCategory.class.getDeclaredField("active");
        assertEquals(boolean.class, active.getType());
        Column column = active.getAnnotation(Column.class);
        assertEquals("is_active", column.name());
        assertFalse(column.nullable());
        assertEquals(String.class, ComplaintCategory.class.getDeclaredField("code").getType());
        assertTrue(new ComplaintCategory().isActive());
    }

    private static void assertColumn(String fieldName, String name, boolean nullable, int length)
            throws Exception {
        Column column = ComplaintCategory.class.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(name, column.name());
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }
}
