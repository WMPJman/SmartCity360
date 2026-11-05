package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.core.energy.EnergyStreamProcessingException;
import com.wmpj.smartcity360.web.dto.ApiError;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class EnergyStreamExceptionMapper
        implements ExceptionMapper<EnergyStreamProcessingException> {

    @Override
    public Response toResponse(EnergyStreamProcessingException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiError("Energy payload could not be processed"))
                .build();
    }
}
