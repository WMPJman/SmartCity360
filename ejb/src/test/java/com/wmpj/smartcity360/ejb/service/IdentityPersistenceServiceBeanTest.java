package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.dao.RoleRepository;
import com.wmpj.smartcity360.core.dao.UserRepository;
import com.wmpj.smartcity360.core.dao.UserRoleRepository;
import com.wmpj.smartcity360.core.dto.UserSummary;
import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserRoleId;
import com.wmpj.smartcity360.core.entity.UserStatus;
import com.wmpj.smartcity360.core.exception.DuplicateResourceException;
import com.wmpj.smartcity360.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentityPersistenceServiceBeanTest {

    @Test
    void safeUserReadDoesNotExposeThePasswordHash() throws Exception {
        User user = user(7L, "Mixed.Case@example.test");
        IdentityPersistenceServiceBean service = service(new UserRepositoryFake(user), new RoleRepositoryFake(),
                new UserRoleRepositoryFake());

        UserSummary summary = service.findUser(7L).orElseThrow();

        assertEquals(7L, summary.id());
        assertEquals("Test User", summary.fullName());
        assertEquals("Mixed.Case@example.test", summary.email());
        assertFalse(summary.toString().contains("synthetic-hash"));
    }

    @Test
    void createUserRejectsAnEmailAlreadyPresentUnderDatabaseCaseRules() {
        UserRepositoryFake users = new UserRepositoryFake();
        users.emailExists = true;
        IdentityPersistenceServiceBean service = service(users, new RoleRepositoryFake(),
                new UserRoleRepositoryFake());

        assertThrows(DuplicateResourceException.class,
                () -> service.createUser(user(null, "Mixed.Case@example.test")));
    }

    @Test
    void assignRoleCoordinatesExistingEntitiesAndRejectsMissingOrDuplicateAssignments() throws Exception {
        User user = user(7L, "user@example.test");
        Role role = role(9L, "RESIDENT");
        UserRepositoryFake users = new UserRepositoryFake(user);
        RoleRepositoryFake roles = new RoleRepositoryFake(role);
        UserRoleRepositoryFake assignments = new UserRoleRepositoryFake();
        IdentityPersistenceServiceBean service = service(users, roles, assignments);
        OffsetDateTime assignedAt = OffsetDateTime.parse("2026-11-05T12:00:00+05:30");

        UserRole created = service.assignRole(7L, "RESIDENT", assignedAt);
        assertEquals(new UserRoleId(7L, 9L), created.getId());
        assertSame(created, assignments.persisted);

        assignments.exists = true;
        assertThrows(DuplicateResourceException.class, () -> service.assignRole(7L, "RESIDENT", assignedAt));
        assertThrows(ResourceNotFoundException.class, () -> service.assignRole(404L, "RESIDENT", assignedAt));
        assertThrows(ResourceNotFoundException.class, () -> service.assignRole(7L, "UNKNOWN", assignedAt));
    }

    private static IdentityPersistenceServiceBean service(
            UserRepository users,
            RoleRepository roles,
            UserRoleRepository assignments
    ) {
        return new IdentityPersistenceServiceBean(users, roles, assignments);
    }

    private static User user(Long id, String email) {
        User user = new User("Test User", email, "synthetic-hash", UserStatus.ACTIVE,
                OffsetDateTime.MIN, OffsetDateTime.MIN);
        if (id != null) {
            setId(user, id);
        }
        return user;
    }

    private static Role role(long id, String code) throws Exception {
        var constructor = Role.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Role role = constructor.newInstance();
        role.setCode(code);
        setId(role, id);
        return role;
    }

    private static void setId(Object entity, long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static final class UserRepositoryFake implements UserRepository {
        private final List<User> users = new ArrayList<>();
        private boolean emailExists;

        UserRepositoryFake(User... initial) {
            users.addAll(List.of(initial));
        }

        public Optional<User> findById(long id) { return users.stream().filter(u -> u.getId() != null && u.getId() == id).findFirst(); }
        public Optional<User> findByNormalizedEmail(String email) { return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst(); }
        public boolean existsByEmail(String email) { return emailExists || findByNormalizedEmail(email).isPresent(); }
        public User persist(User user) { users.add(user); return user; }
        public User update(User user) { return user; }
        public List<User> findPage(int offset, int limit) { return List.copyOf(users); }
    }

    private static final class RoleRepositoryFake implements RoleRepository {
        private final List<Role> roles = new ArrayList<>();

        RoleRepositoryFake(Role... initial) { roles.addAll(List.of(initial)); }
        public Optional<Role> findById(long id) { return roles.stream().filter(r -> r.getId() == id).findFirst(); }
        public Optional<Role> findByCode(String code) { return roles.stream().filter(r -> r.getCode().equals(code)).findFirst(); }
        public List<Role> findAll() { return List.copyOf(roles); }
        public boolean existsByCode(String code) { return findByCode(code).isPresent(); }
    }

    private static final class UserRoleRepositoryFake implements UserRoleRepository {
        private boolean exists;
        private UserRole persisted;

        public Optional<UserRole> findById(UserRoleId id) { return Optional.empty(); }
        public List<UserRole> findByUserId(long userId) { return persisted == null ? List.of() : List.of(persisted); }
        public boolean exists(long userId, long roleId) { return exists; }
        public UserRole persist(UserRole assignment) { persisted = assignment; return assignment; }
        public boolean remove(UserRoleId id) { return false; }
    }
}
