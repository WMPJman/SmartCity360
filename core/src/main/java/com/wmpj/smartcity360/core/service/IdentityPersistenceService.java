package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.dto.UserSummary;
import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserRoleId;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface IdentityPersistenceService {
    Optional<UserSummary> findUser(long userId);

    List<Role> findRoles(long userId);

    boolean roleExists(String roleCode);

    User createUser(User user);

    User updateUser(User user);

    UserRole assignRole(long userId, String roleCode, OffsetDateTime assignedAt);

    boolean removeRole(UserRoleId id);
}
