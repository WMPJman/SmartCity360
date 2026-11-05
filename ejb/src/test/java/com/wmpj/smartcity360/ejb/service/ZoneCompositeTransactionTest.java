package com.wmpj.smartcity360.ejb.service;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZoneCompositeTransactionTest {

    @Test
    void powerOperationsUseRequiredTransactionsAndReportsAreReadOnly() throws Exception {
        TransactionAttribute classTransaction = ZoneCompositeServiceBean.class.getAnnotation(
                TransactionAttribute.class
        );
        TransactionAttribute reportTransaction = ZoneCompositeServiceBean.class
                .getMethod(
                        "getUsage",
                        long.class,
                        java.time.OffsetDateTime.class,
                        java.time.OffsetDateTime.class
                )
                .getAnnotation(TransactionAttribute.class);

        assertEquals(TransactionAttributeType.REQUIRED, classTransaction.value());
        assertEquals(TransactionAttributeType.SUPPORTS, reportTransaction.value());
    }
}
