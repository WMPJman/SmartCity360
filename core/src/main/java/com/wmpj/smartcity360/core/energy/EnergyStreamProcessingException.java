package com.wmpj.smartcity360.core.energy;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class EnergyStreamProcessingException extends RuntimeException {

    public EnergyStreamProcessingException(String message) {
        super(message);
    }

    public EnergyStreamProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
