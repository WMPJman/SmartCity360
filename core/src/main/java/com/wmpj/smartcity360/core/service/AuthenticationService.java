package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;

import java.util.Optional;

public interface AuthenticationService {

    Optional<AuthenticatedIdentity> authenticate(String email, char[] password);
}
