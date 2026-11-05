package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.service.CurrentIdentityService;
import com.wmpj.smartcity360.web.dto.AuthenticatedUserResponse;
import com.wmpj.smartcity360.web.dto.CsrfTokenResponse;
import com.wmpj.smartcity360.web.dto.LoginRequest;
import com.wmpj.smartcity360.web.security.CsrfTokenManager;
import com.wmpj.smartcity360.web.security.InvalidCredentialsException;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import com.wmpj.smartcity360.web.security.SecurityEventLogger;
import com.wmpj.smartcity360.web.security.SecurityOperationException;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import java.util.Objects;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class AuthenticationResource {

    @Inject
    private SecurityContext securityContext;

    @EJB
    private CurrentIdentityService currentIdentityService;

    @Inject
    private CsrfTokenManager csrfTokenManager;

    @Inject
    private SecurityEventLogger securityEventLogger;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    public AuthenticationResource() {
    }

    AuthenticationResource(
            SecurityContext securityContext,
            CurrentIdentityService currentIdentityService,
            CsrfTokenManager csrfTokenManager,
            SecurityEventLogger securityEventLogger,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        this.securityContext = Objects.requireNonNull(securityContext, "securityContext");
        this.currentIdentityService = Objects.requireNonNull(
                currentIdentityService,
                "currentIdentityService"
        );
        this.csrfTokenManager = Objects.requireNonNull(csrfTokenManager, "csrfTokenManager");
        this.securityEventLogger = Objects.requireNonNull(
                securityEventLogger,
                "securityEventLogger"
        );
        this.request = Objects.requireNonNull(request, "request");
        this.response = Objects.requireNonNull(response, "response");
    }

    @GET
    @Path("/csrf")
    public CsrfTokenResponse csrfToken() {
        HttpSession session = request.getSession(true);
        String token = csrfTokenManager.getOrCreate(session);
        response.setHeader(CsrfTokenManager.HEADER_NAME, token);
        return new CsrfTokenResponse(token);
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public AuthenticatedUserResponse login(LoginRequest loginRequest) {
        validate(loginRequest);
        UsernamePasswordCredential credential = new UsernamePasswordCredential(
                loginRequest.getEmail().trim(),
                loginRequest.getPassword()
        );

        try {
            AuthenticationParameters parameters = AuthenticationParameters.withParams()
                    .credential(credential)
                    .newAuthentication(true);
            AuthenticationStatus status = securityContext.authenticate(
                    request,
                    response,
                    parameters
            );

            if (status != AuthenticationStatus.SUCCESS) {
                securityEventLogger.loginFailed(request.getRemoteAddr());
                throw new InvalidCredentialsException();
            }

            HttpSession session = request.getSession(false);

            if (session == null) {
                throw new SecurityOperationException(
                        "Authenticated session was not established",
                        null
                );
            }

            request.changeSessionId();
            String csrfToken = csrfTokenManager.rotate(session);
            response.setHeader(CsrfTokenManager.HEADER_NAME, csrfToken);

            AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
            securityEventLogger.loginSucceeded(identity.email(), request.getRemoteAddr());
            return AuthenticatedUserResponse.from(identity);
        } finally {
            credential.clearCredential();
            loginRequest.setPassword(null);
        }
    }

    @GET
    @Path("/me")
    public AuthenticatedUserResponse currentUser() {
        return AuthenticatedUserResponse.from(currentIdentityService.getCurrentIdentity());
    }

    @POST
    @Path("/logout")
    public void logout() {
        AuthenticatedIdentity identity = currentIdentityService.getCurrentIdentity();
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new InvalidCredentialsException();
        }

        try {
            request.logout();
            csrfTokenManager.invalidate(session);
            session.invalidate();
            securityEventLogger.loggedOut(identity.email(), request.getRemoteAddr());
        } catch (ServletException exception) {
            throw new SecurityOperationException("Logout failed", exception);
        }
    }

    private static void validate(LoginRequest loginRequest) {
        if (loginRequest == null
                || loginRequest.getEmail() == null
                || loginRequest.getEmail().isBlank()
                || loginRequest.getPassword() == null
                || loginRequest.getPassword().isEmpty()) {
            throw new InvalidRequestException("Email and password are required");
        }
    }
}
