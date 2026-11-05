package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.service.DatabaseCheckService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/db-check")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class DatabaseCheckResource {

    static final String ENABLED_PROPERTY = "smartcity360.dbCheck.enabled";

    private static final Logger LOGGER = Logger.getLogger(DatabaseCheckResource.class.getName());

    @EJB
    private DatabaseCheckService databaseCheckService;

    private BooleanSupplier endpointEnabled;

    public DatabaseCheckResource() {
        endpointEnabled = () -> Boolean.getBoolean(ENABLED_PROPERTY);
    }

    DatabaseCheckResource(DatabaseCheckService databaseCheckService, BooleanSupplier endpointEnabled) {
        this.databaseCheckService = databaseCheckService;
        this.endpointEnabled = endpointEnabled;
    }

    @GET
    public Map<String, Object> dbCheck() {
        if (!endpointEnabled.getAsBoolean()) {
            throw new IllegalStateException("Database check endpoint is disabled");
        }

        try {
            long rolesCount = databaseCheckService.countRoles();
            return Map.of(
                    "database", "smartcity360_db",
                    "connection", "UP",
                    "rolesCount", rolesCount
            );
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING,
                    "SmartCity360 database verification failed ({0})",
                    exception.getClass().getName());
            throw new IllegalStateException("Database check failed");
        }
    }
}
