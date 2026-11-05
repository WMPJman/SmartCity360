package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.CurrentIdentityService;
import com.wmpj.smartcity360.web.dto.AuthenticatedUserResponse;
import com.wmpj.smartcity360.web.dto.CsrfTokenResponse;
import com.wmpj.smartcity360.web.dto.LoginRequest;
import com.wmpj.smartcity360.web.security.CsrfTokenManager;
import com.wmpj.smartcity360.web.security.InvalidCredentialsException;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import com.wmpj.smartcity360.web.security.SecurityEventLogger;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationResourceTest {

    @Test
    void onlyLoginConsumesJsonSoLogoutAcceptsAnEmptyPost() throws NoSuchMethodException {
        Method login = AuthenticationResource.class.getMethod("login", LoginRequest.class);
        Method logout = AuthenticationResource.class.getMethod("logout");

        Consumes loginConsumes = login.getAnnotation(Consumes.class);

        assertEquals(MediaType.APPLICATION_JSON, loginConsumes.value()[0]);
        assertFalse(AuthenticationResource.class.isAnnotationPresent(Consumes.class));
        assertFalse(logout.isAnnotationPresent(Consumes.class));
        assertTrue(logout.isAnnotationPresent(POST.class));
    }

    @Test
    void csrfEndpointCreatesAnAnonymousSessionTokenForLoginProtection() {
        SessionFixture fixture = new SessionFixture();
        CsrfTokenManager tokenManager = new CsrfTokenManager();
        AuthenticationResource resource = resource(
                fixture,
                tokenManager,
                AuthenticationStatus.NOT_DONE
        );

        CsrfTokenResponse response = resource.csrfToken();

        assertTrue(tokenManager.isValid(fixture.session, response.csrfToken()));
        assertEquals(
                response.csrfToken(),
                fixture.responseHeaders.get(CsrfTokenManager.HEADER_NAME)
        );
    }

    @Test
    void successfulLoginRotatesSessionAndCsrfTokenAndReturnsOnlySafeIdentity() {
        SessionFixture fixture = new SessionFixture();
        CsrfTokenManager tokenManager = new CsrfTokenManager();
        String loginToken = tokenManager.getOrCreate(fixture.session);
        AuthenticationResource resource = resource(
                fixture,
                tokenManager,
                AuthenticationStatus.SUCCESS
        );
        LoginRequest request = new LoginRequest();
        request.setEmail("resident@example.test");
        request.setPassword("correct password");

        AuthenticatedUserResponse response = resource.login(request);
        String authenticatedToken = fixture.responseHeaders.get(
                CsrfTokenManager.HEADER_NAME
        );

        assertEquals(1, fixture.sessionRotations.get());
        assertNotEquals(loginToken, authenticatedToken);
        assertTrue(tokenManager.isValid(fixture.session, authenticatedToken));
        assertEquals(7L, response.userId());
        assertEquals(Set.of(SecurityRoles.RESIDENT), Set.copyOf(response.roleCodes()));
        assertFalse(response.toString().toLowerCase().contains("password"));
    }

    @Test
    void failedOrMalformedLoginDoesNotRotateTheSession() {
        SessionFixture fixture = new SessionFixture();
        CsrfTokenManager tokenManager = new CsrfTokenManager();
        AuthenticationResource resource = resource(
                fixture,
                tokenManager,
                AuthenticationStatus.SEND_FAILURE
        );
        LoginRequest invalidCredentials = new LoginRequest();
        invalidCredentials.setEmail("unknown@example.test");
        invalidCredentials.setPassword("wrong");
        LoginRequest malformedRequest = new LoginRequest();
        malformedRequest.setEmail(" ");
        malformedRequest.setPassword("password");

        assertThrows(
                InvalidCredentialsException.class,
                () -> resource.login(invalidCredentials)
        );
        assertThrows(
                InvalidRequestException.class,
                () -> resource.login(malformedRequest)
        );
        assertEquals(0, fixture.sessionRotations.get());
    }

    @Test
    void currentUserComesFromTheTrustedContainerBackedService() {
        SessionFixture fixture = new SessionFixture();
        AuthenticationResource resource = resource(
                fixture,
                new CsrfTokenManager(),
                AuthenticationStatus.NOT_DONE
        );

        AuthenticatedUserResponse response = resource.currentUser();

        assertEquals("resident@example.test", response.email());
        assertEquals(Set.of(SecurityRoles.RESIDENT), Set.copyOf(response.roleCodes()));
    }

    @Test
    void logoutClearsContainerStateInvalidatesCsrfAndInvalidatesSession() {
        SessionFixture fixture = new SessionFixture();
        CsrfTokenManager tokenManager = new CsrfTokenManager();
        String token = tokenManager.getOrCreate(fixture.session);
        AuthenticationResource resource = resource(
                fixture,
                tokenManager,
                AuthenticationStatus.NOT_DONE
        );

        resource.logout();

        assertTrue(fixture.loggedOut.get());
        assertTrue(fixture.sessionInvalidated.get());
        assertFalse(tokenManager.isValid(fixture.session, token));
    }

    private static AuthenticationResource resource(
            SessionFixture fixture,
            CsrfTokenManager tokenManager,
            AuthenticationStatus authenticationStatus
    ) {
        CurrentIdentityService currentIdentityService = () -> new AuthenticatedIdentity(
                7L,
                "Test Resident",
                "resident@example.test",
                Set.of(SecurityRoles.RESIDENT)
        );

        return new AuthenticationResource(
                securityContext(authenticationStatus),
                currentIdentityService,
                tokenManager,
                new SecurityEventLogger(),
                fixture.request,
                fixture.response
        );
    }

    private static SecurityContext securityContext(AuthenticationStatus status) {
        return new SecurityContext() {
            @Override
            public Principal getCallerPrincipal() {
                return null;
            }

            @Override
            public <T extends Principal> Set<T> getPrincipalsByType(Class<T> principalType) {
                return Set.of();
            }

            @Override
            public boolean isCallerInRole(String role) {
                return false;
            }

            @Override
            public boolean hasAccessToWebResource(String resource, String... methods) {
                return false;
            }

            @Override
            public AuthenticationStatus authenticate(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    AuthenticationParameters parameters
            ) {
                return status;
            }
        };
    }

    private static final class SessionFixture {

        private final Map<String, Object> attributes = new HashMap<>();
        private final Map<String, String> responseHeaders = new HashMap<>();
        private final AtomicInteger sessionRotations = new AtomicInteger();
        private final AtomicBoolean loggedOut = new AtomicBoolean();
        private final AtomicBoolean sessionInvalidated = new AtomicBoolean();
        private final HttpSession session;
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        SessionFixture() {
            session = session();
            request = request();
            response = response();
        }

        private HttpSession session() {
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    (proxy, method, arguments) -> {
                        if (method.getName().equals("getAttribute")) {
                            return attributes.get((String) arguments[0]);
                        }

                        if (method.getName().equals("setAttribute")) {
                            attributes.put((String) arguments[0], arguments[1]);
                            return null;
                        }

                        if (method.getName().equals("removeAttribute")) {
                            attributes.remove((String) arguments[0]);
                            return null;
                        }

                        if (method.getName().equals("invalidate")) {
                            attributes.clear();
                            sessionInvalidated.set(true);
                            return null;
                        }

                        return null;
                    }
            );
        }

        private HttpServletRequest request() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    (proxy, method, arguments) -> {
                        if (method.getName().equals("getSession")) {
                            return session;
                        }

                        if (method.getName().equals("changeSessionId")) {
                            return "rotated-" + sessionRotations.incrementAndGet();
                        }

                        if (method.getName().equals("logout")) {
                            loggedOut.set(true);
                            return null;
                        }

                        if (method.getName().equals("getRemoteAddr")) {
                            return "127.0.0.1";
                        }

                        return null;
                    }
            );
        }

        private HttpServletResponse response() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    (proxy, method, arguments) -> {
                        if (method.getName().equals("setHeader")) {
                            responseHeaders.put(
                                    (String) arguments[0],
                                    (String) arguments[1]
                            );
                        }

                        return null;
                    }
            );
        }
    }
}
