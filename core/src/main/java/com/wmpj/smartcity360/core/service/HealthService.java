package com.wmpj.smartcity360.core.service;

/**
 * Shared contract for the application's health check.
 */
@FunctionalInterface
public interface HealthService {

    String getStatus();
}
