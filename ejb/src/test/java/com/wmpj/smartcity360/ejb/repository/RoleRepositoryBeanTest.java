package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.entity.Role;
import com.wmpj.smartcity360.ejb.support.JpaTestDouble;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RoleRepositoryBeanTest {

    @Test
    void roleQueriesUseExactCodesAndDeterministicOrdering() throws Exception {
        JpaTestDouble jpa = new JpaTestDouble();
        var constructor = Role.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Role role = constructor.newInstance();
        jpa.results = List.of(role);
        RoleRepositoryBean repository = new RoleRepositoryBean(jpa.entityManager());

        assertSame(role, repository.findByCode("ADMIN").orElseThrow());
        assertEquals("SELECT r FROM Role r WHERE r.code = :code", jpa.jpql.get(0));
        assertEquals("ADMIN", jpa.parameters.get("code"));

        repository.findAll();
        assertEquals("SELECT r FROM Role r ORDER BY r.code", jpa.jpql.get(1));
    }
}
