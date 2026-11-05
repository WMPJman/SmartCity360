package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.PolicyCreation;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationHistorySummary;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationInput;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationSummary;
import com.wmpj.smartcity360.core.dto.PolicySummary;
import com.wmpj.smartcity360.core.dto.PolicyValidationSummary;
import com.wmpj.smartcity360.core.entity.CityPolicy;
import com.wmpj.smartcity360.core.entity.CityPolicyStatus;
import com.wmpj.smartcity360.core.entity.PolicyEvaluation;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.exception.DuplicateResourceException;
import com.wmpj.smartcity360.core.exception.ResourceNotFoundException;
import com.wmpj.smartcity360.core.policy.PolicyLifecycleException;
import com.wmpj.smartcity360.core.policy.PolicyLifecyclePolicy;
import com.wmpj.smartcity360.core.policy.interpreter.PolicyEvaluationContext;
import com.wmpj.smartcity360.core.policy.interpreter.PolicyInterpretationResult;
import com.wmpj.smartcity360.core.policy.interpreter.PolicyInterpreter;
import com.wmpj.smartcity360.core.policy.interpreter.PolicySyntaxException;
import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.PolicyAccessPolicy;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.CurrentIdentityService;
import com.wmpj.smartcity360.core.service.PolicyManagementService;
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

@Stateless
@Local(PolicyManagementService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PolicyManagementServiceBean implements PolicyManagementService {

    private static final int MAXIMUM_ZONE_LEVELS = 4;
    private static final PolicyInterpreter INTERPRETER = new PolicyInterpreter();

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    @EJB
    private CurrentIdentityService currentIdentityService;

    @Override
    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.CITY_MANAGER})
    public PolicySummary create(PolicyCreation creation) {
        validateCreation(creation);
        INTERPRETER.validate(creation.expression());
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        Zone zone = resolveZone(creation.zoneId());
        requireManagement(identity, zone);
        ensureUniqueCode(creation.policyCode());
        User actor = entityManager.getReference(User.class, identity.userId());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CityPolicy policy = new CityPolicy(
                creation.policyCode().trim().toUpperCase(Locale.ROOT),
                creation.name().trim(),
                trimNullable(creation.description()),
                zone,
                creation.policyType(),
                creation.expression().trim(),
                actor,
                now
        );
        entityManager.persist(policy);
        entityManager.flush();
        return toSummary(policy);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public PageResponse<PolicySummary> list(PageRequest pageRequest) {
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        List<CityPolicy> policies;

        if (identity.roleCodes().contains(SecurityRoles.ADMIN)) {
            policies = entityManager.createQuery(
                            "SELECT p FROM CityPolicy p LEFT JOIN FETCH p.zone "
                                    + "ORDER BY p.createdAt DESC",
                            CityPolicy.class
                    )
                    .setFirstResult(pageRequest.offset())
                    .setMaxResults(pageRequest.limit())
                    .getResultList();
        } else {
            policies = entityManager.createQuery(
                            "SELECT DISTINCT p FROM CityPolicy p "
                                    + "JOIN FETCH p.zone zone "
                                    + "LEFT JOIN zone.parent parentZone "
                                    + "LEFT JOIN parentZone.parent grandparentZone "
                                    + "LEFT JOIN grandparentZone.parent cityZone, "
                                    + "UserZoneAssignment a "
                                    + "WHERE a.user.id = :userId AND (a.zone.id = zone.id "
                                    + "OR a.zone.id = parentZone.id "
                                    + "OR a.zone.id = grandparentZone.id "
                                    + "OR a.zone.id = cityZone.id) "
                                    + "ORDER BY p.createdAt DESC",
                            CityPolicy.class
                    )
                    .setParameter("userId", identity.userId())
                    .setFirstResult(pageRequest.offset())
                    .setMaxResults(pageRequest.limit())
                    .getResultList();
        }

        return new PageResponse<>(
                policies.stream()
                        .map(PolicyManagementServiceBean::toSummary)
                        .toList(),
                pageRequest.offset(),
                pageRequest.limit()
        );
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public PolicySummary get(long policyId) {
        CityPolicy policy = findPolicy(policyId);
        requireEvaluation(currentIdentityService.getCurrentIdentity(), policy.getZone());
        return toSummary(policy);
    }

    @Override
    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.CITY_MANAGER})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public PolicyValidationSummary validate(long policyId) {
        CityPolicy policy = findPolicy(policyId);
        requireManagement(currentIdentityService.getCurrentIdentity(), policy.getZone());

        try {
            INTERPRETER.validate(policy.getDslExpression());
            return new PolicyValidationSummary(true, null);
        } catch (PolicySyntaxException exception) {
            return new PolicyValidationSummary(false, exception.getMessage());
        }
    }

    @Override
    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.CITY_MANAGER})
    public PolicySummary activate(long policyId) {
        CityPolicy policy = findPolicy(policyId);
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        requireManagement(identity, policy.getZone());
        PolicyLifecyclePolicy.requireActivatable(policy.getStatus());
        INTERPRETER.validate(policy.getDslExpression());
        updateStatus(policy, CityPolicyStatus.ACTIVE, identity.userId());
        return toSummary(policy);
    }

    @Override
    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.CITY_MANAGER})
    public PolicySummary deactivate(long policyId) {
        CityPolicy policy = findPolicy(policyId);
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        requireManagement(identity, policy.getZone());
        PolicyLifecyclePolicy.requireDeactivatable(policy.getStatus());
        updateStatus(policy, CityPolicyStatus.INACTIVE, identity.userId());
        return toSummary(policy);
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public PolicyEvaluationSummary evaluate(
            long policyId,
            PolicyEvaluationInput evaluationInput
    ) {
        if (evaluationInput == null) {
            throw new IllegalArgumentException("Policy evaluation input is required");
        }

        CityPolicy policy = findPolicy(policyId);
        requireEvaluation(currentIdentityService.getCurrentIdentity(), policy.getZone());

        if (policy.getStatus() != CityPolicyStatus.ACTIVE) {
            throw new PolicyLifecycleException("Only active policies can be evaluated");
        }

        PolicyEvaluationContext context = PolicyEvaluationContext.fromTypedValues(
                evaluationInput.numericValues(),
                evaluationInput.booleanValues()
        );
        PolicyInterpretationResult result = INTERPRETER.evaluate(
                policy.getDslExpression(),
                context
        );

        return new PolicyEvaluationSummary(
                policy.getId(),
                policy.getPolicyCode(),
                policy.getDslExpression(),
                result.result(),
                result.errorMessage(),
                OffsetDateTime.now(ZoneOffset.UTC),
                false
        );
    }

    @Override
    @RolesAllowed({
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR
    })
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public PageResponse<PolicyEvaluationHistorySummary> getEvaluations(
            long policyId,
            PageRequest pageRequest
    ) {
        CityPolicy policy = findPolicy(policyId);
        requireEvaluation(currentIdentityService.getCurrentIdentity(), policy.getZone());
        List<PolicyEvaluationHistorySummary> evaluations = entityManager.createQuery(
                        "SELECT e FROM PolicyEvaluation e "
                                + "WHERE e.policy.id = :policyId ORDER BY e.evaluatedAt DESC",
                        PolicyEvaluation.class
                )
                .setParameter("policyId", policyId)
                .setFirstResult(pageRequest.offset())
                .setMaxResults(pageRequest.limit())
                .getResultList()
                .stream()
                .map(PolicyManagementServiceBean::toHistorySummary)
                .toList();

        return new PageResponse<>(
                evaluations,
                pageRequest.offset(),
                pageRequest.limit()
        );
    }

    private CityPolicy findPolicy(long policyId) {
        if (policyId <= 0L) {
            throw new IllegalArgumentException("policyId must be positive");
        }

        List<CityPolicy> policies = entityManager.createQuery(
                        "SELECT p FROM CityPolicy p LEFT JOIN FETCH p.zone "
                                + "WHERE p.id = :policyId",
                        CityPolicy.class
                )
                .setParameter("policyId", policyId)
                .setMaxResults(1)
                .getResultList();

        if (policies.isEmpty()) {
            throw new ResourceNotFoundException("Policy was not found");
        }

        return policies.get(0);
    }

    private Zone resolveZone(Long zoneId) {
        if (zoneId == null) {
            return null;
        }

        if (zoneId <= 0L) {
            throw new IllegalArgumentException("zoneId must be positive");
        }

        Zone zone = entityManager.find(Zone.class, zoneId);

        if (zone == null) {
            throw new ResourceNotFoundException("Zone was not found");
        }

        return zone;
    }

    private void requireManagement(AuthenticatedIdentity identity, Zone zone) {
        PolicyAccessPolicy.requireManagement(
                identity.roleCodes(),
                hasZoneAssignment(identity, zone)
        );
    }

    private void requireEvaluation(AuthenticatedIdentity identity, Zone zone) {
        PolicyAccessPolicy.requireEvaluation(
                identity.roleCodes(),
                hasZoneAssignment(identity, zone)
        );
    }

    private boolean hasZoneAssignment(AuthenticatedIdentity identity, Zone zone) {
        if (identity.roleCodes().contains(SecurityRoles.ADMIN)) {
            return true;
        }

        if (zone == null) {
            return false;
        }

        Long count = entityManager.createQuery(
                        "SELECT COUNT(a) FROM UserZoneAssignment a "
                                + "WHERE a.user.id = :userId AND a.zone.id IN :zoneIds",
                        Long.class
                )
                .setParameter("userId", identity.userId())
                .setParameter("zoneIds", ancestorIds(zone))
                .getSingleResult();
        return count > 0L;
    }

    private static Set<Long> ancestorIds(Zone zone) {
        Set<Long> ids = new HashSet<>();
        Zone current = zone;

        while (current != null && ids.size() < MAXIMUM_ZONE_LEVELS) {
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

    private void ensureUniqueCode(String policyCode) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(p) FROM CityPolicy p WHERE p.policyCode = :policyCode",
                        Long.class
                )
                .setParameter("policyCode", policyCode.trim().toUpperCase(Locale.ROOT))
                .getSingleResult();

        if (count > 0L) {
            throw new DuplicateResourceException("Policy code already exists");
        }
    }

    private void updateStatus(
            CityPolicy policy,
            CityPolicyStatus status,
            long actorId
    ) {
        policy.setStatus(status);
        policy.setUpdatedBy(entityManager.getReference(User.class, actorId));
        policy.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entityManager.flush();
    }

    private static void validateCreation(PolicyCreation creation) {
        if (creation == null
                || creation.policyCode() == null
                || creation.name() == null
                || creation.policyType() == null
                || creation.expression() == null) {
            throw new IllegalArgumentException("Policy creation is incomplete");
        }

        if (!creation.policyCode().matches("[A-Za-z0-9_-]{1,60}")) {
            throw new IllegalArgumentException("Policy code is invalid");
        }

        if (creation.name().isBlank() || creation.name().length() > 150) {
            throw new IllegalArgumentException("Policy name is invalid");
        }

        if (creation.description() != null && creation.description().length() > 2_000) {
            throw new IllegalArgumentException("Policy description is too long");
        }
    }

    private static String trimNullable(String value) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private static PolicySummary toSummary(CityPolicy policy) {
        Long zoneId = null;

        if (policy.getZone() != null) {
            zoneId = policy.getZone().getId();
        }

        return new PolicySummary(
                policy.getId(),
                policy.getPolicyCode(),
                policy.getName(),
                policy.getDescription(),
                zoneId,
                policy.getPolicyType(),
                policy.getDslExpression(),
                policy.getStatus(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }

    private static PolicyEvaluationHistorySummary toHistorySummary(
            PolicyEvaluation evaluation
    ) {
        return new PolicyEvaluationHistorySummary(
                evaluation.getId(),
                evaluation.getExpressionSnapshot(),
                evaluation.getResult(),
                evaluation.getErrorMessage(),
                evaluation.getEvaluatedAt()
        );
    }
}
