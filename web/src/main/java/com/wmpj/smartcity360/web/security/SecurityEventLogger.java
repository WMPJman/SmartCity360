package com.wmpj.smartcity360.web.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SecurityEventLogger {

    private static final Logger LOGGER = Logger.getLogger(SecurityEventLogger.class.getName());

    public void loginSucceeded(String principalName, String remoteAddress) {
        LOGGER.log(
                Level.INFO,
                "event=LOGIN_SUCCESS principal={0} remoteAddress={1}",
                new Object[]{safe(principalName), safe(remoteAddress)}
        );
    }

    public void loginFailed(String remoteAddress) {
        LOGGER.log(
                Level.WARNING,
                "event=LOGIN_FAILURE remoteAddress={0}",
                safe(remoteAddress)
        );
    }

    public void loggedOut(String principalName, String remoteAddress) {
        LOGGER.log(
                Level.INFO,
                "event=LOGOUT principal={0} remoteAddress={1}",
                new Object[]{safe(principalName), safe(remoteAddress)}
        );
    }

    public void accessDenied(String reason, String remoteAddress) {
        LOGGER.log(
                Level.WARNING,
                "event=ACCESS_DENIED reason={0} remoteAddress={1}",
                new Object[]{safe(reason), safe(remoteAddress)}
        );
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value.replaceAll("[\\r\\n\\t]", "_");
    }
}
