package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.dao.ZoneRepository;
import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.exception.DuplicateResourceException;
import com.wmpj.smartcity360.core.exception.InvalidZoneHierarchyException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZonePersistenceServiceBeanTest {

    @Test
    void createZoneAcceptsTheDatabaseHierarchySequence() {
        ZoneRepositoryFake repository = new ZoneRepositoryFake();
        ZonePersistenceServiceBean service = new ZonePersistenceServiceBean(repository);
        Zone city = zone("CITY", ZoneType.CITY, null);
        Zone district = zone("DISTRICT", ZoneType.DISTRICT, city);
        Zone street = zone("STREET", ZoneType.STREET, district);
        Zone building = zone("BUILDING", ZoneType.BUILDING, street);

        assertSame(city, service.createZone(city));
        assertSame(district, service.createZone(district));
        assertSame(street, service.createZone(street));
        assertSame(building, service.createZone(building));
    }

    @Test
    void createZoneRejectsDuplicateCodesAndInvalidDirectParents() {
        ZoneRepositoryFake repository = new ZoneRepositoryFake();
        ZonePersistenceServiceBean service = new ZonePersistenceServiceBean(repository);
        Zone city = zone("CITY", ZoneType.CITY, null);
        repository.duplicateCode = true;
        assertThrows(DuplicateResourceException.class, () -> service.createZone(city));

        repository.duplicateCode = false;
        assertThrows(InvalidZoneHierarchyException.class,
                () -> service.createZone(zone("CITY-CHILD", ZoneType.CITY, city)));
        assertThrows(InvalidZoneHierarchyException.class,
                () -> service.createZone(zone("DISTRICT-NO-PARENT", ZoneType.DISTRICT, null)));
        assertThrows(InvalidZoneHierarchyException.class,
                () -> service.createZone(zone("STREET-UNDER-CITY", ZoneType.STREET, city)));
    }

    private static Zone zone(String code, ZoneType type, Zone parent) {
        return new Zone("Test " + code, code, type, PowerStatus.ON, parent,
                OffsetDateTime.MIN, OffsetDateTime.MIN);
    }

    private static final class ZoneRepositoryFake implements ZoneRepository {
        private final List<Zone> zones = new ArrayList<>();
        private boolean duplicateCode;

        public Optional<Zone> findById(long id) { return Optional.empty(); }
        public Optional<Zone> findByCode(String code) { return Optional.empty(); }
        public boolean existsByCode(String code) { return duplicateCode; }
        public List<Zone> findRootCities(int limit) { return List.of(); }
        public List<Zone> findDirectChildren(long parentId, int limit) { return List.of(); }
        public List<Zone> findByType(ZoneType type, int offset, int limit) { return List.of(); }
        public Zone persist(Zone zone) { zones.add(zone); return zone; }
        public Zone update(Zone zone) { return zone; }
    }
}
