package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.ZoneSummary;
import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.ZoneCompositeService;
import com.wmpj.smartcity360.core.service.ZoneQueryService;
import com.wmpj.smartcity360.core.zone.composite.PowerOperationResult;
import com.wmpj.smartcity360.core.zone.composite.ZoneUsageResult;
import com.wmpj.smartcity360.web.security.BusinessApiSecurity;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.Locale;
import java.util.Objects;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.time.format.DateTimeParseException;

@Path("/zones")
@Produces(MediaType.APPLICATION_JSON)
public class ZoneResource {

    @EJB
    private ZoneQueryService zoneQueryService;

    @EJB
    private ZoneCompositeService zoneCompositeService;

    @Inject
    private BusinessApiSecurity businessApiSecurity;

    public ZoneResource() {
    }

    ZoneResource(
            ZoneQueryService zoneQueryService,
            BusinessApiSecurity businessApiSecurity
    ) {
        this.zoneQueryService = Objects.requireNonNull(
                zoneQueryService,
                "zoneQueryService"
        );
        this.businessApiSecurity = Objects.requireNonNull(
                businessApiSecurity,
                "businessApiSecurity"
        );
    }

    ZoneResource(
            ZoneQueryService zoneQueryService,
            ZoneCompositeService zoneCompositeService,
            BusinessApiSecurity businessApiSecurity
    ) {
        this(zoneQueryService, businessApiSecurity);
        this.zoneCompositeService = Objects.requireNonNull(
                zoneCompositeService,
                "zoneCompositeService"
        );
    }

    @GET
    public PageResponse<ZoneSummary> findZones(
            @QueryParam("type") @DefaultValue("CITY") String type,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit
    ) {
        businessApiSecurity.requireAnyRole(
                SecurityRoles.ADMIN,
                SecurityRoles.CITY_MANAGER,
                SecurityRoles.SUPERVISOR,
                SecurityRoles.LOCAL_STAFF
        );

        try {
            ZoneType zoneType = ZoneType.valueOf(type.trim().toUpperCase(Locale.ROOT));
            PageRequest pageRequest = new PageRequest(offset, limit);

            return zoneQueryService.findZones(zoneType, pageRequest);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException(
                    "type, offset, or limit is invalid"
            );
        }
    }

    @GET
    @Path("/{zoneId}/hierarchy")
    public com.wmpj.smartcity360.core.dto.ZoneHierarchyNode getHierarchy(
            @PathParam("zoneId") long zoneId
    ) {
        requireReportingAccess();
        requirePositiveZoneId(zoneId);
        return zoneCompositeService.getHierarchy(zoneId);
    }

    @GET
    @Path("/{zoneId}/usage")
    public ZoneUsageResult getUsage(
            @PathParam("zoneId") long zoneId,
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {
        requireReportingAccess();
        requirePositiveZoneId(zoneId);

        try {
            OffsetDateTime intervalStart = OffsetDateTime.parse(from);
            OffsetDateTime intervalEnd = OffsetDateTime.parse(to);

            if (!intervalStart.isBefore(intervalEnd)) {
                throw new InvalidRequestException("Reporting interval is invalid");
            }

            if (Duration.between(intervalStart, intervalEnd).compareTo(
                    Duration.ofDays(31)
            ) > 0) {
                throw new InvalidRequestException(
                        "Reporting interval exceeds 31 days"
                );
            }

            return zoneCompositeService.getUsage(
                    zoneId,
                    intervalStart,
                    intervalEnd
            );
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new InvalidRequestException("Reporting interval is invalid");
        }
    }

    @POST
    @Path("/{zoneId}/shutdown")
    public PowerOperationResult shutdownPower(@PathParam("zoneId") long zoneId) {
        requirePowerControlAccess();
        requirePositiveZoneId(zoneId);
        return zoneCompositeService.shutdownPower(zoneId);
    }

    @POST
    @Path("/{zoneId}/restore")
    public PowerOperationResult restorePower(@PathParam("zoneId") long zoneId) {
        requirePowerControlAccess();
        requirePositiveZoneId(zoneId);
        return zoneCompositeService.restorePower(zoneId);
    }

    private void requireReportingAccess() {
        businessApiSecurity.requireAnyRole(
                SecurityRoles.ADMIN,
                SecurityRoles.CITY_MANAGER,
                SecurityRoles.SUPERVISOR,
                SecurityRoles.LOCAL_STAFF
        );
    }

    private void requirePowerControlAccess() {
        businessApiSecurity.requireAnyRole(
                SecurityRoles.ADMIN,
                SecurityRoles.CITY_MANAGER,
                SecurityRoles.SUPERVISOR
        );
    }

    private static void requirePositiveZoneId(long zoneId) {
        if (zoneId <= 0L) {
            throw new InvalidRequestException("zoneId must be positive");
        }
    }
}
