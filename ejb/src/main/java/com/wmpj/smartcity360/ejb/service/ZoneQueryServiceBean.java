package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.dao.ZoneRepository;
import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.ZoneSummary;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.ZoneQueryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.List;
import java.util.Objects;

@Stateless
@Local(ZoneQueryService.class)
@RolesAllowed({
        SecurityRoles.ADMIN,
        SecurityRoles.CITY_MANAGER,
        SecurityRoles.SUPERVISOR,
        SecurityRoles.LOCAL_STAFF
})
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ZoneQueryServiceBean implements ZoneQueryService {

    @EJB
    private ZoneRepository zoneRepository;

    public ZoneQueryServiceBean() {
    }

    ZoneQueryServiceBean(ZoneRepository zoneRepository) {
        this.zoneRepository = Objects.requireNonNull(zoneRepository, "zoneRepository");
    }

    @Override
    public PageResponse<ZoneSummary> findZones(
            ZoneType zoneType,
            PageRequest pageRequest
    ) {
        Objects.requireNonNull(zoneType, "zoneType");
        Objects.requireNonNull(pageRequest, "pageRequest");

        List<ZoneSummary> summaries = zoneRepository.findByType(
                        zoneType,
                        pageRequest.offset(),
                        pageRequest.limit()
                )
                .stream()
                .map(ZoneQueryServiceBean::toSummary)
                .toList();

        return new PageResponse<>(
                summaries,
                pageRequest.offset(),
                pageRequest.limit()
        );
    }

    private static ZoneSummary toSummary(Zone zone) {
        Zone parent = zone.getParent();
        Long parentId = null;

        if (parent != null) {
            parentId = parent.getId();
        }

        return new ZoneSummary(
                zone.getId(),
                parentId,
                zone.getName(),
                zone.getZoneCode(),
                zone.getZoneType(),
                zone.getPowerStatus()
        );
    }
}
