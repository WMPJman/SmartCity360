package com.wmpj.smartcity360.core.complaint.chain;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintSeverity;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;

public final class CityManagerHandler extends AbstractComplaintHandler {

    public CityManagerHandler() {
        super(ComplaintHandlerLevel.CITY_MANAGER);
    }

    @Override
    protected boolean canHandle(ComplaintHandlingContext context) {
        if (context.status() == ComplaintStatus.RESOLVED
                || context.status() == ComplaintStatus.REJECTED) {
            return false;
        }

        return context.currentLevel() == ComplaintHandlerLevel.CITY_MANAGER
                || context.severity() == ComplaintSeverity.CRITICAL;
    }
}
