package com.wmpj.smartcity360.core.complaint.chain;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;

import java.util.List;

public record ComplaintHandlingResult(
        boolean handled,
        ComplaintHandlerLevel responsibleLevel,
        List<ComplaintHandlerLevel> visitedLevels
) {

    public ComplaintHandlingResult {
        visitedLevels = List.copyOf(visitedLevels);
    }
}
