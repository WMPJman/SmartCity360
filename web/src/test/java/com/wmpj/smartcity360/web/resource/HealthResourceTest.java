package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.service.HealthService;
import com.wmpj.smartcity360.web.config.RestApplication;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HealthResourceTest {

    @Test
    void exposesExpectedRestPaths() throws NoSuchMethodException {
        ApplicationPath applicationPath = RestApplication.class.getAnnotation(ApplicationPath.class);
        Path resourcePath = HealthResource.class.getAnnotation(Path.class);
        Method healthMethod = HealthResource.class.getMethod("health");

        assertNotNull(applicationPath);
        assertEquals("/api", applicationPath.value());
        assertNotNull(resourcePath);
        assertEquals("/health", resourcePath.value());
        assertNotNull(HealthResource.class.getAnnotation(RequestScoped.class));
        assertNotNull(healthMethod.getAnnotation(GET.class));
    }

    @Test
    void healthReturnsApplicationAndServiceStatus() {
        HealthService service = () -> "UP";
        HealthResource resource = new HealthResource(service);

        Map<String, String> response = resource.health();

        assertEquals("SmartCity360", response.get("application"));
        assertEquals("UP", response.get("status"));
    }
}
