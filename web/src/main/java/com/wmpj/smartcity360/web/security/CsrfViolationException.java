package com.wmpj.smartcity360.web.security;

public class CsrfViolationException extends RuntimeException {

    public CsrfViolationException() {
        super("The CSRF token is missing or invalid");
    }
}
