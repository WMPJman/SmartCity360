package com.wmpj.smartcity360.core.dao;

import com.wmpj.smartcity360.core.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long id);

    Optional<User> findByNormalizedEmail(String email);

    boolean existsByEmail(String email);

    User persist(User user);

    User update(User user);

    List<User> findPage(int offset, int limit);
}
