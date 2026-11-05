package com.wmpj.smartcity360.core.policy;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class PolicyLifecycleException extends RuntimeException {

    public PolicyLifecycleException(String message) {
        super(message);
    }
}
