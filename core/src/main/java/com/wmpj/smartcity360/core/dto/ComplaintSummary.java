package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintSeverity;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;

import java.time.OffsetDateTime;

public record ComplaintSummary(
        long id,
        String complaintNumber,
        long zoneId,
        String categoryCode,
        String title,
        String description,
        ComplaintSeverity severity,
        ComplaintStatus status,
        ComplaintHandlerLevel handlerLevel,
        Long assignedUserId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime resolvedAt
) {
}
