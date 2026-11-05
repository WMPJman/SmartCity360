package com.wmpj.smartcity360.core.dao;

import com.wmpj.smartcity360.core.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findById(long id);

    Optional<Role> findByCode(String code);

    List<Role> findAll();

    boolean existsByCode(String code);
}
