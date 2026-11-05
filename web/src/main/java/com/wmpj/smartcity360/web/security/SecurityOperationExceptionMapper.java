package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.web.dto.ApiError;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class SecurityOperationExceptionMapper
        implements ExceptionMapper<SecurityOperationException> {

    @Override
    public Response toResponse(SecurityOperationException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiError("The security operation could not be completed"))
                .build();
    }
}
