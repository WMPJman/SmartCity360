package com.wmpj.smartcity360.web.security;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfTokenManagerTest {

    @Test
    void acceptsOnlyTheTokenBoundToTheSameSession() {
        CsrfTokenManager manager = new CsrfTokenManager();
        HttpSession firstSession = session(new HashMap<>());
        HttpSession secondSession = session(new HashMap<>());

        String firstToken = manager.getOrCreate(firstSession);
        String secondToken = manager.getOrCreate(secondSession);

        assertTrue(manager.isValid(firstSession, firstToken));
        assertFalse(manager.isValid(firstSession, secondToken));
        assertFalse(manager.isValid(secondSession, firstToken));
        assertFalse(manager.isValid(firstSession, null));
    }

    @Test
    void rotationAndInvalidationMakeThePreviousTokenUnusable() {
        CsrfTokenManager manager = new CsrfTokenManager();
        HttpSession session = session(new HashMap<>());
        String originalToken = manager.getOrCreate(session);

        String rotatedToken = manager.rotate(session);

        assertNotEquals(originalToken, rotatedToken);
        assertFalse(manager.isValid(session, originalToken));
        assertTrue(manager.isValid(session, rotatedToken));

        manager.invalidate(session);

        assertFalse(manager.isValid(session, rotatedToken));
    }

    static HttpSession session(Map<String, Object> attributes) {
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

                    return null;
                }
        );
    }
}
