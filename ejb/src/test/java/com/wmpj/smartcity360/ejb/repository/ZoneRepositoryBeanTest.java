package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.PowerStatus;
import com.wmpj.smartcity360.ejb.support.JpaTestDouble;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZoneRepositoryBeanTest {

    @Test
    void hierarchyQueriesAreDirectDeterministicAndBounded() {
        JpaTestDouble jpa = new JpaTestDouble();
        ZoneRepositoryBean repository = new ZoneRepositoryBean(jpa.entityManager());

        repository.findRootCities(25);
        assertEquals("SELECT z FROM Zone z WHERE z.parent IS NULL AND z.zoneType = :type ORDER BY z.id", jpa.jpql.get(0));
        assertEquals(ZoneType.CITY, jpa.parameters.get("type"));
        assertEquals(25, jpa.maxResults);

        repository.findDirectChildren(7L, 30);
        assertEquals("SELECT z FROM Zone z WHERE z.parent.id = :parentId ORDER BY z.id", jpa.jpql.get(1));
        assertEquals(7L, jpa.parameters.get("parentId"));
        assertEquals(30, jpa.maxResults);

        repository.findByType(ZoneType.DISTRICT, 10, 40);
        assertEquals("SELECT z FROM Zone z WHERE z.zoneType = :type ORDER BY z.id", jpa.jpql.get(2));
        assertEquals(10, jpa.firstResult);
        assertEquals(40, jpa.maxResults);

        assertThrows(IllegalArgumentException.class, () -> repository.findRootCities(0));
        assertThrows(IllegalArgumentException.class, () -> repository.findDirectChildren(1L, 101));
        assertThrows(IllegalArgumentException.class, () -> repository.findByType(ZoneType.CITY, -1, 10));
    }

    @Test
    void updateRejectsTransientZonesInsteadOfSilentlyInsertingThem() {
        ZoneRepositoryBean repository = new ZoneRepositoryBean(new JpaTestDouble().entityManager());
        Zone zone = new Zone("New City", "NEW-CITY", ZoneType.CITY, PowerStatus.ON, null,
                OffsetDateTime.MIN, OffsetDateTime.MIN);

        assertThrows(IllegalArgumentException.class, () -> repository.update(zone));
    }
}
