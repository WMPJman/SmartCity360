package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.service.HealthService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class HealthResource {

    @EJB
    private HealthService healthService;

    public HealthResource() {
    }

    HealthResource(HealthService healthService) {
        this.healthService = healthService;
    }

    @GET
    public Map<String, String> health() {
        return Map.of(
                "application", "SmartCity360",
                "status", healthService.getStatus()
        );
    }
}
