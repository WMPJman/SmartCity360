package com.wmpj.smartcity360.core.complaint.chain;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintSeverity;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;

public final class SupervisorHandler extends AbstractComplaintHandler {

    public SupervisorHandler() {
        super(ComplaintHandlerLevel.SUPERVISOR);
    }

    @Override
    protected boolean canHandle(ComplaintHandlingContext context) {
        if (context.status() == ComplaintStatus.RESOLVED
                || context.status() == ComplaintStatus.REJECTED) {
            return false;
        }

        if (context.currentLevel() == ComplaintHandlerLevel.SUPERVISOR) {
            return context.severity() != ComplaintSeverity.CRITICAL;
        }

        return context.currentLevel() == ComplaintHandlerLevel.LOCAL_STAFF
                && context.severity() == ComplaintSeverity.HIGH;
    }
}
