package com.wmpj.smartcity360.core.complaint;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;

public final class ComplaintTransitionPolicy {

    private ComplaintTransitionPolicy() {
    }

    public static void requireRoutable(ComplaintStatus status) {
        if (status != ComplaintStatus.SUBMITTED) {
            throw new ComplaintWorkflowException("Only submitted complaints can be routed");
        }
    }

    public static void requireEscalatable(ComplaintStatus status) {
        if (status != ComplaintStatus.IN_PROGRESS) {
            throw new ComplaintWorkflowException("Only in-progress complaints can be escalated");
        }
    }

    public static void requireResolvable(ComplaintStatus status) {
        if (status != ComplaintStatus.IN_PROGRESS && status != ComplaintStatus.ESCALATED) {
            throw new ComplaintWorkflowException(
                    "Complaint cannot be resolved from its current state"
            );
        }
    }

    public static ComplaintHandlerLevel nextLevel(ComplaintHandlerLevel currentLevel) {
        if (currentLevel == ComplaintHandlerLevel.LOCAL_STAFF) {
            return ComplaintHandlerLevel.SUPERVISOR;
        }

        if (currentLevel == ComplaintHandlerLevel.SUPERVISOR) {
            return ComplaintHandlerLevel.CITY_MANAGER;
        }

        throw new ComplaintWorkflowException(
                "Complaint is already at the final handler level"
        );
    }
}
