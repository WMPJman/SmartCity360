package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.dao.ZoneRepository;
import com.wmpj.smartcity360.core.entity.Zone;
import com.wmpj.smartcity360.core.entity.ZoneType;
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
@Local(ZoneRepository.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ZoneRepositoryBean implements ZoneRepository {

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    public ZoneRepositoryBean() {
    }

    ZoneRepositoryBean(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
    }

    @Override
    public Optional<Zone> findById(long id) {
        return Optional.ofNullable(entityManager.find(Zone.class, id));
    }

    @Override
    public Optional<Zone> findByCode(String code) {
        Objects.requireNonNull(code, "code");
        return entityManager.createQuery("SELECT z FROM Zone z WHERE z.zoneCode = :code", Zone.class)
                .setParameter("code", code)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByCode(String code) {
        Objects.requireNonNull(code, "code");
        return entityManager.createQuery("SELECT COUNT(z) FROM Zone z WHERE z.zoneCode = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult() > 0L;
    }

    @Override
    public List<Zone> findRootCities(int limit) {
        QueryBounds.requireLimit(limit);
        return entityManager.createQuery(
                        "SELECT z FROM Zone z WHERE z.parent IS NULL AND z.zoneType = :type ORDER BY z.id",
                        Zone.class)
                .setParameter("type", ZoneType.CITY)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Zone> findDirectChildren(long parentId, int limit) {
        QueryBounds.requireLimit(limit);
        return entityManager.createQuery(
                        "SELECT z FROM Zone z WHERE z.parent.id = :parentId ORDER BY z.id", Zone.class)
                .setParameter("parentId", parentId)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Zone> findByType(ZoneType type, int offset, int limit) {
        Objects.requireNonNull(type, "type");
        QueryBounds.requirePage(offset, limit);
        return entityManager.createQuery("SELECT z FROM Zone z WHERE z.zoneType = :type ORDER BY z.id", Zone.class)
                .setParameter("type", type)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public Zone persist(Zone zone) {
        entityManager.persist(Objects.requireNonNull(zone, "zone"));
        entityManager.flush();
        return zone;
    }

    @Override
    public Zone update(Zone zone) {
        Objects.requireNonNull(zone, "zone");
        if (zone.getId() == null) {
            throw new IllegalArgumentException("zone must have an id before update");
        }
        Zone managed = entityManager.merge(zone);
        entityManager.flush();
        return managed;
    }
}
