package com.wmpj.smartcity360.core.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InvalidZoneHierarchyException extends RuntimeException {
    public InvalidZoneHierarchyException(String message) {
        super(message);
    }
}
