package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.entity.User;
import com.wmpj.smartcity360.core.entity.UserStatus;
import com.wmpj.smartcity360.ejb.support.JpaTestDouble;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRepositoryBeanTest {

    @Test
    void emailLookupUsesDatabaseLowerSemanticsWithoutMutatingTheEntity() {
        JpaTestDouble jpa = new JpaTestDouble();
        User user = user("Mixed.Case@Example.test");
        jpa.results = List.of(user);
        UserRepositoryBean repository = new UserRepositoryBean(jpa.entityManager());

        assertSame(user, repository.findByNormalizedEmail("MIXED.CASE@EXAMPLE.TEST").orElseThrow());
        assertEquals("SELECT u FROM SmartCityUser u WHERE LOWER(u.email) = :email", jpa.jpql.get(0));
        assertEquals("mixed.case@example.test", jpa.parameters.get("email"));
        assertEquals("Mixed.Case@Example.test", user.getEmail());
    }

    @Test
    void pageIsDeterministicAndRejectsUnsafeBounds() {
        JpaTestDouble jpa = new JpaTestDouble();
        UserRepositoryBean repository = new UserRepositoryBean(jpa.entityManager());

        repository.findPage(10, 20);

        assertEquals("SELECT u FROM SmartCityUser u ORDER BY u.id", jpa.jpql.get(0));
        assertEquals(10, jpa.firstResult);
        assertEquals(20, jpa.maxResults);
        assertThrows(IllegalArgumentException.class, () -> repository.findPage(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> repository.findPage(0, 0));
        assertThrows(IllegalArgumentException.class, () -> repository.findPage(0, 101));
    }

    @Test
    void writesFlushThroughTheContainerManagedEntityManager() {
        JpaTestDouble jpa = new JpaTestDouble();
        UserRepositoryBean repository = new UserRepositoryBean(jpa.entityManager());
        User user = user("user@example.test");

        assertSame(user, repository.persist(user));
        setId(user, 7L);
        assertSame(user, repository.update(user));
        assertSame(user, jpa.persisted);
        assertSame(user, jpa.merged);
        assertEquals(2, jpa.flushes);
    }

    @Test
    void updateRejectsTransientUsersInsteadOfSilentlyInsertingThem() {
        UserRepositoryBean repository = new UserRepositoryBean(new JpaTestDouble().entityManager());

        assertThrows(IllegalArgumentException.class,
                () -> repository.update(user("new-user@example.test")));
    }

    private static User user(String email) {
        return new User("Test User", email, "synthetic-hash", UserStatus.ACTIVE,
                OffsetDateTime.MIN, OffsetDateTime.MIN);
    }

    private static void setId(User user, long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
