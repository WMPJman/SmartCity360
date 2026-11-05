package com.wmpj.smartcity360.ejb.repository;

import com.wmpj.smartcity360.core.dao.RoleRepository;
import com.wmpj.smartcity360.core.dao.UserRepository;
import com.wmpj.smartcity360.core.dao.UserRoleRepository;
import com.wmpj.smartcity360.core.dao.ZoneRepository;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RepositoryTransactionMetadataTest {

    @Test
    void repositoriesAreLocalStatelessEjbsWithRequiredJtaPersistenceContexts() throws Exception {
        Map<Class<?>, Class<?>> beans = Map.of(
                UserRepositoryBean.class, UserRepository.class,
                RoleRepositoryBean.class, RoleRepository.class,
                UserRoleRepositoryBean.class, UserRoleRepository.class,
                ZoneRepositoryBean.class, ZoneRepository.class
        );

        for (var entry : beans.entrySet()) {
            Class<?> bean = entry.getKey();
            assertNotNull(bean.getAnnotation(Stateless.class));
            assertArrayEquals(new Class<?>[]{entry.getValue()}, bean.getAnnotation(Local.class).value());
            assertEquals(TransactionAttributeType.REQUIRED,
                    bean.getAnnotation(TransactionAttribute.class).value());
            Field entityManager = bean.getDeclaredField("entityManager");
            assertEquals(EntityManager.class, entityManager.getType());
            assertEquals("SmartCity360PU", entityManager.getAnnotation(PersistenceContext.class).unitName());
        }
    }
}
