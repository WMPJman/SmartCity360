package com.wmpj.smartcity360.core.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistenceEntityConstructionTest {

    @Test
    void userConstructorPreservesEmailAndInitialStateWithoutCanonicalizingIt() {
        OffsetDateTime now = OffsetDateTime.parse("2026-11-05T10:00:00+05:30");

        User user = new User("Test User", "Mixed.Case@Example.test", "synthetic-hash",
                UserStatus.ACTIVE, now, now);

        assertEquals("Test User", user.getFullName());
        assertEquals("Mixed.Case@Example.test", user.getEmail());
        assertEquals("synthetic-hash", user.getPasswordHash());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void zoneConstructorRetainsParentAndHierarchyAttributes() {
        OffsetDateTime now = OffsetDateTime.parse("2026-11-05T10:00:00+05:30");
        Zone city = new Zone("Test City", "TEST-CITY", ZoneType.CITY, PowerStatus.ON, null, now, now);
        Zone district = new Zone("Test District", "TEST-DISTRICT", ZoneType.DISTRICT,
                PowerStatus.ON, city, now, now);

        assertNull(city.getParent());
        assertEquals(city, district.getParent());
        assertEquals(ZoneType.DISTRICT, district.getZoneType());
    }

    @Test
    void userRoleConstructorBuildsTheExistingCompositeKey() throws Exception {
        User user = new User("Test User", "user@example.test", "synthetic-hash", UserStatus.ACTIVE,
                OffsetDateTime.MIN, OffsetDateTime.MIN);
        Role role = newRole(9L, "TEST_ROLE");
        setId(user, 7L);
        OffsetDateTime assignedAt = OffsetDateTime.parse("2026-11-05T10:00:00+05:30");

        UserRole assignment = new UserRole(user, role, assignedAt);

        assertEquals(new UserRoleId(7L, 9L), assignment.getId());
        assertEquals(user, assignment.getUser());
        assertEquals(role, assignment.getRole());
        assertEquals(assignedAt, assignment.getAssignedAt());
    }

    @Test
    void constructorsRejectMissingRequiredValuesBeforePersistence() {
        assertThrows(NullPointerException.class, () -> new User(
                "Test User", null, "synthetic-hash", UserStatus.ACTIVE,
                OffsetDateTime.MIN, OffsetDateTime.MIN));
        assertThrows(NullPointerException.class, () -> new Zone(
                "Test City", "TEST-CITY", null, PowerStatus.ON, null,
                OffsetDateTime.MIN, OffsetDateTime.MIN));
    }

    private static Role newRole(long id, String code) throws Exception {
        var constructor = Role.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Role role = constructor.newInstance();
        setId(role, id);
        role.setCode(code);
        return role;
    }

    private static void setId(Object entity, long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
