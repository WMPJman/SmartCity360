package com.wmpj.smartcity360.core.policy.interpreter;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class PolicySyntaxException extends RuntimeException {

    public PolicySyntaxException(String message) {
        super(message);
    }
}
