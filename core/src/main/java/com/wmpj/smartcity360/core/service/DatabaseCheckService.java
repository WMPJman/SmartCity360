package com.wmpj.smartcity360.core.service;

/**
 * Shared contract for a read-only database connectivity check.
 */
@FunctionalInterface
public interface DatabaseCheckService {

    long countRoles();
}
