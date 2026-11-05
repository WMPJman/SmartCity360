package com.wmpj.smartcity360.core.complaint.chain;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintSeverity;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;

public final class LocalStaffHandler extends AbstractComplaintHandler {

    public LocalStaffHandler() {
        super(ComplaintHandlerLevel.LOCAL_STAFF);
    }

    @Override
    protected boolean canHandle(ComplaintHandlingContext context) {
        if (isTerminal(context.status())) {
            return false;
        }

        if (context.currentLevel() != ComplaintHandlerLevel.LOCAL_STAFF) {
            return false;
        }

        return context.severity() == ComplaintSeverity.LOW
                || context.severity() == ComplaintSeverity.MEDIUM;
    }

    private static boolean isTerminal(ComplaintStatus status) {
        return status == ComplaintStatus.RESOLVED || status == ComplaintStatus.REJECTED;
    }
}
