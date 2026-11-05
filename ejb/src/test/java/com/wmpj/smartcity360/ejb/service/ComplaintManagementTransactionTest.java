package com.wmpj.smartcity360.ejb.service;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComplaintManagementTransactionTest {

    @Test
    void mutationsAndHistoryShareRequiredTransactions() throws Exception {
        TransactionAttribute classTransaction = ComplaintManagementServiceBean.class
                .getAnnotation(TransactionAttribute.class);
        TransactionAttribute historyTransaction = ComplaintManagementServiceBean.class
                .getMethod("getHistory", long.class)
                .getAnnotation(TransactionAttribute.class);

        assertEquals(TransactionAttributeType.REQUIRED, classTransaction.value());
        assertEquals(TransactionAttributeType.SUPPORTS, historyTransaction.value());
    }
}
