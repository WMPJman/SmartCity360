package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.core.entity.ZoneType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseCheckServiceBeanTest {

    @Test
    void countRolesVerifiesAllThreeIdentityMappingsWithJpql() throws Exception {
        List<String> executedJpql = new ArrayList<>();
        AtomicReference<Object> zoneTypeParameter = new AtomicReference<>();
        EntityManager entityManager = identityEntityManager(
                List.of("ADMIN", "CITY_MANAGER", "SUPERVISOR", "LOCAL_STAFF", "RESIDENT"),
                executedJpql,
                zoneTypeParameter
        );

        DatabaseCheckServiceBean service = new DatabaseCheckServiceBean(entityManager);

        assertEquals(5L, service.countRoles());
        assertEquals(List.of(
                "SELECT r FROM Role r ORDER BY r.id",
                "SELECT COUNT(u) FROM SmartCityUser u",
                "SELECT COUNT(ur) FROM UserRole ur",
                "SELECT COUNT(z) FROM Zone z",
                "SELECT z FROM Zone z WHERE z.parent IS NULL AND z.zoneType = :zoneType ORDER BY z.id",
                "SELECT COUNT(z) FROM Zone z WHERE z.parent IS NOT NULL",
                "SELECT COUNT(c) FROM Zone p JOIN p.children c",
                "SELECT COUNT(m) FROM EnergyMeter m",
                "SELECT COUNT(m) FROM EnergyMeter m WHERE m.zone IS NOT NULL",
                "SELECT COUNT(r) FROM EnergyReading r",
                "SELECT COUNT(r) FROM EnergyReading r WHERE r.meter IS NOT NULL",
                "SELECT r.intervalStart, r.intervalEnd, r.consumptionKwh FROM EnergyReading r ORDER BY r.id",
                "SELECT COUNT(c) FROM StreamConfiguration c",
                "SELECT COUNT(c) FROM StreamConfiguration c WHERE c.meter IS NOT NULL",
                "SELECT COUNT(c) FROM StreamConfiguration c WHERE c.updatedBy IS NOT NULL",
                "SELECT c.layerOrder FROM StreamConfiguration c ORDER BY c.id",
                "SELECT COUNT(c) FROM ComplaintCategory c",
                "SELECT c.code FROM ComplaintCategory c ORDER BY c.id",
                "SELECT COUNT(c) FROM Complaint c",
                "SELECT COUNT(c) FROM Complaint c WHERE c.category IS NOT NULL",
                "SELECT COUNT(c) FROM Complaint c WHERE c.resident IS NOT NULL",
                "SELECT COUNT(c) FROM Complaint c WHERE c.zone IS NOT NULL",
                "SELECT COUNT(c) FROM Complaint c WHERE c.assignedTo IS NOT NULL",
                "SELECT c.status, c.severity, c.currentHandlerLevel FROM Complaint c ORDER BY c.id",
                "SELECT COUNT(h) FROM ComplaintHistory h",
                "SELECT COUNT(h) FROM ComplaintHistory h WHERE h.complaint IS NOT NULL",
                "SELECT COUNT(h) FROM ComplaintHistory h WHERE h.actor IS NOT NULL",
                "SELECT h.action, h.fromStatus, h.toStatus FROM ComplaintHistory h ORDER BY h.id",
                "SELECT COUNT(p) FROM StreetlightProfile p",
                "SELECT COUNT(s) FROM Streetlight s",
                "SELECT COUNT(s) FROM Streetlight s WHERE s.profile IS NOT NULL AND s.zone IS NOT NULL",
                "SELECT COUNT(p) FROM CityPolicy p",
                "SELECT COUNT(p) FROM CityPolicy p WHERE p.createdBy IS NOT NULL",
                "SELECT COUNT(e) FROM PolicyEvaluation e",
                "SELECT COUNT(e) FROM PolicyEvaluation e WHERE e.policy IS NOT NULL",
                "SELECT e.inputContext FROM PolicyEvaluation e ORDER BY e.id",
                "SELECT COUNT(a) FROM UserZoneAssignment a",
                "SELECT COUNT(a) FROM UserZoneAssignment a WHERE a.user IS NOT NULL AND a.zone IS NOT NULL",
                "SELECT COUNT(n) FROM Notification n",
                "SELECT COUNT(n) FROM Notification n WHERE n.user IS NOT NULL",
                "SELECT COUNT(l) FROM AuditLog l",
                "SELECT l.details FROM AuditLog l ORDER BY l.id"
        ), executedJpql);
        assertEquals(ZoneType.CITY, zoneTypeParameter.get());
    }

    @Test
    void countRolesRejectsUnexpectedSeededRoleCodes() throws Exception {
        EntityManager entityManager = identityEntityManager(
                List.of("ADMIN", "CITY_MANAGER", "SUPERVISOR", "LOCAL_STAFF", "UNKNOWN"),
                new ArrayList<>(),
                new AtomicReference<>()
        );

        DatabaseCheckServiceBean service = new DatabaseCheckServiceBean(entityManager);

        assertThrows(IllegalStateException.class, service::countRoles);
    }

    @Test
    void countPublicTablesReturnsTheDatabaseMetadataCount() {
        AtomicReference<String> executedSql = new AtomicReference<>();
        Query query = proxy(Query.class, (methodName, arguments) -> {
            if ("getSingleResult".equals(methodName)) {
                return BigInteger.valueOf(17);
            }
            return null;
        });
        EntityManager entityManager = proxy(EntityManager.class, (methodName, arguments) -> {
            if ("createNativeQuery".equals(methodName)) {
                executedSql.set((String) arguments[0]);
                return query;
            }
            return null;
        });

        DatabaseCheckServiceBean service = new DatabaseCheckServiceBean(entityManager);

        assertEquals(17L, service.countPublicTables());
        assertEquals("SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'", executedSql.get());
    }

    private static EntityManager identityEntityManager(
            List<String> roleCodes,
            List<String> executedJpql,
            AtomicReference<Object> zoneTypeParameter
    ) throws Exception {
        List<Role> roles = new ArrayList<>();
        for (String code : roleCodes) {
            var constructor = Role.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Role role = constructor.newInstance();
            role.setCode(code);
            roles.add(role);
        }

        Query tableCountQuery = proxy(Query.class, (methodName, arguments) ->
                "getSingleResult".equals(methodName) ? BigInteger.valueOf(17) : null);

        return proxy(EntityManager.class, (methodName, arguments) -> {
            if ("createNativeQuery".equals(methodName)) {
                return tableCountQuery;
            }
            if ("createQuery".equals(methodName)) {
                String jpql = (String) arguments[0];
                executedJpql.add(jpql);
                return typedQuery(jpql, roles, zoneTypeParameter);
            }
            return null;
        });
    }

    private static TypedQuery<?> typedQuery(
            String jpql,
            List<Role> roles,
            AtomicReference<Object> zoneTypeParameter
    ) {
        return proxy(TypedQuery.class, (methodName, arguments) -> {
            if ("getResultList".equals(methodName) && jpql.startsWith("SELECT r FROM Role")) {
                return roles;
            }
            if ("getResultList".equals(methodName) && jpql.startsWith("SELECT z FROM Zone")) {
                return List.of();
            }
            if ("getResultList".equals(methodName)) {
                return List.of();
            }
            if ("setParameter".equals(methodName) && "zoneType".equals(arguments[0])) {
                zoneTypeParameter.set(arguments[1]);
                return null;
            }
            if ("getSingleResult".equals(methodName)) {
                return 0L;
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, arguments) -> invocation.invoke(method.getName(), arguments)
        );
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(String methodName, Object[] arguments);
    }
}
