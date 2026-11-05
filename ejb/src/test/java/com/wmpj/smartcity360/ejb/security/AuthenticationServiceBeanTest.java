package com.wmpj.smartcity360.ejb.security;

import com.wmpj.smartcity360.core.dao.UserRepository;
import com.wmpj.smartcity360.core.dao.UserRoleRepository;
import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserRoleId;
import com.wmpj.smartcity360.core.entity.UserStatus;
import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.PasswordHashService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceBeanTest {

    @Test
    void authenticatesAnActiveAccountAndReturnsTrustedDeduplicatedRoles() throws Exception {
        User user = user(7L, UserStatus.ACTIVE);
        List<UserRole> assignments = List.of(
                assignment(user, role(1L, SecurityRoles.RESIDENT)),
                assignment(user, role(2L, SecurityRoles.CITY_MANAGER)),
                assignment(user, role(3L, SecurityRoles.RESIDENT)),
                assignment(user, role(4L, "UNTRUSTED_ROLE"))
        );
        PasswordHashServiceFake passwordHashes = new PasswordHashServiceFake(true);
        AuthenticationServiceBean service = new AuthenticationServiceBean(
                new UserRepositoryFake(user),
                new UserRoleRepositoryFake(assignments),
                passwordHashes
        );

        Optional<AuthenticatedIdentity> result = service.authenticate(
                "  RESIDENT@EXAMPLE.TEST  ",
                "correct password".toCharArray()
        );

        assertTrue(result.isPresent());
        assertEquals("resident@example.test", result.orElseThrow().email());
        assertEquals(
                List.of(SecurityRoles.CITY_MANAGER, SecurityRoles.RESIDENT),
                result.orElseThrow().roleCodes().stream().sorted().toList()
        );
        assertEquals(1, passwordHashes.verificationCount);
    }

    @Test
    void activeAccountWithoutRolesCanAuthenticateWithNoPrivileges() throws Exception {
        User user = user(7L, UserStatus.ACTIVE);
        AuthenticationServiceBean service = new AuthenticationServiceBean(
                new UserRepositoryFake(user),
                new UserRoleRepositoryFake(List.of()),
                new PasswordHashServiceFake(true)
        );

        AuthenticatedIdentity identity = service.authenticate(
                "resident@example.test",
                "correct password".toCharArray()
        ).orElseThrow();

        assertTrue(identity.roleCodes().isEmpty());
    }

    @Test
    void rejectsWrongPasswordSuspendedAndDisabledAccounts() throws Exception {
        assertTrue(authenticate(user(7L, UserStatus.ACTIVE), false).isEmpty());
        assertTrue(authenticate(user(7L, UserStatus.SUSPENDED), true).isEmpty());
        assertTrue(authenticate(user(7L, UserStatus.DISABLED), true).isEmpty());
    }

    @Test
    void unknownAccountPerformsPasswordWorkAndReturnsTheSameEmptyResult() {
        PasswordHashServiceFake passwordHashes = new PasswordHashServiceFake(false);
        AuthenticationServiceBean service = new AuthenticationServiceBean(
                new UserRepositoryFake(),
                new UserRoleRepositoryFake(List.of()),
                passwordHashes
        );

        Optional<AuthenticatedIdentity> result = service.authenticate(
                "unknown@example.test",
                "incorrect password".toCharArray()
        );

        assertTrue(result.isEmpty());
        assertEquals(1, passwordHashes.verificationCount);
    }

    @Test
    void persistenceFailureReturnsAGenericAuthenticationFailure() {
        PasswordHashServiceFake passwordHashes = new PasswordHashServiceFake(false);
        AuthenticationServiceBean service = new AuthenticationServiceBean(
                new FailingUserRepository(),
                new UserRoleRepositoryFake(List.of()),
                passwordHashes
        );

        Optional<AuthenticatedIdentity> result = service.authenticate(
                "resident@example.test",
                "password".toCharArray()
        );

        assertTrue(result.isEmpty());
    }

    private static Optional<AuthenticatedIdentity> authenticate(
            User user,
            boolean passwordMatches
    ) {
        AuthenticationServiceBean service = new AuthenticationServiceBean(
                new UserRepositoryFake(user),
                new UserRoleRepositoryFake(List.of()),
                new PasswordHashServiceFake(passwordMatches)
        );

        return service.authenticate(
                "resident@example.test",
                "password".toCharArray()
        );
    }

    private static User user(long id, UserStatus status) throws Exception {
        User user = new User(
                "Test Resident",
                "resident@example.test",
                "stored-password-hash",
                status,
                OffsetDateTime.MIN,
                OffsetDateTime.MIN
        );
        setId(user, id);
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

    private static UserRole assignment(User user, Role role) {
        return new UserRole(user, role, OffsetDateTime.MIN);
    }

    private static void setId(Object entity, long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private static class UserRepositoryFake implements UserRepository {

        private final List<User> users = new ArrayList<>();

        UserRepositoryFake(User... initialUsers) {
            users.addAll(List.of(initialUsers));
        }

        @Override
        public Optional<User> findById(long id) {
            return users.stream()
                    .filter(user -> user.getId() == id)
                    .findFirst();
        }

        @Override
        public Optional<User> findByNormalizedEmail(String email) {
            return users.stream()
                    .filter(user -> user.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByNormalizedEmail(email).isPresent();
        }

        @Override
        public User persist(User user) {
            users.add(user);
            return user;
        }

        @Override
        public User update(User user) {
            return user;
        }

        @Override
        public List<User> findPage(int offset, int limit) {
            return List.copyOf(users);
        }
    }

    private static final class FailingUserRepository extends UserRepositoryFake {

        @Override
        public Optional<User> findByNormalizedEmail(String email) {
            throw new IllegalStateException("synthetic persistence failure");
        }
    }

    private static final class UserRoleRepositoryFake implements UserRoleRepository {

        private final List<UserRole> assignments;

        UserRoleRepositoryFake(List<UserRole> assignments) {
            this.assignments = assignments;
        }

        @Override
        public Optional<UserRole> findById(UserRoleId id) {
            return Optional.empty();
        }

        @Override
        public List<UserRole> findByUserId(long userId) {
            return assignments;
        }

        @Override
        public boolean exists(long userId, long roleId) {
            return false;
        }

        @Override
        public UserRole persist(UserRole assignment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(UserRoleId id) {
            return false;
        }
    }

    private static final class PasswordHashServiceFake implements PasswordHashService {

        private final boolean matches;
        private int verificationCount;

        PasswordHashServiceFake(boolean matches) {
            this.matches = matches;
        }

        @Override
        public String hash(char[] password) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean verify(char[] password, String storedHash) {
            verificationCount++;
            return matches;
        }
    }
}
