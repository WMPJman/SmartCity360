package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.dao.UserRoleRepository;
import com.wmpj.smartcity360.core.entity.UserRole;
import com.wmpj.smartcity360.core.entity.UserRoleId;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Stateless
@Local(UserRoleRepository.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserRoleRepositoryBean implements UserRoleRepository {

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    public UserRoleRepositoryBean() {
    }

    UserRoleRepositoryBean(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
    }

    @Override
    public Optional<UserRole> findById(UserRoleId id) {
        return Optional.ofNullable(entityManager.find(UserRole.class, Objects.requireNonNull(id, "id")));
    }

    @Override
    public List<UserRole> findByUserId(long userId) {
        return entityManager.createQuery(
                        "SELECT ur FROM UserRole ur JOIN FETCH ur.role "
                                + "WHERE ur.id.userId = :userId ORDER BY ur.role.code",
                        UserRole.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public boolean exists(long userId, long roleId) {
        return entityManager.createQuery(
                        "SELECT COUNT(ur) FROM UserRole ur "
                                + "WHERE ur.id.userId = :userId AND ur.id.roleId = :roleId",
                        Long.class)
                .setParameter("userId", userId)
                .setParameter("roleId", roleId)
                .getSingleResult() > 0L;
    }

    @Override
    public UserRole persist(UserRole assignment) {
        entityManager.persist(Objects.requireNonNull(assignment, "assignment"));
        entityManager.flush();
        return assignment;
    }

    @Override
    public boolean remove(UserRoleId id) {
        UserRole assignment = entityManager.find(UserRole.class, Objects.requireNonNull(id, "id"));
        if (assignment == null) {
            return false;
        }
        entityManager.remove(assignment);
        entityManager.flush();
        return true;
    }
}
