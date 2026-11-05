package com.wmpj.smartcity360.core.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
