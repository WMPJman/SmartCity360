package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserRoleId;
import com.wmpj.smartcity360.ejb.support.JpaTestDouble;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRoleRepositoryBeanTest {

    @Test
    void compositeKeyLookupsAndRoleListsUseTheMappedId() throws Exception {
        JpaTestDouble jpa = new JpaTestDouble();
        UserRole assignment = newAssignment();
        UserRoleId id = new UserRoleId(7L, 9L);
        jpa.findResult(UserRole.class, id, assignment);
        UserRoleRepositoryBean repository = new UserRoleRepositoryBean(jpa.entityManager());

        assertSame(assignment, repository.findById(id).orElseThrow());
        jpa.results = List.of(assignment);
        assertEquals(List.of(assignment), repository.findByUserId(7L));
        assertEquals("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.id.userId = :userId ORDER BY ur.role.code",
                jpa.jpql.get(0));
        assertEquals(7L, jpa.parameters.get("userId"));
    }

    @Test
    void removeReportsWhetherTheCompositeAssignmentExisted() throws Exception {
        JpaTestDouble jpa = new JpaTestDouble();
        UserRoleRepositoryBean repository = new UserRoleRepositoryBean(jpa.entityManager());
        UserRoleId missing = new UserRoleId(1L, 2L);
        assertFalse(repository.remove(missing));

        UserRole assignment = newAssignment();
        UserRoleId existing = new UserRoleId(7L, 9L);
        jpa.findResult(UserRole.class, existing, assignment);
        assertTrue(repository.remove(existing));
        assertSame(assignment, jpa.removed);
        assertEquals(1, jpa.flushes);
    }

    private static UserRole newAssignment() throws Exception {
        var constructor = UserRole.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
