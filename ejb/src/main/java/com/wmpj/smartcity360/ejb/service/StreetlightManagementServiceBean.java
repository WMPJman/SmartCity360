package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.dto.LightingProfileSummary;
import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.ProfileSharingSummary;
import com.wmpj.smartcity360.core.dto.StreetlightSummary;
import com.wmpj.smartcity360.core.entity.Streetlight;
import com.wmpj.smartcity360.core.entity.StreetlightProfile;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.exception.ResourceNotFoundException;
import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.security.StreetlightAccessPolicy;
import com.wmpj.smartcity360.core.service.CurrentIdentityService;
import com.wmpj.smartcity360.core.service.StreetlightManagementService;
import com.wmpj.smartcity360.core.streetlight.StreetlightStatusChange;
import com.wmpj.smartcity360.core.streetlight.flyweight.LightingProfileDefinition;
import com.wmpj.smartcity360.core.streetlight.flyweight.LightingProfileFlyweight;
import com.wmpj.smartcity360.core.streetlight.flyweight.LightingProfileFlyweightFactory;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@Stateless
@Local(StreetlightManagementService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class StreetlightManagementServiceBean implements StreetlightManagementService {

    private static final int MAX_ZONE_LEVELS = 4;
    private static final LightingProfileFlyweightFactory FLYWEIGHT_FACTORY =
            new LightingProfileFlyweightFactory();

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
    public PageResponse<StreetlightSummary> list(
            Long zoneId,
            PageRequest pageRequest
    ) {
        validateZoneId(zoneId);
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        List<Streetlight> streetlights = loadVisibleStreetlights(
                identity,
                zoneId,
                pageRequest
        );

        return new PageResponse<>(
                streetlights.stream()
                        .map(StreetlightManagementServiceBean::toSummary)
                        .toList(),
                pageRequest.offset(),
                pageRequest.limit()
        );
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public StreetlightSummary get(long streetlightId) {
        Streetlight streetlight = findStreetlight(streetlightId);
        authorize(streetlight, AccessOperation.READ);
        return toSummary(streetlight);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public LightingProfileSummary getProfile(long streetlightId) {
        Streetlight streetlight = findStreetlight(streetlightId);
        authorize(streetlight, AccessOperation.READ);
        return toProfileSummary(resolveFlyweight(streetlight.getProfile()));
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    public StreetlightSummary updateStatus(
            long streetlightId,
            StreetlightStatusChange statusChange
    ) {
        if (statusChange == null) {
            throw new IllegalArgumentException("Streetlight status change is required");
        }

        Streetlight streetlight = findStreetlight(streetlightId);
        authorize(streetlight, AccessOperation.STATUS_CONTROL);
        streetlight.setOn(statusChange.on());
        streetlight.setBrightness(statusChange.brightness());
        streetlight.setOperationalStatus(statusChange.operationalStatus());
        streetlight.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entityManager.flush();
        return toSummary(streetlight);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR
    })
    public StreetlightSummary assignProfile(long streetlightId, long profileId) {
        if (profileId <= 0L) {
            throw new IllegalArgumentException("profileId must be positive");
        }

        Streetlight streetlight = findStreetlight(streetlightId);
        authorize(streetlight, AccessOperation.PROFILE_ASSIGNMENT);
        StreetlightProfile profile = entityManager.find(StreetlightProfile.class, profileId);

        if (profile == null) {
            throw new ResourceNotFoundException("Streetlight profile was not found");
        }

        streetlight.setProfile(profile);
        streetlight.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entityManager.flush();
        return toSummary(streetlight);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public ProfileSharingSummary getProfileSharing(
            Long zoneId,
            PageRequest pageRequest
    ) {
        validateZoneId(zoneId);
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        List<Streetlight> streetlights = loadVisibleStreetlights(
                identity,
                zoneId,
                pageRequest
        );
        Set<LightingProfileFlyweight> distinctProfiles = Collections.newSetFromMap(
                new IdentityHashMap<>()
        );

        for (Streetlight streetlight : streetlights) {
            distinctProfiles.add(resolveFlyweight(streetlight.getProfile()));
        }

        int reusedReferences = streetlights.size() - distinctProfiles.size();
        return new ProfileSharingSummary(
                streetlights.size(),
                distinctProfiles.size(),
                reusedReferences,
                FLYWEIGHT_FACTORY.cachedProfileCount(),
                pageRequest.offset(),
                pageRequest.limit()
        );
    }

    private List<Streetlight> loadVisibleStreetlights(
            AuthenticatedIdentity identity,
            Long zoneId,
            PageRequest pageRequest
    ) {
        String zoneFilter = "";

        if (zoneId != null) {
            zoneFilter = " AND s.zone.id = :zoneId";
        }

        TypedQuery<Streetlight> query;

        if (identity.roleCodes().contains(SecurityRoles.ADMIN)) {
            query = entityManager.createQuery(
                    "SELECT s FROM Streetlight s "
                            + "JOIN FETCH s.profile JOIN FETCH s.zone "
                            + "WHERE 1 = 1" + zoneFilter + " ORDER BY s.id",
                    Streetlight.class
            );
        } else {
            query = entityManager.createQuery(
                    "SELECT DISTINCT s FROM Streetlight s "
                            + "JOIN FETCH s.profile JOIN FETCH s.zone "
                            + "LEFT JOIN s.zone.parent parentZone "
                            + "LEFT JOIN parentZone.parent grandparentZone "
                            + "LEFT JOIN grandparentZone.parent cityZone, "
                            + "UserZoneAssignment a "
                            + "WHERE a.user.id = :userId AND (a.zone.id = s.zone.id "
                            + "OR a.zone.id = parentZone.id "
                            + "OR a.zone.id = grandparentZone.id "
                            + "OR a.zone.id = cityZone.id)"
                            + zoneFilter + " ORDER BY s.id",
                    Streetlight.class
            ).setParameter("userId", identity.userId());
        }

        if (zoneId != null) {
            query.setParameter("zoneId", zoneId);
        }

        return query.setFirstResult(pageRequest.offset())
                .setMaxResults(pageRequest.limit())
                .getResultList();
    }

    private Streetlight findStreetlight(long streetlightId) {
        if (streetlightId <= 0L) {
            throw new IllegalArgumentException("streetlightId must be positive");
        }

        List<Streetlight> streetlights = entityManager.createQuery(
                        "SELECT s FROM Streetlight s JOIN FETCH s.profile "
                                + "JOIN FETCH s.zone WHERE s.id = :streetlightId",
                        Streetlight.class
                )
                .setParameter("streetlightId", streetlightId)
                .setMaxResults(1)
                .getResultList();

        if (streetlights.isEmpty()) {
            throw new ResourceNotFoundException("Streetlight was not found");
        }

        return streetlights.get(0);
    }

    private void authorize(Streetlight streetlight, AccessOperation operation) {
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        boolean assignedToZone = hasZoneAssignment(identity.userId(), streetlight.getZone());

        if (operation == AccessOperation.READ) {
            StreetlightAccessPolicy.requireRead(identity.roleCodes(), assignedToZone);
            return;
        }

        if (operation == AccessOperation.STATUS_CONTROL) {
            StreetlightAccessPolicy.requireStatusControl(
                    identity.roleCodes(),
                    assignedToZone
            );
            return;
        }

        StreetlightAccessPolicy.requireProfileAssignment(
                identity.roleCodes(),
                assignedToZone
        );
    }

    private boolean hasZoneAssignment(long userId, Zone zone) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(a) FROM UserZoneAssignment a "
                                + "WHERE a.user.id = :userId AND a.zone.id IN :zoneIds",
                        Long.class
                )
                .setParameter("userId", userId)
                .setParameter("zoneIds", ancestorIds(zone))
                .getSingleResult();
        return count > 0L;
    }

    private static Set<Long> ancestorIds(Zone zone) {
        Set<Long> ids = new HashSet<>();
        Zone current = zone;

        while (current != null && ids.size() < MAX_ZONE_LEVELS) {
            if (!ids.add(current.getId())) {
                throw new IllegalArgumentException("Zone hierarchy contains a cycle");
            }

            current = current.getParent();
        }

        if (current != null) {
            throw new IllegalArgumentException("Zone hierarchy exceeds the supported depth");
        }

        return ids;
    }

    private static LightingProfileFlyweight resolveFlyweight(
            StreetlightProfile profile
    ) {
        return FLYWEIGHT_FACTORY.getOrCreate(
                new LightingProfileDefinition(
                        profile.getId(),
                        profile.getProfileCode(),
                        profile.getName(),
                        profile.getManufacturer(),
                        profile.getModel(),
                        profile.getWattage(),
                        profile.getLightType(),
                        profile.getDefaultBrightness(),
                        profile.getFirmwareVersion()
                )
        );
    }

    private static StreetlightSummary toSummary(Streetlight streetlight) {
        return new StreetlightSummary(
                streetlight.getId(),
                streetlight.getLightCode(),
                streetlight.getZone().getId(),
                streetlight.getLatitude(),
                streetlight.getLongitude(),
                streetlight.isOn(),
                streetlight.getBrightness(),
                streetlight.getOperationalStatus(),
                streetlight.getProfile().getId(),
                streetlight.getProfile().getProfileCode(),
                streetlight.getUpdatedAt()
        );
    }

    private static LightingProfileSummary toProfileSummary(
            LightingProfileFlyweight profile
    ) {
        return new LightingProfileSummary(
                profile.profileId(),
                profile.profileCode(),
                profile.name(),
                profile.manufacturer(),
                profile.model(),
                profile.wattage(),
                profile.lightType(),
                profile.defaultBrightness(),
                profile.firmwareVersion()
        );
    }

    private static void validateZoneId(Long zoneId) {
        if (zoneId != null && zoneId <= 0L) {
            throw new IllegalArgumentException("zoneId must be positive");
        }
    }

    private enum AccessOperation {
        READ,
        STATUS_CONTROL,
        PROFILE_ASSIGNMENT
    }
}
