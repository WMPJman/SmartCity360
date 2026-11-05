package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.PolicyEvaluationResult;

import java.time.OffsetDateTime;

public record PolicyEvaluationSummary(
        long policyId,
        String policyCode,
        String expressionSnapshot,
        PolicyEvaluationResult result,
        String errorMessage,
        OffsetDateTime evaluatedAt,
        boolean persisted
) {
}
