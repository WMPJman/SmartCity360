package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;

public interface CurrentIdentityService {

    AuthenticatedIdentity getCurrentIdentity();
}
