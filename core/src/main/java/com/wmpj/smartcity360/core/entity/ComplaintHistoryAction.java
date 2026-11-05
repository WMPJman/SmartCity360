package com.wmpj.smartcity360.core.entity;

/**
 * Values enforced by the complaint_history.action check constraint.
 */
public enum ComplaintHistoryAction {
    SUBMITTED,
    ASSIGNED,
    ESCALATED,
    STATUS_CHANGED,
    RESOLVED,
    REJECTED
}
