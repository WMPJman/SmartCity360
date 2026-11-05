package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintHistoryAction;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;

import java.time.OffsetDateTime;

public record ComplaintHistorySummary(
        long id,
        ComplaintHistoryAction action,
        ComplaintHandlerLevel fromHandlerLevel,
        ComplaintHandlerLevel toHandlerLevel,
        ComplaintStatus fromStatus,
        ComplaintStatus toStatus,
        String notes,
        OffsetDateTime createdAt
) {
}
