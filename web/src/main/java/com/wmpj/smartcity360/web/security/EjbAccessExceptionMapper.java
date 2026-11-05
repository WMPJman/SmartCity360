package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.web.dto.ApiError;
import jakarta.ejb.EJBAccessException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class EjbAccessExceptionMapper implements ExceptionMapper<EJBAccessException> {

    @Override
    public Response toResponse(EJBAccessException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiError("Access is denied"))
                .build();
    }
}
