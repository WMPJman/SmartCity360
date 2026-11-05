package com.wmpj.smartcity360.core.dao;

import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.ZoneType;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository {
    Optional<Zone> findById(long id);

    Optional<Zone> findByCode(String code);

    boolean existsByCode(String code);

    List<Zone> findRootCities(int limit);

    List<Zone> findDirectChildren(long parentId, int limit);

    List<Zone> findByType(ZoneType type, int offset, int limit);

    Zone persist(Zone zone);

    Zone update(Zone zone);
}
