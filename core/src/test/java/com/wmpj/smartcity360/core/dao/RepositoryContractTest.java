package com.wmpj.smartcity360.core.dao;

import com.wmpj.smartcity360.core.dto.UserSummary;
import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserRoleId;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.service.IdentityPersistenceService;
import com.wmpj.smartcity360.core.service.ZonePersistenceService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RepositoryContractTest {

    @Test
    void repositoryContractsExposeTypedOptionalLookupsAndBoundedCollections() throws Exception {
        assertReturnType(UserRepository.class, "findById", Optional.class, long.class);
        assertReturnType(UserRepository.class, "findByNormalizedEmail", Optional.class, String.class);
        assertReturnType(UserRepository.class, "existsByEmail", boolean.class, String.class);
        assertReturnType(UserRepository.class, "persist", User.class, User.class);
        assertReturnType(UserRepository.class, "update", User.class, User.class);
        assertReturnType(UserRepository.class, "findPage", List.class, int.class, int.class);

        assertReturnType(RoleRepository.class, "findById", Optional.class, long.class);
        assertReturnType(RoleRepository.class, "findByCode", Optional.class, String.class);
        assertReturnType(RoleRepository.class, "findAll", List.class);
        assertReturnType(RoleRepository.class, "existsByCode", boolean.class, String.class);

        assertReturnType(UserRoleRepository.class, "findById", Optional.class, UserRoleId.class);
        assertReturnType(UserRoleRepository.class, "findByUserId", List.class, long.class);
        assertReturnType(UserRoleRepository.class, "exists", boolean.class, long.class, long.class);
        assertReturnType(UserRoleRepository.class, "persist", UserRole.class, UserRole.class);
        assertReturnType(UserRoleRepository.class, "remove", boolean.class, UserRoleId.class);

        assertReturnType(ZoneRepository.class, "findById", Optional.class, long.class);
        assertReturnType(ZoneRepository.class, "findByCode", Optional.class, String.class);
        assertReturnType(ZoneRepository.class, "existsByCode", boolean.class, String.class);
        assertReturnType(ZoneRepository.class, "findRootCities", List.class, int.class);
        assertReturnType(ZoneRepository.class, "findDirectChildren", List.class, long.class, int.class);
        assertReturnType(ZoneRepository.class, "findByType", List.class, ZoneType.class, int.class, int.class);
        assertReturnType(ZoneRepository.class, "persist", Zone.class, Zone.class);
        assertReturnType(ZoneRepository.class, "update", Zone.class, Zone.class);
    }

    @Test
    void serviceContractsKeepPasswordHashesOutOfReadDtos() throws Exception {
        assertReturnType(IdentityPersistenceService.class, "findUser", Optional.class, long.class);
        assertReturnType(IdentityPersistenceService.class, "findRoles", List.class, long.class);
        assertReturnType(IdentityPersistenceService.class, "roleExists", boolean.class, String.class);
        assertReturnType(IdentityPersistenceService.class, "createUser", User.class, User.class);
        assertReturnType(IdentityPersistenceService.class, "updateUser", User.class, User.class);
        assertReturnType(IdentityPersistenceService.class, "assignRole", UserRole.class,
                long.class, String.class, OffsetDateTime.class);
        assertReturnType(IdentityPersistenceService.class, "removeRole", boolean.class, UserRoleId.class);

        assertReturnType(ZonePersistenceService.class, "findZone", Optional.class, long.class);
        assertReturnType(ZonePersistenceService.class, "findDirectChildren", List.class, long.class, int.class);
        assertReturnType(ZonePersistenceService.class, "createZone", Zone.class, Zone.class);
        assertReturnType(ZonePersistenceService.class, "updateZone", Zone.class, Zone.class);

        for (var component : UserSummary.class.getRecordComponents()) {
            assertFalse(component.getName().toLowerCase().contains("password"));
        }
        assertEquals(7, UserSummary.class.getRecordComponents().length);
    }

    private static void assertReturnType(
            Class<?> owner,
            String methodName,
            Class<?> returnType,
            Class<?>... parameterTypes
    ) throws Exception {
        Method method = owner.getMethod(methodName, parameterTypes);
        assertEquals(returnType, method.getReturnType());
    }
}
