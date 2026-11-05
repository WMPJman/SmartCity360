package com.wmpj.smartcity360.web.security;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Produces;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityExceptionMapperTest {

    @Test
    void allSecurityErrorsExplicitlyProduceJson() {
        List<Class<?>> mapperTypes = List.of(
                CsrfViolationExceptionMapper.class,
                UnauthenticatedExceptionMapper.class,
                InvalidCredentialsExceptionMapper.class,
                InvalidRequestExceptionMapper.class,
                EjbAccessExceptionMapper.class,
                SecurityOperationExceptionMapper.class
        );

        for (Class<?> mapperType : mapperTypes) {
            Produces produces = mapperType.getAnnotation(Produces.class);

            assertEquals(MediaType.APPLICATION_JSON, produces.value()[0]);
        }
    }
}
