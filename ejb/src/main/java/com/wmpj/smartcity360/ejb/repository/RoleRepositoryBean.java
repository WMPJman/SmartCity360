package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.dao.RoleRepository;
import com.wmpj.smartcity360.core.entity.Role;
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
@Local(RoleRepository.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RoleRepositoryBean implements RoleRepository {

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    public RoleRepositoryBean() {
    }

    RoleRepositoryBean(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
    }

    @Override
    public Optional<Role> findById(long id) {
        return Optional.ofNullable(entityManager.find(Role.class, id));
    }

    @Override
    public Optional<Role> findByCode(String code) {
        Objects.requireNonNull(code, "code");
        return entityManager.createQuery("SELECT r FROM Role r WHERE r.code = :code", Role.class)
                .setParameter("code", code)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Role> findAll() {
        return entityManager.createQuery("SELECT r FROM Role r ORDER BY r.code", Role.class)
                .getResultList();
    }

    @Override
    public boolean existsByCode(String code) {
        Objects.requireNonNull(code, "code");
        return entityManager.createQuery("SELECT COUNT(r) FROM Role r WHERE r.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult() > 0L;
    }
}
