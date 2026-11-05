package com.wmpj.smartcity360.ejb.security;

import com.wmpj.smartcity360.core.dao.UserRepository;
import com.wmpj.smartcity360.core.dao.UserRoleRepository;
import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserStatus;
import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.AuthenticationService;
import com.wmpj.smartcity360.core.service.PasswordHashService;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Local(AuthenticationService.class)
@PermitAll
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class AuthenticationServiceBean implements AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationServiceBean.class.getName());

    private static final String DUMMY_PASSWORD_HASH =
            "$pbkdf2-sha256$i=600000,l=32$AAAAAAAAAAAAAAAAAAAAAA$"
                    + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @EJB
    private UserRepository userRepository;

    @EJB
    private UserRoleRepository userRoleRepository;

    @EJB
    private PasswordHashService passwordHashService;

    public AuthenticationServiceBean() {
    }

    AuthenticationServiceBean(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordHashService passwordHashService
    ) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
        this.userRoleRepository = Objects.requireNonNull(userRoleRepository, "userRoleRepository");
        this.passwordHashService = Objects.requireNonNull(passwordHashService, "passwordHashService");
    }

    @Override
    public Optional<AuthenticatedIdentity> authenticate(String email, char[] password) {
        char[] passwordCopy = copyPassword(password);

        try {
            String normalizedEmail = normalizeEmail(email);
            Optional<User> userResult = userRepository.findByNormalizedEmail(normalizedEmail);

            if (userResult.isEmpty()) {
                passwordHashService.verify(passwordCopy, DUMMY_PASSWORD_HASH);
                return Optional.empty();
            }

            User user = userResult.orElseThrow();
            boolean passwordMatches = passwordHashService.verify(
                    passwordCopy,
                    user.getPasswordHash()
            );

            if (!passwordMatches || user.getStatus() != UserStatus.ACTIVE) {
                return Optional.empty();
            }

            Set<String> trustedRoles = resolveTrustedRoles(user.getId());
            AuthenticatedIdentity identity = new AuthenticatedIdentity(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    trustedRoles
            );

            return Optional.of(identity);
        } catch (RuntimeException exception) {
            LOGGER.log(
                    Level.WARNING,
                    "event=AUTHENTICATION_BACKEND_FAILURE exceptionType={0}",
                    exception.getClass().getName()
            );

            return Optional.empty();
        } finally {
            Arrays.fill(passwordCopy, '\0');
        }
    }

    private Set<String> resolveTrustedRoles(long userId) {
        Set<String> trustedRoles = new HashSet<>();

        for (UserRole assignment : userRoleRepository.findByUserId(userId)) {
            if (assignment == null) {
                continue;
            }

            Role role = assignment.getRole();

            if (role == null || role.getCode() == null) {
                continue;
            }

            if (SecurityRoles.ALL.contains(role.getCode())) {
                trustedRoles.add(role.getCode());
            }
        }

        return Set.copyOf(trustedRoles);
    }

    private static char[] copyPassword(char[] password) {
        if (password == null) {
            return new char[0];
        }

        return Arrays.copyOf(password, password.length);
    }

    private static String normalizeEmail(String email) {
        Objects.requireNonNull(email, "email");
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        return normalizedEmail;
    }
}
