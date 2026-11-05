package com.wmpj.smartcity360.ejb.security;

import com.wmpj.smartcity360.core.dao.UserRepository;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserStatus;
import com.wmpj.smartcity360.core.exception.UnauthenticatedException;
import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import jakarta.ejb.SessionContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentIdentityServiceBeanTest {

    @Test
    void resolvesIdentityAndRolesFromTheContainerCallerContext() throws Exception {
        User user = user(7L);
        CurrentIdentityServiceBean service = new CurrentIdentityServiceBean(
                new UserRepositoryFake(user),
                sessionContext(
                        "resident@example.test",
                        Set.of(SecurityRoles.RESIDENT, SecurityRoles.LOCAL_STAFF)
                )
        );

        AuthenticatedIdentity identity = service.getCurrentIdentity();

        assertEquals(7L, identity.userId());
        assertEquals(
                Set.of(SecurityRoles.RESIDENT, SecurityRoles.LOCAL_STAFF),
                identity.roleCodes()
        );
    }

    @Test
    void rejectsAnonymousAndUnknownContainerPrincipals() throws Exception {
        User user = user(7L);

        CurrentIdentityServiceBean anonymousService = new CurrentIdentityServiceBean(
                new UserRepositoryFake(user),
                sessionContext(null, Set.of())
        );
        CurrentIdentityServiceBean unknownService = new CurrentIdentityServiceBean(
                new UserRepositoryFake(user),
                sessionContext("unknown@example.test", Set.of())
        );

        assertThrows(
                UnauthenticatedException.class,
                anonymousService::getCurrentIdentity
        );
        assertThrows(
                UnauthenticatedException.class,
                unknownService::getCurrentIdentity
        );
    }

    private static User user(long id) throws Exception {
        User user = new User(
                "Test Resident",
                "resident@example.test",
                "stored-hash",
                UserStatus.ACTIVE,
                OffsetDateTime.MIN,
                OffsetDateTime.MIN
        );
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        return user;
    }

    private static SessionContext sessionContext(
            String principalName,
            Set<String> roles
    ) {
        return (SessionContext) Proxy.newProxyInstance(
                SessionContext.class.getClassLoader(),
                new Class<?>[]{SessionContext.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getCallerPrincipal")) {
                        if (principalName == null) {
                            return null;
                        }

                        Principal principal = () -> principalName;
                        return principal;
                    }

                    if (method.getName().equals("isCallerInRole")) {
                        return roles.contains((String) arguments[0]);
                    }

                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (type.equals(boolean.class)) {
            return false;
        }

        if (type.equals(int.class)) {
            return 0;
        }

        if (type.equals(long.class)) {
            return 0L;
        }

        return null;
    }

    private static final class UserRepositoryFake implements UserRepository {

        private final User user;

        UserRepositoryFake(User user) {
            this.user = user;
        }

        @Override
        public Optional<User> findById(long id) {
            return Optional.ofNullable(user);
        }

        @Override
        public Optional<User> findByNormalizedEmail(String email) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(user);
            }

            return Optional.empty();
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByNormalizedEmail(email).isPresent();
        }

        @Override
        public User persist(User user) {
            return user;
        }

        @Override
        public User update(User user) {
            return user;
        }

        @Override
        public List<User> findPage(int offset, int limit) {
            return List.of(user);
        }
    }
}
