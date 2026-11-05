package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.dto.LightingProfileSummary;
import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.ProfileSharingSummary;
import com.wmpj.smartcity360.core.dto.StreetlightSummary;
import com.wmpj.smartcity360.core.entity.StreetlightOperationalStatus;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.StreetlightManagementService;
import com.wmpj.smartcity360.core.streetlight.StreetlightStatusChange;
import com.wmpj.smartcity360.web.dto.StreetlightProfileAssignmentRequest;
import com.wmpj.smartcity360.web.dto.StreetlightStatusRequest;
import com.wmpj.smartcity360.web.security.BusinessApiSecurity;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.Locale;

@Path("/streetlights")
@Produces(MediaType.APPLICATION_JSON)
public class StreetlightResource {

    private static final String[] STREETLIGHT_ROLES = {
            SecurityRoles.ADMIN,
            SecurityRoles.CITY_MANAGER,
            SecurityRoles.SUPERVISOR,
            SecurityRoles.LOCAL_STAFF
    };

    @EJB
    private StreetlightManagementService streetlightManagementService;

    @Inject
    private BusinessApiSecurity businessApiSecurity;

    public StreetlightResource() {
    }

    StreetlightResource(
            StreetlightManagementService streetlightManagementService,
            BusinessApiSecurity businessApiSecurity
    ) {
        this.streetlightManagementService = streetlightManagementService;
        this.businessApiSecurity = businessApiSecurity;
    }

    @GET
    public PageResponse<StreetlightSummary> list(
            @QueryParam("zoneId") Long zoneId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit
    ) {
        requireStreetlightRole();
        PageRequest pageRequest = pageRequest(offset, limit);
        validateOptionalZoneId(zoneId);
        return streetlightManagementService.list(zoneId, pageRequest);
    }

    @GET
    @Path("/{streetlightId}")
    public StreetlightSummary get(@PathParam("streetlightId") long streetlightId) {
        requireStreetlightRole();
        requirePositiveStreetlightId(streetlightId);
        return streetlightManagementService.get(streetlightId);
    }

    @GET
    @Path("/{streetlightId}/profile")
    public LightingProfileSummary getProfile(
            @PathParam("streetlightId") long streetlightId
    ) {
        requireStreetlightRole();
        requirePositiveStreetlightId(streetlightId);
        return streetlightManagementService.getProfile(streetlightId);
    }

    @PUT
    @Path("/{streetlightId}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public StreetlightSummary updateStatus(
            @PathParam("streetlightId") long streetlightId,
            StreetlightStatusRequest request
    ) {
        requireStreetlightRole();
        requirePositiveStreetlightId(streetlightId);

        if (request == null
                || request.getOn() == null
                || request.getBrightness() == null
                || request.getOperationalStatus() == null) {
            throw new InvalidRequestException("Streetlight status request is incomplete");
        }

        try {
            StreetlightOperationalStatus operationalStatus =
                    StreetlightOperationalStatus.valueOf(
                            request.getOperationalStatus().trim().toUpperCase(Locale.ROOT)
                    );
            StreetlightStatusChange statusChange = new StreetlightStatusChange(
                    request.getOn(),
                    request.getBrightness(),
                    operationalStatus
            );
            return streetlightManagementService.updateStatus(
                    streetlightId,
                    statusChange
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Streetlight status request is invalid");
        }
    }

    @PUT
    @Path("/{streetlightId}/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    public StreetlightSummary assignProfile(
            @PathParam("streetlightId") long streetlightId,
            StreetlightProfileAssignmentRequest request
    ) {
        businessApiSecurity.requireAnyRole(
                SecurityRoles.ADMIN,
                SecurityRoles.CITY_MANAGER,
                SecurityRoles.SUPERVISOR
        );
        requirePositiveStreetlightId(streetlightId);

        if (request == null
                || request.getProfileId() == null
                || request.getProfileId() <= 0L) {
            throw new InvalidRequestException("Streetlight profile id is invalid");
        }

        return streetlightManagementService.assignProfile(
                streetlightId,
                request.getProfileId()
        );
    }

    @GET
    @Path("/profile-sharing")
    public ProfileSharingSummary getProfileSharing(
            @QueryParam("zoneId") Long zoneId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("100") int limit
    ) {
        requireStreetlightRole();
        PageRequest pageRequest = pageRequest(offset, limit);
        validateOptionalZoneId(zoneId);
        return streetlightManagementService.getProfileSharing(zoneId, pageRequest);
    }

    private void requireStreetlightRole() {
        businessApiSecurity.requireAnyRole(STREETLIGHT_ROLES);
    }

    private static PageRequest pageRequest(int offset, int limit) {
        try {
            return new PageRequest(offset, limit);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Streetlight page request is invalid");
        }
    }

    private static void validateOptionalZoneId(Long zoneId) {
        if (zoneId != null && zoneId <= 0L) {
            throw new InvalidRequestException("zoneId must be positive");
        }
    }

    private static void requirePositiveStreetlightId(long streetlightId) {
        if (streetlightId <= 0L) {
            throw new InvalidRequestException("streetlightId must be positive");
        }
    }
}
