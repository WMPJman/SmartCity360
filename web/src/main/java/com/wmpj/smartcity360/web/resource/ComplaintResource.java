package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.dto.ComplaintHistorySummary;
import com.wmpj.smartcity360.core.dto.ComplaintSubmission;
import com.wmpj.smartcity360.core.dto.ComplaintSummary;
import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.entity.ComplaintSeverity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.ComplaintManagementService;
import com.wmpj.smartcity360.web.dto.ComplaintActionRequest;
import com.wmpj.smartcity360.web.dto.ComplaintSubmissionRequest;
import com.wmpj.smartcity360.web.security.BusinessApiSecurity;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Locale;

@Path("/complaints")
@Produces(MediaType.APPLICATION_JSON)
public class ComplaintResource {

    private static final String[] ALL_COMPLAINT_ROLES = {
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF,
            SecurityRoles.RESIDENT
    };

    private static final String[] OPERATIONAL_ROLES = {
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    };

    @EJB
    private ComplaintManagementService complaintManagementService;

    @Inject
    private BusinessApiSecurity businessApiSecurity;

    public ComplaintResource() {
    }

    ComplaintResource(
            ComplaintManagementService complaintManagementService,
            BusinessApiSecurity businessApiSecurity
    ) {
        this.complaintManagementService = complaintManagementService;
        this.businessApiSecurity = businessApiSecurity;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ComplaintSummary submit(ComplaintSubmissionRequest request) {
        businessApiSecurity.requireAnyRole(SecurityRoles.RESIDENT, SecurityRoles.ADMIN);
        validateSubmission(request);

        ComplaintSeverity severity;

        try {
            severity = ComplaintSeverity.valueOf(
                    request.getSeverity().trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Complaint severity is invalid");
        }

        return complaintManagementService.submit(
                new ComplaintSubmission(
                        request.getZoneId(),
                        request.getCategoryCode(),
                        request.getTitle(),
                        request.getDescription(),
                        severity
                )
        );
    }

    @GET
    public PageResponse<ComplaintSummary> list(
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit
    ) {
        businessApiSecurity.requireAnyRole(ALL_COMPLAINT_ROLES);
        int effectiveOffset = 0;
        int effectiveLimit = PageRequest.DEFAULT_LIMIT;

        if (offset != null) {
            effectiveOffset = offset;
        }

        if (limit != null) {
            effectiveLimit = limit;
        }

        try {
            return complaintManagementService.list(
                    new PageRequest(effectiveOffset, effectiveLimit)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Complaint page request is invalid");
        }
    }

    @GET
    @Path("/{complaintId}")
    public ComplaintSummary get(@PathParam("complaintId") long complaintId) {
        businessApiSecurity.requireAnyRole(ALL_COMPLAINT_ROLES);
        requirePositiveId(complaintId);
        return complaintManagementService.get(complaintId);
    }

    @GET
    @Path("/{complaintId}/history")
    public List<ComplaintHistorySummary> history(
            @PathParam("complaintId") long complaintId
    ) {
        businessApiSecurity.requireAnyRole(ALL_COMPLAINT_ROLES);
        requirePositiveId(complaintId);
        return complaintManagementService.getHistory(complaintId);
    }

    @POST
    @Path("/{complaintId}/route")
    public ComplaintSummary route(@PathParam("complaintId") long complaintId) {
        businessApiSecurity.requireAnyRole(OPERATIONAL_ROLES);
        requirePositiveId(complaintId);
        return complaintManagementService.route(complaintId);
    }

    @POST
    @Path("/{complaintId}/escalate")
    @Consumes(MediaType.APPLICATION_JSON)
    public ComplaintSummary escalate(
            @PathParam("complaintId") long complaintId,
            ComplaintActionRequest request
    ) {
        businessApiSecurity.requireAnyRole(OPERATIONAL_ROLES);
        requirePositiveId(complaintId);
        return complaintManagementService.escalate(complaintId, requireNotes(request));
    }

    @POST
    @Path("/{complaintId}/resolve")
    @Consumes(MediaType.APPLICATION_JSON)
    public ComplaintSummary resolve(
            @PathParam("complaintId") long complaintId,
            ComplaintActionRequest request
    ) {
        businessApiSecurity.requireAnyRole(OPERATIONAL_ROLES);
        requirePositiveId(complaintId);
        return complaintManagementService.resolve(complaintId, requireNotes(request));
    }

    private static void validateSubmission(ComplaintSubmissionRequest request) {
        if (request == null
                || request.getZoneId() <= 0L
                || request.getCategoryCode() == null
                || request.getTitle() == null
                || request.getDescription() == null
                || request.getSeverity() == null) {
            throw new InvalidRequestException("Complaint submission is incomplete");
        }

        if (request.getCategoryCode().isBlank()
                || request.getTitle().isBlank()
                || request.getTitle().length() > 200
                || request.getDescription().isBlank()
                || request.getDescription().length() > 5_000) {
            throw new InvalidRequestException("Complaint submission is invalid");
        }
    }

    private static void requirePositiveId(long complaintId) {
        if (complaintId <= 0L) {
            throw new InvalidRequestException("Complaint id must be positive");
        }
    }

    private static String requireNotes(ComplaintActionRequest request) {
        if (request == null
                || request.getNotes() == null
                || request.getNotes().isBlank()
                || request.getNotes().length() > 2_000) {
            throw new InvalidRequestException("Complaint action notes are invalid");
        }

        return request.getNotes().trim();
    }
}
