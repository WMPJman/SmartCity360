package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.core.exception.UnauthenticatedException;
import jakarta.ejb.EJBAccessException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;

import java.util.Objects;

@RequestScoped
public class BusinessApiSecurity {

    @Inject
    private SecurityContext securityContext;

    public BusinessApiSecurity() {
    }

    public BusinessApiSecurity(SecurityContext securityContext) {
        this.securityContext = Objects.requireNonNull(securityContext, "securityContext");
    }

    public void requireAnyRole(String... allowedRoles) {
        if (securityContext.getCallerPrincipal() == null) {
            throw new UnauthenticatedException("Authentication is required");
        }

        for (String allowedRole : allowedRoles) {
            if (securityContext.isCallerInRole(allowedRole)) {
                return;
            }
        }

        throw new EJBAccessException("Access is denied");
    }
}
