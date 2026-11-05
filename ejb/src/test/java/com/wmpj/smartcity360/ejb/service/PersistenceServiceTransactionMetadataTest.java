package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.service.IdentityPersistenceService;
import com.wmpj.smartcity360.core.service.ZonePersistenceService;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PersistenceServiceTransactionMetadataTest {

    @Test
    void persistenceServicesAreLocalStatelessEjbsWithRequiredTransactions() {
        Map<Class<?>, Class<?>> beans = Map.of(
                IdentityPersistenceServiceBean.class, IdentityPersistenceService.class,
                ZonePersistenceServiceBean.class, ZonePersistenceService.class
        );
        for (var entry : beans.entrySet()) {
            assertNotNull(entry.getKey().getAnnotation(Stateless.class));
            assertArrayEquals(new Class<?>[]{entry.getValue()}, entry.getKey().getAnnotation(Local.class).value());
            assertEquals(TransactionAttributeType.REQUIRED,
                    entry.getKey().getAnnotation(TransactionAttribute.class).value());
        }
    }
}
