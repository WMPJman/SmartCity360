package com.wmpj.smartcity360.core.energy;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class EnergyKeyUnavailableException extends RuntimeException {

    public EnergyKeyUnavailableException(String message) {
        super(message);
    }
}
