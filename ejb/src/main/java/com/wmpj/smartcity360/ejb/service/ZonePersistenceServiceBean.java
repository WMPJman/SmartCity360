package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.dao.ZoneRepository;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.exception.DuplicateResourceException;
import com.wmpj.smartcity360.core.exception.InvalidZoneHierarchyException;
import com.wmpj.smartcity360.core.service.ZonePersistenceService;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Stateless
@Local(ZonePersistenceService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ZonePersistenceServiceBean implements ZonePersistenceService {

    @EJB
    private ZoneRepository zoneRepository;

    public ZonePersistenceServiceBean() {
    }

    ZonePersistenceServiceBean(ZoneRepository zoneRepository) {
        this.zoneRepository = Objects.requireNonNull(zoneRepository, "zoneRepository");
    }

    @Override
    public Optional<Zone> findZone(long zoneId) {
        return zoneRepository.findById(zoneId);
    }

    @Override
    public List<Zone> findDirectChildren(long parentId, int limit) {
        return zoneRepository.findDirectChildren(parentId, limit);
    }

    @Override
    public Zone createZone(Zone zone) {
        Objects.requireNonNull(zone, "zone");
        if (zoneRepository.existsByCode(zone.getZoneCode())) {
            throw new DuplicateResourceException("A zone with that code already exists");
        }
        validateHierarchy(zone);
        return zoneRepository.persist(zone);
    }

    @Override
    public Zone updateZone(Zone zone) {
        Objects.requireNonNull(zone, "zone");
        zoneRepository.findByCode(zone.getZoneCode())
                .filter(existing -> !Objects.equals(existing.getId(), zone.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A zone with that code already exists");
                });
        validateHierarchy(zone);
        return zoneRepository.update(zone);
    }

    private static void validateHierarchy(Zone zone) {
        ZoneType type = Objects.requireNonNull(zone.getZoneType(), "zone.zoneType");
        Zone parent = zone.getParent();
        if (type == ZoneType.CITY) {
            if (parent != null) {
                throw new InvalidZoneHierarchyException("A CITY zone cannot have a parent");
            }
            return;
        }
        if (parent == null) {
            throw new InvalidZoneHierarchyException(type + " requires a parent zone");
        }
        ZoneType expectedParent = switch (type) {
            case DISTRICT -> ZoneType.CITY;
            case STREET -> ZoneType.DISTRICT;
            case BUILDING -> ZoneType.STREET;
            case CITY -> throw new IllegalStateException("CITY handled above");
        };
        if (parent.getZoneType() != expectedParent) {
            throw new InvalidZoneHierarchyException(
                    type + " requires a " + expectedParent + " parent");
        }
    }
}
