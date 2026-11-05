package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.dao.UserRepository;
import com.wmpj.smartcity360.core.entity.User;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Stateless
@Local(UserRepository.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserRepositoryBean implements UserRepository {

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    public UserRepositoryBean() {
    }

    UserRepositoryBean(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public Optional<User> findByNormalizedEmail(String email) {
        return entityManager.createQuery(
                        "SELECT u FROM SmartCityUser u WHERE LOWER(u.email) = :email", User.class)
                .setParameter("email", normalizeEmail(email))
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return entityManager.createQuery(
                        "SELECT COUNT(u) FROM SmartCityUser u WHERE LOWER(u.email) = :email", Long.class)
                .setParameter("email", normalizeEmail(email))
                .getSingleResult() > 0L;
    }

    @Override
    public User persist(User user) {
        entityManager.persist(Objects.requireNonNull(user, "user"));
        entityManager.flush();
        return user;
    }

    @Override
    public User update(User user) {
        Objects.requireNonNull(user, "user");
        if (user.getId() == null) {
            throw new IllegalArgumentException("user must have an id before update");
        }
        User managed = entityManager.merge(user);
        entityManager.flush();
        return managed;
    }

    @Override
    public List<User> findPage(int offset, int limit) {
        QueryBounds.requirePage(offset, limit);
        return entityManager.createQuery("SELECT u FROM SmartCityUser u ORDER BY u.id", User.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    private static String normalizeEmail(String email) {
        Objects.requireNonNull(email, "email");
        return email.toLowerCase(Locale.ROOT);
    }
}
