package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.dto.LightingProfileSummary;
import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.ProfileSharingSummary;
import com.wmpj.smartcity360.core.dto.StreetlightSummary;
import com.wmpj.smartcity360.core.streetlight.StreetlightStatusChange;

public interface StreetlightManagementService {

    PageResponse<StreetlightSummary> list(Long zoneId, PageRequest pageRequest);

    StreetlightSummary get(long streetlightId);

    LightingProfileSummary getProfile(long streetlightId);

    StreetlightSummary updateStatus(
            long streetlightId,
            StreetlightStatusChange statusChange
    );

    StreetlightSummary assignProfile(long streetlightId, long profileId);

    ProfileSharingSummary getProfileSharing(
            Long zoneId,
            PageRequest pageRequest
    );
}
