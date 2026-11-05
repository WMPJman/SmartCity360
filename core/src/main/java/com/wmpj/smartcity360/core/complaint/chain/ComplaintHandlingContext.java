package com.wmpj.smartcity360.core.complaint.chain;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintSeverity;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;

import java.util.Objects;

public record ComplaintHandlingContext(
        long complaintId,
        ComplaintSeverity severity,
        ComplaintStatus status,
        ComplaintHandlerLevel currentLevel
) {

    public ComplaintHandlingContext {
        if (complaintId <= 0L) {
            throw new IllegalArgumentException("complaintId must be positive");
        }

        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(currentLevel, "currentLevel");
    }
}
