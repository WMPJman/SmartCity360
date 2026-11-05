package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.PolicyCreation;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationHistorySummary;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationInput;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationSummary;
import com.wmpj.smartcity360.core.dto.PolicySummary;
import com.wmpj.smartcity360.core.dto.PolicyValidationSummary;
import com.wmpj.smartcity360.core.entity.CityPolicyType;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.PolicyManagementService;
import com.wmpj.smartcity360.web.dto.PolicyCreationRequest;
import com.wmpj.smartcity360.web.dto.PolicyEvaluationRequest;
import com.wmpj.smartcity360.web.security.BusinessApiSecurity;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.Locale;

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
public class PolicyResource {

    private static final String[] POLICY_READ_ROLES = {
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR
    };

    @EJB
    private PolicyManagementService policyManagementService;

    @Inject
    private BusinessApiSecurity businessApiSecurity;

    public PolicyResource() {
    }

    PolicyResource(
            PolicyManagementService policyManagementService,
            BusinessApiSecurity businessApiSecurity
    ) {
        this.policyManagementService = policyManagementService;
        this.businessApiSecurity = businessApiSecurity;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public PolicySummary create(PolicyCreationRequest request) {
        requireManagementRole();
        validateCreation(request);
        CityPolicyType policyType;

        try {
            policyType = CityPolicyType.valueOf(
                    request.getPolicyType().trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Policy type is invalid");
        }

        return policyManagementService.create(
                new PolicyCreation(
                        request.getPolicyCode(),
                        request.getName(),
                        request.getDescription(),
                        request.getZoneId(),
                        policyType,
                        request.getExpression()
                )
        );
    }

    @GET
    public PageResponse<PolicySummary> list(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit
    ) {
        requireReadRole();
        return policyManagementService.list(pageRequest(offset, limit));
    }

    @GET
    @Path("/{policyId}")
    public PolicySummary get(@PathParam("policyId") long policyId) {
        requireReadRole();
        requirePositiveId(policyId);
        return policyManagementService.get(policyId);
    }

    @POST
    @Path("/{policyId}/validate")
    public PolicyValidationSummary validate(@PathParam("policyId") long policyId) {
        requireManagementRole();
        requirePositiveId(policyId);
        return policyManagementService.validate(policyId);
    }

    @POST
    @Path("/{policyId}/activate")
    public PolicySummary activate(@PathParam("policyId") long policyId) {
        requireManagementRole();
        requirePositiveId(policyId);
        return policyManagementService.activate(policyId);
    }

    @POST
    @Path("/{policyId}/deactivate")
    public PolicySummary deactivate(@PathParam("policyId") long policyId) {
        requireManagementRole();
        requirePositiveId(policyId);
        return policyManagementService.deactivate(policyId);
    }

    @POST
    @Path("/{policyId}/evaluate")
    @Consumes(MediaType.APPLICATION_JSON)
    public PolicyEvaluationSummary evaluate(
            @PathParam("policyId") long policyId,
            PolicyEvaluationRequest request
    ) {
        requireReadRole();
        requirePositiveId(policyId);

        if (request == null
                || request.getNumericValues() == null
                || request.getBooleanValues() == null) {
            throw new InvalidRequestException("Policy evaluation context is incomplete");
        }

        return policyManagementService.evaluate(
                policyId,
                new PolicyEvaluationInput(
                        request.getNumericValues(),
                        request.getBooleanValues()
                )
        );
    }

    @GET
    @Path("/{policyId}/evaluations")
    public PageResponse<PolicyEvaluationHistorySummary> getEvaluations(
            @PathParam("policyId") long policyId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit
    ) {
        requireReadRole();
        requirePositiveId(policyId);
        return policyManagementService.getEvaluations(
                policyId,
                pageRequest(offset, limit)
        );
    }

    private void requireManagementRole() {
        businessApiSecurity.requireAnyRole(
                SecurityRoles.ADMIN,
                SecurityRoles.CITY_MANAGER
        );
    }

    private void requireReadRole() {
        businessApiSecurity.requireAnyRole(POLICY_READ_ROLES);
    }

    private static void validateCreation(PolicyCreationRequest request) {
        if (request == null
                || request.getPolicyCode() == null
                || request.getName() == null
                || request.getPolicyType() == null
                || request.getExpression() == null) {
            throw new InvalidRequestException("Policy creation request is incomplete");
        }

        if (!request.getPolicyCode().matches("[A-Za-z0-9_-]{1,60}")
                || request.getName().isBlank()
                || request.getName().length() > 150
                || request.getExpression().isBlank()) {
            throw new InvalidRequestException("Policy creation request is invalid");
        }

        if (request.getZoneId() != null && request.getZoneId() <= 0L) {
            throw new InvalidRequestException("Policy zone id is invalid");
        }
    }

    private static PageRequest pageRequest(int offset, int limit) {
        try {
            return new PageRequest(offset, limit);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Policy page request is invalid");
        }
    }

    private static void requirePositiveId(long policyId) {
        if (policyId <= 0L) {
            throw new InvalidRequestException("policyId must be positive");
        }
    }
}
