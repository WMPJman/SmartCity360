package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.ComplaintSeverity;

public record ComplaintSubmission(
        long zoneId,
        String categoryCode,
        String title,
        String description,
        ComplaintSeverity severity
) {
}
