package com.wmpj.smartcity360.ejb.service;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreetlightManagementTransactionTest {

    @Test
    void writesUseRequiredTransactionsAndReadsUseSupports() throws Exception {
        TransactionAttribute classTransaction = StreetlightManagementServiceBean.class
                .getAnnotation(TransactionAttribute.class);
        TransactionAttribute listTransaction = StreetlightManagementServiceBean.class
                .getMethod(
                        "list",
                        Long.class,
                        com.wmpj.smartcity360.core.dto.PageRequest.class
                )
                .getAnnotation(TransactionAttribute.class);

        assertEquals(TransactionAttributeType.REQUIRED, classTransaction.value());
        assertEquals(TransactionAttributeType.SUPPORTS, listTransaction.value());
    }
}
