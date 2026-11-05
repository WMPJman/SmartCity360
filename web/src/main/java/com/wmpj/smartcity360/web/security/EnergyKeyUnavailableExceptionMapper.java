package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.core.energy.EnergyKeyUnavailableException;
import com.wmpj.smartcity360.web.dto.ApiError;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class EnergyKeyUnavailableExceptionMapper
        implements ExceptionMapper<EnergyKeyUnavailableException> {

    @Override
    public Response toResponse(EnergyKeyUnavailableException exception) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiError("Energy processing keys are unavailable"))
                .build();
    }
}
