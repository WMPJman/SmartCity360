package com.wmpj.smartcity360.core.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
