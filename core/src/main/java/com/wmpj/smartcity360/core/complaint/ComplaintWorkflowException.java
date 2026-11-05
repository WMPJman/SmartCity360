package com.wmpj.smartcity360.core.complaint;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ComplaintWorkflowException extends RuntimeException {

    public ComplaintWorkflowException(String message) {
        super(message);
    }
}
