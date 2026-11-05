package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.complaint.ComplaintWorkflowException;
import com.wmpj.smartcity360.core.complaint.ComplaintTransitionPolicy;
import com.wmpj.smartcity360.core.complaint.chain.ComplaintChainFactory;
import com.wmpj.smartcity360.core.complaint.chain.ComplaintHandlingContext;
import com.wmpj.smartcity360.core.complaint.chain.ComplaintHandlingResult;
import com.wmpj.smartcity360.core.dto.ComplaintHistorySummary;
import com.wmpj.smartcity360.core.dto.ComplaintSubmission;
import com.wmpj.smartcity360.core.dto.ComplaintSummary;
import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.entity.Complaint;
import com.wmpj.smartcity360.core.entity.ComplaintCategory;
import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintHistory;
import com.wmpj.smartcity360.core.entity.ComplaintHistoryAction;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserStatus;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.exception.ResourceNotFoundException;
import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.ComplaintAccessPolicy;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.ComplaintManagementService;
import com.wmpj.smartcity360.core.service.CurrentIdentityService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Stateless
@Local(ComplaintManagementService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ComplaintManagementServiceBean implements ComplaintManagementService {

    private static final int MAX_HISTORY_RESULTS = 200;

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    @EJB
    private CurrentIdentityService currentIdentityService;

    @Override
    @RolesAllowed({SecurityRoles.RESIDENT, SecurityRoles.ADMIN})
    public ComplaintSummary submit(ComplaintSubmission submission) {
        validateSubmission(submission);
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        User resident = entityManager.getReference(User.class, identity.userId());
        Zone zone = entityManager.find(Zone.class, submission.zoneId());

        if (zone == null) {
            throw new ResourceNotFoundException("Zone was not found");
        }

        ComplaintCategory category = findCategory(submission.categoryCode());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Complaint complaint = new Complaint(
                "CMP-" + UUID.randomUUID(),
                resident,
                zone,
                category,
                submission.title().trim(),
                submission.description().trim(),
                submission.severity(),
                now
        );
        entityManager.persist(complaint);
        entityManager.flush();
        persistHistory(
                complaint,
                resident,
                ComplaintHistoryAction.SUBMITTED,
                null,
                ComplaintHandlerLevel.LOCAL_STAFF,
                null,
                ComplaintStatus.SUBMITTED,
                "Complaint submitted",
                now
        );
        return toSummary(complaint);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF,
            SecurityRoles.RESIDENT
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public PageResponse<ComplaintSummary> list(PageRequest pageRequest) {
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        List<Complaint> complaints;

        if (identity.roleCodes().contains(SecurityRoles.ADMIN)) {
            complaints = entityManager.createQuery(
                            "SELECT c FROM Complaint c ORDER BY c.createdAt DESC",
                            Complaint.class
                    )
                    .setFirstResult(pageRequest.offset())
                    .setMaxResults(pageRequest.limit())
                    .getResultList();
        } else if (identity.roleCodes().contains(SecurityRoles.RESIDENT)) {
            complaints = entityManager.createQuery(
                            "SELECT c FROM Complaint c WHERE c.resident.id = :userId "
                                    + "ORDER BY c.createdAt DESC",
                            Complaint.class
                    )
                    .setParameter("userId", identity.userId())
                    .setFirstResult(pageRequest.offset())
                    .setMaxResults(pageRequest.limit())
                    .getResultList();
        } else {
            complaints = entityManager.createQuery(
                            "SELECT DISTINCT c FROM Complaint c "
                                    + "LEFT JOIN c.zone.parent parentZone "
                                    + "LEFT JOIN parentZone.parent grandparentZone "
                                    + "LEFT JOIN grandparentZone.parent cityZone, "
                                    + "UserZoneAssignment a "
                                    + "WHERE a.user.id = :userId AND (a.zone.id = c.zone.id "
                                    + "OR a.zone.id = parentZone.id "
                                    + "OR a.zone.id = grandparentZone.id "
                                    + "OR a.zone.id = cityZone.id) "
                                    + "ORDER BY c.createdAt DESC",
                            Complaint.class
                    )
                    .setParameter("userId", identity.userId())
                    .setFirstResult(pageRequest.offset())
                    .setMaxResults(pageRequest.limit())
                    .getResultList();
        }

        return new PageResponse<>(
                complaints.stream().map(ComplaintManagementServiceBean::toSummary).toList(),
                pageRequest.offset(),
                pageRequest.limit()
        );
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF,
            SecurityRoles.RESIDENT
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public ComplaintSummary get(long complaintId) {
        Complaint complaint = findComplaint(complaintId);
        requireViewAccess(currentIdentityService.getCurrentIdentity(), complaint);
        return toSummary(complaint);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF,
            SecurityRoles.RESIDENT
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<ComplaintHistorySummary> getHistory(long complaintId) {
        Complaint complaint = findComplaint(complaintId);
        requireViewAccess(currentIdentityService.getCurrentIdentity(), complaint);
        return entityManager.createQuery(
                        "SELECT h FROM ComplaintHistory h WHERE h.complaint.id = :complaintId "
                                + "ORDER BY h.createdAt, h.id",
                        ComplaintHistory.class
                )
                .setParameter("complaintId", complaintId)
                .setMaxResults(MAX_HISTORY_RESULTS)
                .getResultList()
                .stream()
                .map(ComplaintManagementServiceBean::toHistorySummary)
                .toList();
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    public ComplaintSummary route(long complaintId) {
        Complaint complaint = findComplaint(complaintId);
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        requireOperationalScope(identity, complaint);

        ComplaintTransitionPolicy.requireRoutable(complaint.getStatus());

        ComplaintHandlingResult result = handle(complaint);
        User assignee = findAssignee(complaint, result.responsibleLevel());
        ComplaintStatus previousStatus = complaint.getStatus();
        ComplaintHandlerLevel previousLevel = complaint.getCurrentHandlerLevel();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        complaint.setAssignedTo(assignee);
        complaint.setCurrentHandlerLevel(result.responsibleLevel());
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setUpdatedAt(now);
        persistHistory(
                complaint,
                entityManager.getReference(User.class, identity.userId()),
                ComplaintHistoryAction.ASSIGNED,
                previousLevel,
                result.responsibleLevel(),
                previousStatus,
                ComplaintStatus.IN_PROGRESS,
                "Complaint routed through handler chain",
                now
        );
        entityManager.flush();
        return toSummary(complaint);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    public ComplaintSummary escalate(long complaintId, String reason) {
        Complaint complaint = findComplaint(complaintId);
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        requireOperationalScope(identity, complaint);

        ComplaintTransitionPolicy.requireEscalatable(complaint.getStatus());

        ComplaintHandlerLevel previousLevel = complaint.getCurrentHandlerLevel();
        ComplaintHandlerLevel nextLevel = ComplaintTransitionPolicy.nextLevel(previousLevel);
        complaint.setCurrentHandlerLevel(nextLevel);
        ComplaintHandlingResult result = handle(complaint);
        User assignee = findAssignee(complaint, result.responsibleLevel());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        complaint.setAssignedTo(assignee);
        complaint.setStatus(ComplaintStatus.ESCALATED);
        complaint.setUpdatedAt(now);
        persistHistory(
                complaint,
                entityManager.getReference(User.class, identity.userId()),
                ComplaintHistoryAction.ESCALATED,
                previousLevel,
                result.responsibleLevel(),
                ComplaintStatus.IN_PROGRESS,
                ComplaintStatus.ESCALATED,
                requireNotes(reason, "Escalation reason"),
                now
        );
        entityManager.flush();
        return toSummary(complaint);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    })
    public ComplaintSummary resolve(long complaintId, String notes) {
        Complaint complaint = findComplaint(complaintId);
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        requireResolutionAccess(identity, complaint);

        ComplaintTransitionPolicy.requireResolvable(complaint.getStatus());

        ComplaintStatus previousStatus = complaint.getStatus();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(now);
        complaint.setUpdatedAt(now);
        persistHistory(
                complaint,
                entityManager.getReference(User.class, identity.userId()),
                ComplaintHistoryAction.RESOLVED,
                complaint.getCurrentHandlerLevel(),
                complaint.getCurrentHandlerLevel(),
                previousStatus,
                ComplaintStatus.RESOLVED,
                requireNotes(notes, "Resolution notes"),
                now
        );
        entityManager.flush();
        return toSummary(complaint);
    }

    private ComplaintHandlingResult handle(Complaint complaint) {
        ComplaintHandlingResult result = new ComplaintChainFactory().create().handle(
                new ComplaintHandlingContext(
                        complaint.getId(),
                        complaint.getSeverity(),
                        complaint.getStatus(),
                        complaint.getCurrentHandlerLevel()
                )
        );

        if (!result.handled()) {
            throw new ComplaintWorkflowException("No eligible complaint handler was found");
        }

        return result;
    }

    private User findAssignee(
            Complaint complaint,
            ComplaintHandlerLevel handlerLevel
    ) {
        String roleCode = handlerLevel.name();
        Set<Long> zoneIds = ancestorIds(complaint.getZone());
        List<User> candidates = entityManager.createQuery(
                        "SELECT DISTINCT ur.user FROM UserRole ur, UserZoneAssignment a "
                                + "WHERE ur.user = a.user AND ur.role.code = :roleCode "
                                + "AND a.zone.id IN :zoneIds AND ur.user.status = :activeStatus "
                                + "ORDER BY ur.user.id",
                        User.class
                )
                .setParameter("roleCode", roleCode)
                .setParameter("zoneIds", zoneIds)
                .setParameter("activeStatus", UserStatus.ACTIVE)
                .setMaxResults(1)
                .getResultList();

        if (candidates.isEmpty()) {
            throw new ComplaintWorkflowException("No eligible assigned handler is available");
        }

        return candidates.get(0);
    }

    private void requireViewAccess(
            AuthenticatedIdentity identity,
            Complaint complaint
    ) {
        boolean residentOwner = complaint.getResident().getId().equals(identity.userId());
        boolean operationalZoneScope = hasZoneAssignment(identity.userId(), complaint.getZone());
        ComplaintAccessPolicy.requireViewAccess(
                identity.roleCodes(),
                residentOwner,
                operationalZoneScope
        );
    }

    private void requireOperationalScope(
            AuthenticatedIdentity identity,
            Complaint complaint
    ) {
        ComplaintAccessPolicy.requireOperationalAccess(
                identity.roleCodes(),
                hasZoneAssignment(identity.userId(), complaint.getZone())
        );
    }

    private void requireResolutionAccess(
            AuthenticatedIdentity identity,
            Complaint complaint
    ) {
        boolean assignedActor = complaint.getAssignedTo() != null
                && complaint.getAssignedTo().getId().equals(identity.userId());
        ComplaintAccessPolicy.requireResolutionAccess(
                identity.roleCodes(),
                hasZoneAssignment(identity.userId(), complaint.getZone()),
                assignedActor
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

        while (current != null && ids.size() < 4) {
            if (!ids.add(current.getId())) {
                throw new ComplaintWorkflowException("Zone hierarchy contains a cycle");
            }

            current = current.getParent();
        }

        return ids;
    }

    private Complaint findComplaint(long complaintId) {
        if (complaintId <= 0L) {
            throw new IllegalArgumentException("complaintId must be positive");
        }

        Complaint complaint = entityManager.find(Complaint.class, complaintId);

        if (complaint == null) {
            throw new ResourceNotFoundException("Complaint was not found");
        }

        return complaint;
    }

    private ComplaintCategory findCategory(String categoryCode) {
        List<ComplaintCategory> categories = entityManager.createQuery(
                        "SELECT c FROM ComplaintCategory c "
                                + "WHERE UPPER(c.code) = :code AND c.active = true",
                        ComplaintCategory.class
                )
                .setParameter("code", categoryCode.trim().toUpperCase(Locale.ROOT))
                .setMaxResults(1)
                .getResultList();

        if (categories.isEmpty()) {
            throw new ResourceNotFoundException("Complaint category was not found");
        }

        return categories.get(0);
    }

    private void persistHistory(
            Complaint complaint,
            User actor,
            ComplaintHistoryAction action,
            ComplaintHandlerLevel fromLevel,
            ComplaintHandlerLevel toLevel,
            ComplaintStatus fromStatus,
            ComplaintStatus toStatus,
            String notes,
            OffsetDateTime createdAt
    ) {
        entityManager.persist(
                new ComplaintHistory(
                        complaint,
                        actor,
                        action,
                        fromLevel,
                        toLevel,
                        fromStatus,
                        toStatus,
                        notes,
                        createdAt
                )
        );
    }

    private static void validateSubmission(ComplaintSubmission submission) {
        if (submission == null
                || submission.zoneId() <= 0L
                || submission.categoryCode() == null
                || submission.title() == null
                || submission.description() == null
                || submission.severity() == null) {
            throw new IllegalArgumentException("Complaint submission is incomplete");
        }

        if (submission.title().isBlank() || submission.title().length() > 200) {
            throw new IllegalArgumentException("Complaint title is invalid");
        }

        if (submission.description().isBlank() || submission.description().length() > 5_000) {
            throw new IllegalArgumentException("Complaint description is invalid");
        }
    }

    private static String requireNotes(String notes, String fieldName) {
        if (notes == null || notes.isBlank() || notes.length() > 2_000) {
            throw new ComplaintWorkflowException(fieldName + " is invalid");
        }

        return notes.trim();
    }

    private static ComplaintSummary toSummary(Complaint complaint) {
        Long assignedUserId = null;

        if (complaint.getAssignedTo() != null) {
            assignedUserId = complaint.getAssignedTo().getId();
        }

        return new ComplaintSummary(
                complaint.getId(),
                complaint.getComplaintNumber(),
                complaint.getZone().getId(),
                complaint.getCategory().getCode(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getSeverity(),
                complaint.getStatus(),
                complaint.getCurrentHandlerLevel(),
                assignedUserId,
                complaint.getCreatedAt(),
                complaint.getUpdatedAt(),
                complaint.getResolvedAt()
        );
    }

    private static ComplaintHistorySummary toHistorySummary(ComplaintHistory history) {
        return new ComplaintHistorySummary(
                history.getId(),
                history.getAction(),
                history.getFromHandlerLevel(),
                history.getToHandlerLevel(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getNotes(),
                history.getCreatedAt()
        );
    }
}
