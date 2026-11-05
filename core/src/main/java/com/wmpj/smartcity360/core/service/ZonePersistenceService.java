package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.entity.Zone;

import java.util.List;
import java.util.Optional;

public interface ZonePersistenceService {
    Optional<Zone> findZone(long zoneId);

    List<Zone> findDirectChildren(long parentId, int limit);

    Zone createZone(Zone zone);

    Zone updateZone(Zone zone);
}
