package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.PolicyCreation;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationHistorySummary;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationInput;
import com.wmpj.smartcity360.core.dto.PolicyEvaluationSummary;
import com.wmpj.smartcity360.core.dto.PolicySummary;
import com.wmpj.smartcity360.core.dto.PolicyValidationSummary;

public interface PolicyManagementService {

    PolicySummary create(PolicyCreation creation);

    PageResponse<PolicySummary> list(PageRequest pageRequest);

    PolicySummary get(long policyId);

    PolicyValidationSummary validate(long policyId);

    PolicySummary activate(long policyId);

    PolicySummary deactivate(long policyId);

    PolicyEvaluationSummary evaluate(
            long policyId,
            PolicyEvaluationInput evaluationInput
    );

    PageResponse<PolicyEvaluationHistorySummary> getEvaluations(
            long policyId,
            PageRequest pageRequest
    );
}
