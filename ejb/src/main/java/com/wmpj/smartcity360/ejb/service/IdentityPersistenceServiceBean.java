package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.dao.RoleRepository;
import com.wmpj.smartcity360.core.dao.UserRepository;
import com.wmpj.smartcity360.core.dao.UserRoleRepository;
import com.wmpj.smartcity360.core.dto.UserSummary;
import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserRoleId;
import com.wmpj.smartcity360.core.exception.DuplicateResourceException;
import com.wmpj.smartcity360.core.exception.ResourceNotFoundException;
import com.wmpj.smartcity360.core.service.IdentityPersistenceService;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Stateless
@Local(IdentityPersistenceService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class IdentityPersistenceServiceBean implements IdentityPersistenceService {

    @EJB
    private UserRepository userRepository;

    @EJB
    private RoleRepository roleRepository;

    @EJB
    private UserRoleRepository userRoleRepository;

    public IdentityPersistenceServiceBean() {
    }

    IdentityPersistenceServiceBean(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository
    ) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
        this.roleRepository = Objects.requireNonNull(roleRepository, "roleRepository");
        this.userRoleRepository = Objects.requireNonNull(userRoleRepository, "userRoleRepository");
    }

    @Override
    @PermitAll
    public Optional<UserSummary> findUser(long userId) {
        return userRepository.findById(userId).map(IdentityPersistenceServiceBean::toSummary);
    }

    @Override
    @PermitAll
    public List<Role> findRoles(long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRole)
                .toList();
    }

    @Override
    @PermitAll
    public boolean roleExists(String roleCode) {
        return roleRepository.existsByCode(Objects.requireNonNull(roleCode, "roleCode"));
    }

    @Override
    @RolesAllowed(SecurityRoles.ADMIN)
    public User createUser(User user) {
        Objects.requireNonNull(user, "user");
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("A user with that email already exists");
        }
        return userRepository.persist(user);
    }

    @Override
    @RolesAllowed(SecurityRoles.ADMIN)
    public User updateUser(User user) {
        Objects.requireNonNull(user, "user");
        userRepository.findByNormalizedEmail(user.getEmail())
                .filter(existing -> !Objects.equals(existing.getId(), user.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A user with that email already exists");
                });
        return userRepository.update(user);
    }

    @Override
    @RolesAllowed(SecurityRoles.ADMIN)
    public UserRole assignRole(long userId, String roleCode, OffsetDateTime assignedAt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
        Role role = roleRepository.findByCode(Objects.requireNonNull(roleCode, "roleCode"))
                .orElseThrow(() -> new ResourceNotFoundException("Role was not found"));
        if (userRoleRepository.exists(userId, role.getId())) {
            throw new DuplicateResourceException("The user already has that role");
        }
        return userRoleRepository.persist(new UserRole(user, role,
                Objects.requireNonNull(assignedAt, "assignedAt")));
    }

    @Override
    @RolesAllowed(SecurityRoles.ADMIN)
    public boolean removeRole(UserRoleId id) {
        return userRoleRepository.remove(Objects.requireNonNull(id, "id"));
    }

    private static UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
