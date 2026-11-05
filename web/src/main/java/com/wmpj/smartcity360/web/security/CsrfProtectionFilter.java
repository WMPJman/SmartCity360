package com.wmpj.smartcity360.web.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100)
public class CsrfProtectionFilter implements ContainerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Context
    private HttpServletRequest request;

    @Inject
    private CsrfTokenManager csrfTokenManager;

    @Inject
    private SecurityEventLogger securityEventLogger;

    public CsrfProtectionFilter() {
    }

    CsrfProtectionFilter(
            HttpServletRequest request,
            CsrfTokenManager csrfTokenManager,
            SecurityEventLogger securityEventLogger
    ) {
        this.request = Objects.requireNonNull(request, "request");
        this.csrfTokenManager = Objects.requireNonNull(csrfTokenManager, "csrfTokenManager");
        this.securityEventLogger = Objects.requireNonNull(
                securityEventLogger,
                "securityEventLogger"
        );
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String method = requestContext.getMethod().toUpperCase(Locale.ROOT);

        if (SAFE_METHODS.contains(method)) {
            return;
        }

        HttpSession session = request.getSession(false);
        String submittedToken = requestContext.getHeaderString(CsrfTokenManager.HEADER_NAME);

        if (!csrfTokenManager.isValid(session, submittedToken)) {
            securityEventLogger.accessDenied("CSRF", request.getRemoteAddr());
            throw new CsrfViolationException();
        }
    }
}
