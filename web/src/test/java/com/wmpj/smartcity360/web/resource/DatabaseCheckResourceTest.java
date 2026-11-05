package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.service.DatabaseCheckService;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseCheckResourceTest {

    @Test
    void dbCheckReturnsTheActualRoleCount() {
        DatabaseCheckResource resource = new DatabaseCheckResource(() -> 5L, () -> true);

        Map<String, Object> response = resource.dbCheck();

        assertEquals("smartcity360_db", response.get("database"));
        assertEquals("UP", response.get("connection"));
        assertEquals(5L, response.get("rolesCount"));
    }

    @Test
    void dbCheckIsDisabledByDefaultWithoutCallingTheDatabase() {
        AtomicBoolean called = new AtomicBoolean(false);
        DatabaseCheckService service = () -> {
            called.set(true);
            return 5L;
        };
        DatabaseCheckResource resource = new DatabaseCheckResource(service, () -> false);

        IllegalStateException error = assertThrows(IllegalStateException.class, resource::dbCheck);

        assertEquals("Database check endpoint is disabled", error.getMessage());
        assertFalse(called.get());
    }

    @Test
    void dbCheckDoesNotExposeDatabaseExceptionDetails() {
        DatabaseCheckResource resource = new DatabaseCheckResource(
                () -> { throw new IllegalArgumentException("password=secret"); },
                () -> true
        );

        IllegalStateException error = assertThrows(IllegalStateException.class, resource::dbCheck);

        assertEquals("Database check failed", error.getMessage());
    }
}
