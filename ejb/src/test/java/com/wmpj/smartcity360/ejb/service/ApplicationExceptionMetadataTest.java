package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.exception.DuplicateResourceException;
import com.wmpj.smartcity360.core.exception.InvalidZoneHierarchyException;
import com.wmpj.smartcity360.core.exception.ResourceNotFoundException;
import jakarta.ejb.ApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationExceptionMetadataTest {

    @Test
    void expectedServiceFailuresRemainTypedAndRollBackContainerTransactions() {
        for (Class<? extends RuntimeException> type : List.of(
                DuplicateResourceException.class,
                InvalidZoneHierarchyException.class,
                ResourceNotFoundException.class
        )) {
            ApplicationException annotation = type.getAnnotation(ApplicationException.class);
            assertNotNull(annotation, type.getSimpleName() + " must be an EJB application exception");
            assertTrue(annotation.rollback(), type.getSimpleName() + " must roll back writes");
        }
    }
}
