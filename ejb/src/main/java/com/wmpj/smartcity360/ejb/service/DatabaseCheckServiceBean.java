package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.service.DatabaseCheckService;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Local(DatabaseCheckService.class)
public class DatabaseCheckServiceBean implements DatabaseCheckService {

    private static final Logger LOGGER = Logger.getLogger(DatabaseCheckServiceBean.class.getName());
    private static final Set<String> EXPECTED_ROLE_CODES = Set.of(
            "ADMIN",
            "CITY_MANAGER",
            "SUPERVISOR",
            "LOCAL_STAFF",
            "RESIDENT"
    );

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    public DatabaseCheckServiceBean() {
    }

    DatabaseCheckServiceBean(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public long countRoles() {
        long publicTableCount = countPublicTables();
        List<Role> roles = entityManager
                .createQuery("SELECT r FROM Role r ORDER BY r.id", Role.class)
                .getResultList();
        Set<String> roleCodes = new TreeSet<>();
        for (Role role : roles) {
            roleCodes.add(role.getCode());
        }
        if (roles.size() != EXPECTED_ROLE_CODES.size() || !roleCodes.equals(EXPECTED_ROLE_CODES)) {
            throw new IllegalStateException("Seeded role verification failed");
        }

        long userCount = entityManager
                .createQuery("SELECT COUNT(u) FROM SmartCityUser u", Long.class)
                .getSingleResult();
        long userRoleCount = entityManager
                .createQuery("SELECT COUNT(ur) FROM UserRole ur", Long.class)
                .getSingleResult();

        long zoneCount = entityManager
                .createQuery("SELECT COUNT(z) FROM Zone z", Long.class)
                .getSingleResult();
        var rootCityQuery = entityManager.createQuery(
                "SELECT z FROM Zone z WHERE z.parent IS NULL AND z.zoneType = :zoneType ORDER BY z.id",
                Zone.class
        );
        rootCityQuery.setParameter("zoneType", ZoneType.CITY);
        List<Zone> rootCities = rootCityQuery.getResultList();
        long zonesWithParents = entityManager
                .createQuery("SELECT COUNT(z) FROM Zone z WHERE z.parent IS NOT NULL", Long.class)
                .getSingleResult();
        long childLinks = entityManager
                .createQuery("SELECT COUNT(c) FROM Zone p JOIN p.children c", Long.class)
                .getSingleResult();

        long energyMeterCount = entityManager
                .createQuery("SELECT COUNT(m) FROM EnergyMeter m", Long.class)
                .getSingleResult();
        long metersWithZones = entityManager
                .createQuery("SELECT COUNT(m) FROM EnergyMeter m WHERE m.zone IS NOT NULL", Long.class)
                .getSingleResult();
        long energyReadingCount = entityManager
                .createQuery("SELECT COUNT(r) FROM EnergyReading r", Long.class)
                .getSingleResult();
        long readingsWithMeters = entityManager
                .createQuery("SELECT COUNT(r) FROM EnergyReading r WHERE r.meter IS NOT NULL", Long.class)
                .getSingleResult();
        List<Object[]> readingValues = entityManager.createQuery(
                "SELECT r.intervalStart, r.intervalEnd, r.consumptionKwh FROM EnergyReading r ORDER BY r.id",
                Object[].class
        ).getResultList();
        long streamConfigurationCount = entityManager
                .createQuery("SELECT COUNT(c) FROM StreamConfiguration c", Long.class)
                .getSingleResult();
        long configurationsWithMeters = entityManager.createQuery(
                "SELECT COUNT(c) FROM StreamConfiguration c WHERE c.meter IS NOT NULL",
                Long.class
        ).getSingleResult();
        long configurationsWithUpdaters = entityManager.createQuery(
                "SELECT COUNT(c) FROM StreamConfiguration c WHERE c.updatedBy IS NOT NULL",
                Long.class
        ).getSingleResult();
        List<String> layerOrders = entityManager.createQuery(
                "SELECT c.layerOrder FROM StreamConfiguration c ORDER BY c.id",
                String.class
        ).getResultList();

        long complaintCategoryCount = entityManager
                .createQuery("SELECT COUNT(c) FROM ComplaintCategory c", Long.class)
                .getSingleResult();
        List<String> complaintCategoryCodes = entityManager.createQuery(
                "SELECT c.code FROM ComplaintCategory c ORDER BY c.id",
                String.class
        ).getResultList();
        long complaintCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Complaint c", Long.class)
                .getSingleResult();
        long complaintsWithCategories = entityManager.createQuery(
                "SELECT COUNT(c) FROM Complaint c WHERE c.category IS NOT NULL",
                Long.class
        ).getSingleResult();
        long complaintsWithResidents = entityManager.createQuery(
                "SELECT COUNT(c) FROM Complaint c WHERE c.resident IS NOT NULL",
                Long.class
        ).getSingleResult();
        long complaintsWithZones = entityManager.createQuery(
                "SELECT COUNT(c) FROM Complaint c WHERE c.zone IS NOT NULL",
                Long.class
        ).getSingleResult();
        long complaintsWithAssignees = entityManager.createQuery(
                "SELECT COUNT(c) FROM Complaint c WHERE c.assignedTo IS NOT NULL",
                Long.class
        ).getSingleResult();
        List<Object[]> complaintStates = entityManager.createQuery(
                "SELECT c.status, c.severity, c.currentHandlerLevel FROM Complaint c ORDER BY c.id",
                Object[].class
        ).getResultList();
        long complaintHistoryCount = entityManager
                .createQuery("SELECT COUNT(h) FROM ComplaintHistory h", Long.class)
                .getSingleResult();
        long historyWithComplaints = entityManager.createQuery(
                "SELECT COUNT(h) FROM ComplaintHistory h WHERE h.complaint IS NOT NULL",
                Long.class
        ).getSingleResult();
        long historyWithActors = entityManager.createQuery(
                "SELECT COUNT(h) FROM ComplaintHistory h WHERE h.actor IS NOT NULL",
                Long.class
        ).getSingleResult();
        List<Object[]> historyTransitions = entityManager.createQuery(
                "SELECT h.action, h.fromStatus, h.toStatus FROM ComplaintHistory h ORDER BY h.id",
                Object[].class
        ).getResultList();

        long streetlightProfileCount = entityManager.createQuery("SELECT COUNT(p) FROM StreetlightProfile p", Long.class).getSingleResult();
        long streetlightCount = entityManager.createQuery("SELECT COUNT(s) FROM Streetlight s", Long.class).getSingleResult();
        long streetlightsWithReferences = entityManager.createQuery("SELECT COUNT(s) FROM Streetlight s WHERE s.profile IS NOT NULL AND s.zone IS NOT NULL", Long.class).getSingleResult();
        long cityPolicyCount = entityManager.createQuery("SELECT COUNT(p) FROM CityPolicy p", Long.class).getSingleResult();
        long policiesWithCreators = entityManager.createQuery("SELECT COUNT(p) FROM CityPolicy p WHERE p.createdBy IS NOT NULL", Long.class).getSingleResult();
        long policyEvaluationCount = entityManager.createQuery("SELECT COUNT(e) FROM PolicyEvaluation e", Long.class).getSingleResult();
        long evaluationsWithPolicies = entityManager.createQuery("SELECT COUNT(e) FROM PolicyEvaluation e WHERE e.policy IS NOT NULL", Long.class).getSingleResult();
        List<String> policyContexts = entityManager.createQuery("SELECT e.inputContext FROM PolicyEvaluation e ORDER BY e.id", String.class).getResultList();
        long assignmentCount = entityManager.createQuery("SELECT COUNT(a) FROM UserZoneAssignment a", Long.class).getSingleResult();
        long assignmentsWithReferences = entityManager.createQuery("SELECT COUNT(a) FROM UserZoneAssignment a WHERE a.user IS NOT NULL AND a.zone IS NOT NULL", Long.class).getSingleResult();
        long notificationCount = entityManager.createQuery("SELECT COUNT(n) FROM Notification n", Long.class).getSingleResult();
        long notificationsWithUsers = entityManager.createQuery("SELECT COUNT(n) FROM Notification n WHERE n.user IS NOT NULL", Long.class).getSingleResult();
        long auditLogCount = entityManager.createQuery("SELECT COUNT(l) FROM AuditLog l", Long.class).getSingleResult();
        List<String> auditDetails = entityManager.createQuery("SELECT l.details FROM AuditLog l ORDER BY l.id", String.class).getResultList();

        LOGGER.log(Level.INFO,
                "SmartCity360 verification: tables={0}, roles={1}, roleCodes={2}, users={3}, userRoles={4}, "
                        + "zones={5}, rootCities={6}, zonesWithParents={7}, childLinks={8}, "
                        + "energyMeters={9}, metersWithZones={10}, energyReadings={11}, "
                        + "readingsWithMeters={12}, readingValues={13}, streamConfigurations={14}, "
                        + "configurationsWithMeters={15}, configurationsWithUpdaters={16}, layerOrders={17}, "
                        + "complaintCategories={18}, complaintCategoryCodes={19}, complaints={20}, "
                        + "complaintsWithCategories={21}, complaintsWithResidents={22}, complaintsWithZones={23}, "
                        + "complaintsWithAssignees={24}, complaintStates={25}, complaintHistory={26}, "
                        + "historyWithComplaints={27}, historyWithActors={28}, historyTransitions={29}, "
                        + "streetlightProfiles={30}, streetlights={31}, streetlightsWithReferences={32}, "
                        + "cityPolicies={33}, policiesWithCreators={34}, policyEvaluations={35}, evaluationsWithPolicies={36}, policyContexts={37}, "
                        + "userZoneAssignments={38}, assignmentsWithReferences={39}, notifications={40}, notificationsWithUsers={41}, auditLogs={42}, auditDetails={43}",
                new Object[]{
                        publicTableCount,
                        roles.size(),
                        roleCodes,
                        userCount,
                        userRoleCount,
                        zoneCount,
                        rootCities.size(),
                        zonesWithParents,
                        childLinks,
                        energyMeterCount,
                        metersWithZones,
                        energyReadingCount,
                        readingsWithMeters,
                        readingValues.size(),
                        streamConfigurationCount,
                        configurationsWithMeters,
                        configurationsWithUpdaters,
                        layerOrders.size(),
                        complaintCategoryCount,
                        complaintCategoryCodes,
                        complaintCount,
                        complaintsWithCategories,
                        complaintsWithResidents,
                        complaintsWithZones,
                        complaintsWithAssignees,
                        complaintStates.size(),
                        complaintHistoryCount,
                        historyWithComplaints,
                        historyWithActors,
                        historyTransitions.size(),
                        streetlightProfileCount, streetlightCount, streetlightsWithReferences,
                        cityPolicyCount, policiesWithCreators, policyEvaluationCount, evaluationsWithPolicies, policyContexts.size(),
                        assignmentCount, assignmentsWithReferences, notificationCount, notificationsWithUsers, auditLogCount, auditDetails.size()
                });
        return roles.size();
    }

    long countPublicTables() {
        Number count = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'")
                .getSingleResult();
        return count.longValue();
    }
}
