package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityEntityMappingTest {

    @Test
    void userMapsVerifiedUsersTableColumnsAndIdentityKey() throws Exception {
        assertEntityTable(User.class, "users");
        assertIdentityId(User.class);
        assertColumn(User.class, "fullName", "full_name", false, 150);
        assertColumn(User.class, "email", "email", false, 255);
        assertColumn(User.class, "passwordHash", "password_hash", false, 255);
        assertColumn(User.class, "phoneNumber", "phone_number", true, 30);
        assertColumn(User.class, "createdAt", "created_at", false, 255);
        assertColumn(User.class, "updatedAt", "updated_at", false, 255);
        assertColumn(User.class, "lastLoginAt", "last_login_at", true, 255);

        Field status = User.class.getDeclaredField("status");
        assertEquals(EnumType.STRING, status.getAnnotation(Enumerated.class).value());
        assertColumn(User.class, "status", "status", false, 20);
        assertEquals(OffsetDateTime.class, User.class.getDeclaredField("createdAt").getType());
        assertEquals(OffsetDateTime.class, User.class.getDeclaredField("updatedAt").getType());
        assertEquals(OffsetDateTime.class, User.class.getDeclaredField("lastLoginAt").getType());
    }

    @Test
    void userStatusMatchesTheDatabaseCheckConstraint() {
        assertArrayEquals(
                new String[]{"ACTIVE", "SUSPENDED", "DISABLED"},
                Arrays.stream(UserStatus.values()).map(Enum::name).toArray(String[]::new)
        );
    }

    @Test
    void roleMapsVerifiedRolesTableAndKeepsCodeExtensible() throws Exception {
        assertEntityTable(Role.class, "roles");
        assertIdentityId(Role.class);
        assertColumn(Role.class, "code", "code", false, 40);
        assertTrue(Role.class.getDeclaredField("code").getAnnotation(Column.class).unique());
        assertEquals(String.class, Role.class.getDeclaredField("code").getType());
        assertColumn(Role.class, "name", "name", false, 100);
        assertColumn(Role.class, "description", "description", true, 255);
    }

    @Test
    void userRoleMapsCompositeKeyAndLazyRelationships() throws Exception {
        assertEntityTable(UserRole.class, "user_roles");
        Field id = UserRole.class.getDeclaredField("id");
        assertNotNull(id.getAnnotation(EmbeddedId.class));
        assertEquals(UserRoleId.class, id.getType());

        assertMappedRelationship(UserRole.class.getDeclaredField("user"), "userId", "user_id");
        assertMappedRelationship(UserRole.class.getDeclaredField("role"), "roleId", "role_id");
        assertColumn(UserRole.class, "assignedAt", "assigned_at", false, 255);
        assertEquals(OffsetDateTime.class, UserRole.class.getDeclaredField("assignedAt").getType());
    }

    @Test
    void userRoleIdUsesBothForeignKeysForEquality() {
        UserRoleId first = new UserRoleId(10L, 20L);
        UserRoleId same = new UserRoleId(10L, 20L);
        UserRoleId differentUser = new UserRoleId(11L, 20L);
        UserRoleId differentRole = new UserRoleId(10L, 21L);

        assertNotSame(first, same);
        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(differentUser));
        assertFalse(first.equals(differentRole));
    }

    private static void assertEntityTable(Class<?> type, String tableName) {
        assertNotNull(type.getAnnotation(Entity.class));
        assertEquals(tableName, type.getAnnotation(Table.class).name());
    }

    private static void assertIdentityId(Class<?> type) throws Exception {
        Field id = type.getDeclaredField("id");
        assertEquals(Long.class, id.getType());
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        Column column = id.getAnnotation(Column.class);
        assertEquals("id", column.name());
        assertFalse(column.nullable());
    }

    private static void assertColumn(
            Class<?> type,
            String fieldName,
            String columnName,
            boolean nullable,
            int length
    ) throws Exception {
        Column column = type.getDeclaredField(fieldName).getAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(columnName, column.name());
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }

    private static void assertMappedRelationship(Field field, String mapsId, String joinColumnName) {
        assertEquals(mapsId, field.getAnnotation(MapsId.class).value());
        ManyToOne relationship = field.getAnnotation(ManyToOne.class);
        assertNotNull(relationship);
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertFalse(relationship.optional());
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        assertEquals(joinColumnName, joinColumn.name());
        assertFalse(joinColumn.nullable());
    }
}
