package com.wmpj.smartcity360.web.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@ApplicationScoped
public class CsrfTokenManager {

    public static final String HEADER_NAME = "X-CSRF-Token";

    private static final String SESSION_ATTRIBUTE =
            CsrfTokenManager.class.getName() + ".token";
    private static final int TOKEN_LENGTH_BYTES = 32;

    private final SecureRandom secureRandom;

    public CsrfTokenManager() {
        secureRandom = new SecureRandom();
    }

    CsrfTokenManager(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
    }

    public String getOrCreate(HttpSession session) {
        Objects.requireNonNull(session, "session");
        Object existingToken = session.getAttribute(SESSION_ATTRIBUTE);

        if (existingToken instanceof String token && !token.isBlank()) {
            return token;
        }

        return rotate(session);
    }

    public String rotate(HttpSession session) {
        Objects.requireNonNull(session, "session");
        byte[] randomBytes = new byte[TOKEN_LENGTH_BYTES];
        secureRandom.nextBytes(randomBytes);

        try {
            String token = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);
            session.setAttribute(SESSION_ATTRIBUTE, token);
            return token;
        } finally {
            java.util.Arrays.fill(randomBytes, (byte) 0);
        }
    }

    public boolean isValid(HttpSession session, String submittedToken) {
        if (session == null || submittedToken == null || submittedToken.isBlank()) {
            return false;
        }

        Object storedValue = session.getAttribute(SESSION_ATTRIBUTE);

        if (!(storedValue instanceof String storedToken)) {
            return false;
        }

        byte[] storedBytes = storedToken.getBytes(StandardCharsets.UTF_8);
        byte[] submittedBytes = submittedToken.getBytes(StandardCharsets.UTF_8);

        try {
            return MessageDigest.isEqual(storedBytes, submittedBytes);
        } finally {
            java.util.Arrays.fill(storedBytes, (byte) 0);
            java.util.Arrays.fill(submittedBytes, (byte) 0);
        }
    }

    public void invalidate(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_ATTRIBUTE);
        }
    }
}
