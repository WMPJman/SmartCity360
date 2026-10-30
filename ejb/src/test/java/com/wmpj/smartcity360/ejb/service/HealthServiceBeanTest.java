package com.wmpj.smartcity360.ejb.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthServiceBeanTest {

    @Test
    void getStatusReturnsUp() {
        HealthServiceBean service = new HealthServiceBean();

        assertEquals("UP", service.getStatus());
    }
}
