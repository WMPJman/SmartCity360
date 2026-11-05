package com.wmpj.smartcity360.core.policy.interpreter;

import com.wmpj.smartcity360.core.entity.PolicyEvaluationResult;

public record PolicyInterpretationResult(
        PolicyEvaluationResult result,
        String errorMessage
) {
}
