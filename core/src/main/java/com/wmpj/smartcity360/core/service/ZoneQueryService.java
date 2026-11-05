package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.ZoneSummary;
import com.wmpj.smartcity360.core.entity.ZoneType;

public interface ZoneQueryService {

    PageResponse<ZoneSummary> findZones(
            ZoneType zoneType,
            PageRequest pageRequest
    );
}
