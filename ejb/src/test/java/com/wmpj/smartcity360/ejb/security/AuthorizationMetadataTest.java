package com.wmpj.smartcity360.ejb.security;

import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserRoleId;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.ejb.service.IdentityPersistenceServiceBean;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthorizationMetadataTest {

    @Test
    void identityWritesAreRestrictedToAdministrators() throws Exception {
        assertAdminOnly("createUser", User.class);
        assertAdminOnly("updateUser", User.class);
        assertAdminOnly(
                "assignRole",
                long.class,
                String.class,
                OffsetDateTime.class
        );
        assertAdminOnly("removeRole", UserRoleId.class);
    }

    @Test
    void authenticationAndCallerInspectionRemainAvailableToTheSecurityContainer() {
        assertNotNull(AuthenticationServiceBean.class.getAnnotation(PermitAll.class));
        assertNotNull(CurrentIdentityServiceBean.class.getAnnotation(PermitAll.class));
    }

    private static void assertAdminOnly(
            String methodName,
            Class<?>... parameterTypes
    ) throws Exception {
        Method method = IdentityPersistenceServiceBean.class.getMethod(
                methodName,
                parameterTypes
        );
        RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);

        assertNotNull(rolesAllowed);
        assertEquals(Set.of(SecurityRoles.ADMIN), Set.of(rolesAllowed.value()));
    }
}
