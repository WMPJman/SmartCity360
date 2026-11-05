package com.wmpj.smartcity360.ejb.service;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyManagementTransactionTest {

    @Test
    void lifecycleWritesUseRequiredAndEvaluationIsReadOnly() throws Exception {
        TransactionAttribute classTransaction = PolicyManagementServiceBean.class
                .getAnnotation(TransactionAttribute.class);
        TransactionAttribute evaluationTransaction = PolicyManagementServiceBean.class
                .getMethod(
                        "evaluate",
                        long.class,
                        com.wmpj.smartcity360.core.dto.PolicyEvaluationInput.class
                )
                .getAnnotation(TransactionAttribute.class);

        assertEquals(TransactionAttributeType.REQUIRED, classTransaction.value());
        assertEquals(TransactionAttributeType.SUPPORTS, evaluationTransaction.value());
    }
}
