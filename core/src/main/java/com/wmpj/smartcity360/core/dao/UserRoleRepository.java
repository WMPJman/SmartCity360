package com.wmpj.smartcity360.core.dao;

import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserRoleId;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository {
    Optional<UserRole> findById(UserRoleId id);

    List<UserRole> findByUserId(long userId);

    boolean exists(long userId, long roleId);

    UserRole persist(UserRole assignment);

    boolean remove(UserRoleId id);
}
