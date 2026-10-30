package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.service.HealthService;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;

@Stateless
@Local(HealthService.class)
public class HealthServiceBean implements HealthService {

    @Override
    public String getStatus() {
        return "UP";
    }
}
