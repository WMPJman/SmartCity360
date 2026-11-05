package com.wmpj.smartcity360.core.policy.interpreter;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class PolicyEvaluationException extends RuntimeException {

    public PolicyEvaluationException(String message) {
        super(message);
    }
}
