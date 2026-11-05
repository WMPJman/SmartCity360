package com.wmpj.smartcity360.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsrfProtectionFilterTest {

    @Test
    void acceptsSafeMethodsAndAValidUnsafeRequestToken() {
        CsrfTokenManager manager = new CsrfTokenManager();
        HttpSession session = CsrfTokenManagerTest.session(new HashMap<>());
        String token = manager.getOrCreate(session);
        CsrfProtectionFilter filter = new CsrfProtectionFilter(
                request(session),
                manager,
                new SecurityEventLogger()
        );

        assertDoesNotThrow(() -> filter.filter(context("GET", null)));
        assertDoesNotThrow(() -> filter.filter(context("HEAD", null)));
        assertDoesNotThrow(() -> filter.filter(context("OPTIONS", null)));
        assertDoesNotThrow(() -> filter.filter(context("POST", token)));
    }

    @Test
    void rejectsMissingInvalidAndCrossSessionTokensForUnsafeMethods() {
        CsrfTokenManager manager = new CsrfTokenManager();
        HttpSession session = CsrfTokenManagerTest.session(new HashMap<>());
        HttpSession otherSession = CsrfTokenManagerTest.session(new HashMap<>());
        String token = manager.getOrCreate(session);
        String otherToken = manager.getOrCreate(otherSession);
        CsrfProtectionFilter filter = new CsrfProtectionFilter(
                request(session),
                manager,
                new SecurityEventLogger()
        );

        assertThrows(
                CsrfViolationException.class,
                () -> filter.filter(context("POST", null))
        );
        assertThrows(
                CsrfViolationException.class,
                () -> filter.filter(context("PUT", "invalid"))
        );
        assertThrows(
                CsrfViolationException.class,
                () -> filter.filter(context("PATCH", otherToken))
        );
        assertDoesNotThrow(() -> filter.filter(context("DELETE", token)));
    }

    @Test
    void rejectsAnUnsafeRequestAfterItsSessionExpires() {
        CsrfTokenManager manager = new CsrfTokenManager();
        CsrfProtectionFilter filter = new CsrfProtectionFilter(
                request(null),
                manager,
                new SecurityEventLogger()
        );

        assertThrows(
                CsrfViolationException.class,
                () -> filter.filter(context("POST", "stale-token"))
        );
    }

    private static HttpServletRequest request(HttpSession session) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getSession")) {
                        return session;
                    }

                    if (method.getName().equals("getRemoteAddr")) {
                        return "127.0.0.1";
                    }

                    return null;
                }
        );
    }

    private static ContainerRequestContext context(String method, String csrfToken) {
        Map<String, String> headers = new HashMap<>();

        if (csrfToken != null) {
            headers.put(CsrfTokenManager.HEADER_NAME, csrfToken);
        }

        return (ContainerRequestContext) Proxy.newProxyInstance(
                ContainerRequestContext.class.getClassLoader(),
                new Class<?>[]{ContainerRequestContext.class},
                (proxy, calledMethod, arguments) -> {
                    if (calledMethod.getName().equals("getMethod")) {
                        return method;
                    }

                    if (calledMethod.getName().equals("getHeaderString")) {
                        return headers.get((String) arguments[0]);
                    }

                    return null;
                }
        );
    }
}
