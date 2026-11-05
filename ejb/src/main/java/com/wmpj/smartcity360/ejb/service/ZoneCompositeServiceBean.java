package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.dto.ZoneHierarchyNode;
import com.wmpj.smartcity360.core.entity.AuditLog;
import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.exception.ResourceNotFoundException;
import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.security.ZoneScopePolicy;
import com.wmpj.smartcity360.core.service.CurrentIdentityService;
import com.wmpj.smartcity360.core.service.ZoneCompositeService;
import com.wmpj.smartcity360.core.zone.composite.PowerOperationResult;
import com.wmpj.smartcity360.core.zone.composite.ZoneComponent;
import com.wmpj.smartcity360.core.zone.composite.ZoneHierarchyFactory;
import com.wmpj.smartcity360.core.zone.composite.ZoneNodeData;
import com.wmpj.smartcity360.core.zone.composite.ZoneUsageReport;
import com.wmpj.smartcity360.core.zone.composite.ZoneUsageResult;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Stateless
@Local(ZoneCompositeService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ZoneCompositeServiceBean implements ZoneCompositeService {

    private static final int MAX_SUBTREE_ZONES = 1_000;
    private static final int MAX_HIERARCHY_LEVELS = 4;
    private static final Duration MAX_REPORTING_INTERVAL = Duration.ofDays(31);

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    @EJB
    private CurrentIdentityService currentIdentityService;

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public ZoneHierarchyNode getHierarchy(long zoneId) {
        LoadedHierarchy hierarchy = loadAuthorizedHierarchy(zoneId, false);
        return toHierarchyNode(hierarchy.root());
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR
    })
    public PowerOperationResult shutdownPower(long zoneId) {
        return changePowerStatus(zoneId, PowerStatus.OFF);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR
    })
    public PowerOperationResult restorePower(long zoneId) {
        return changePowerStatus(zoneId, PowerStatus.ON);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public ZoneUsageResult getUsage(
            long zoneId,
            OffsetDateTime intervalStart,
            OffsetDateTime intervalEnd
    ) {
        validateInterval(intervalStart, intervalEnd);
        LoadedHierarchy hierarchy = loadAuthorizedHierarchy(zoneId, false);
        Set<Long> buildingIds = buildingIds(hierarchy.zones());
        Map<Long, BigDecimal> usageByBuilding = loadBuildingUsage(
                buildingIds,
                intervalStart,
                intervalEnd
        );
        ZoneUsageReport report = hierarchy.root().reportUsage(usageByBuilding);

        return new ZoneUsageResult(report, intervalStart, intervalEnd);
    }

    private PowerOperationResult changePowerStatus(long zoneId, PowerStatus status) {
        LoadedHierarchy hierarchy = loadAuthorizedHierarchy(zoneId, true);
        PowerOperationResult result;

        if (status == PowerStatus.OFF) {
            result = hierarchy.root().shutdownPower();
        } else {
            result = hierarchy.root().restorePower();
        }

        for (Zone zone : hierarchy.zones()) {
            if (result.affectedZoneIds().contains(zone.getId())) {
                zone.setPowerStatus(status);
            }
        }

        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        User actor = entityManager.getReference(User.class, identity.userId());
        String action = "ZONE_POWER_" + status.name();
        AuditLog auditLog = new AuditLog(
                actor,
                action,
                "Zone",
                zoneId,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        entityManager.persist(auditLog);
        entityManager.flush();
        return result;
    }

    private LoadedHierarchy loadAuthorizedHierarchy(long zoneId, boolean writeOperation) {
        if (zoneId <= 0L) {
            throw new IllegalArgumentException("zoneId must be positive");
        }

        Zone rootZone = entityManager.find(Zone.class, zoneId);

        if (rootZone == null) {
            throw new ResourceNotFoundException("Zone was not found");
        }

        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        authorize(identity, rootZone, writeOperation);
        List<Zone> zones = loadSubtree(rootZone);
        List<ZoneNodeData> nodeData = zones.stream()
                .map(ZoneCompositeServiceBean::toNodeData)
                .toList();
        ZoneComponent root = new ZoneHierarchyFactory().build(zoneId, nodeData);
        return new LoadedHierarchy(root, zones);
    }

    private void authorize(
            AuthenticatedIdentity identity,
            Zone targetZone,
            boolean writeOperation
    ) {
        Set<String> roles = identity.roleCodes();

        if (roles.contains(SecurityRoles.ADMIN)) {
            return;
        }

        Set<Long> ancestorIds = ancestorIds(targetZone);
        Long assignmentCount = entityManager.createQuery(
                        "SELECT COUNT(a) FROM UserZoneAssignment a "
                                + "WHERE a.user.id = :userId AND a.zone.id IN :zoneIds",
                        Long.class
                )
                .setParameter("userId", identity.userId())
                .setParameter("zoneIds", ancestorIds)
                .getSingleResult();

        ZoneScopePolicy.requireAccess(
                roles,
                assignmentCount > 0L,
                writeOperation
        );
    }

    private List<Zone> loadSubtree(Zone rootZone) {
        List<Zone> zones = new ArrayList<>();
        zones.add(rootZone);
        Set<Long> parentIds = Set.of(rootZone.getId());

        for (int level = 0; level < MAX_HIERARCHY_LEVELS; level++) {
            if (parentIds.isEmpty()) {
                break;
            }

            List<Zone> children = entityManager.createQuery(
                            "SELECT z FROM Zone z WHERE z.parent.id IN :parentIds ORDER BY z.id",
                            Zone.class
                    )
                    .setParameter("parentIds", parentIds)
                    .setMaxResults(MAX_SUBTREE_ZONES + 1)
                    .getResultList();

            if (zones.size() + children.size() > MAX_SUBTREE_ZONES) {
                throw new IllegalArgumentException("Zone hierarchy exceeds the safe limit");
            }

            zones.addAll(children);
            parentIds = new HashSet<>();

            for (Zone child : children) {
                parentIds.add(child.getId());
            }
        }

        if (!parentIds.isEmpty()) {
            throw new IllegalArgumentException("Zone hierarchy exceeds the supported depth");
        }

        return zones;
    }

    private Map<Long, BigDecimal> loadBuildingUsage(
            Set<Long> buildingIds,
            OffsetDateTime intervalStart,
            OffsetDateTime intervalEnd
    ) {
        Map<Long, BigDecimal> usageByBuilding = new HashMap<>();

        if (buildingIds.isEmpty()) {
            return usageByBuilding;
        }

        List<Object[]> rows = entityManager.createQuery(
                        "SELECT r.meter.zone.id, SUM(r.consumptionKwh) "
                                + "FROM EnergyReading r "
                                + "WHERE r.meter.zone.id IN :buildingIds "
                                + "AND r.intervalStart >= :intervalStart "
                                + "AND r.intervalEnd <= :intervalEnd "
                                + "GROUP BY r.meter.zone.id",
                        Object[].class
                )
                .setParameter("buildingIds", buildingIds)
                .setParameter("intervalStart", intervalStart)
                .setParameter("intervalEnd", intervalEnd)
                .getResultList();

        for (Object[] row : rows) {
            usageByBuilding.put((Long) row[0], (BigDecimal) row[1]);
        }

        return usageByBuilding;
    }

    private static void validateInterval(
            OffsetDateTime intervalStart,
            OffsetDateTime intervalEnd
    ) {
        if (intervalStart == null || intervalEnd == null) {
            throw new IllegalArgumentException("Reporting interval is required");
        }

        if (!intervalStart.isBefore(intervalEnd)) {
            throw new IllegalArgumentException("Reporting interval is invalid");
        }

        if (Duration.between(intervalStart, intervalEnd).compareTo(MAX_REPORTING_INTERVAL) > 0) {
            throw new IllegalArgumentException("Reporting interval exceeds 31 days");
        }
    }

    private static Set<Long> ancestorIds(Zone zone) {
        Set<Long> ids = new HashSet<>();
        Zone current = zone;

        while (current != null) {
            if (ids.size() >= MAX_HIERARCHY_LEVELS) {
                throw new IllegalArgumentException(
                        "Zone hierarchy exceeds the supported depth"
                );
            }

            if (!ids.add(current.getId())) {
                throw new IllegalArgumentException("Zone hierarchy contains a cycle");
            }

            current = current.getParent();
        }

        return ids;
    }

    private static Set<Long> buildingIds(List<Zone> zones) {
        Set<Long> buildingIds = new HashSet<>();

        for (Zone zone : zones) {
            if (zone.getZoneType() == ZoneType.BUILDING) {
                buildingIds.add(zone.getId());
            }
        }

        return buildingIds;
    }

    private static ZoneNodeData toNodeData(Zone zone) {
        Long parentId = null;

        if (zone.getParent() != null) {
            parentId = zone.getParent().getId();
        }

        return new ZoneNodeData(
                zone.getId(),
                parentId,
                zone.getName(),
                zone.getZoneType(),
                zone.getPowerStatus()
        );
    }

    private static ZoneHierarchyNode toHierarchyNode(ZoneComponent component) {
        List<ZoneHierarchyNode> children = component.getChildren()
                .stream()
                .map(ZoneCompositeServiceBean::toHierarchyNode)
                .toList();

        return new ZoneHierarchyNode(
                component.getId(),
                component.getName(),
                component.getZoneType(),
                component.getPowerStatus(),
                children
        );
    }

    private record LoadedHierarchy(ZoneComponent root, List<Zone> zones) {
    }
}
