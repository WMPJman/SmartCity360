package com.wmpj.smartcity360.ejb.security;

import com.wmpj.smartcity360.core.dao.UserRepository;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserStatus;
import com.wmpj.smartcity360.core.exception.UnauthenticatedException;
import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.CurrentIdentityService;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.security.Principal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Stateless
@Local(CurrentIdentityService.class)
@PermitAll
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class CurrentIdentityServiceBean implements CurrentIdentityService {

    @EJB
    private UserRepository userRepository;

    @Resource
    private SessionContext sessionContext;

    public CurrentIdentityServiceBean() {
    }

    CurrentIdentityServiceBean(
            UserRepository userRepository,
            SessionContext sessionContext
    ) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext");
    }

    @Override
    public AuthenticatedIdentity getCurrentIdentity() {
        Principal callerPrincipal = sessionContext.getCallerPrincipal();

        if (callerPrincipal == null
                || callerPrincipal.getName() == null
                || callerPrincipal.getName().isBlank()) {
            throw unauthenticated();
        }

        User user = userRepository.findByNormalizedEmail(callerPrincipal.getName())
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(CurrentIdentityServiceBean::unauthenticated);

        Set<String> callerRoles = new HashSet<>();

        for (String roleCode : SecurityRoles.ALL) {
            if (sessionContext.isCallerInRole(roleCode)) {
                callerRoles.add(roleCode);
            }
        }

        return new AuthenticatedIdentity(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                callerRoles
        );
    }

    private static UnauthenticatedException unauthenticated() {
        return new UnauthenticatedException("Authentication is required");
    }
}
