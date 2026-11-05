package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.PolicyEvaluationResult;

import java.time.OffsetDateTime;

public record PolicyEvaluationHistorySummary(
        long id,
        String expressionSnapshot,
        PolicyEvaluationResult result,
        String errorMessage,
        OffsetDateTime evaluatedAt
) {
}
