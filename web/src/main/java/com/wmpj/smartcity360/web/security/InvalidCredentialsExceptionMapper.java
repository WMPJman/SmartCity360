package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.web.dto.ApiError;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class InvalidCredentialsExceptionMapper
        implements ExceptionMapper<InvalidCredentialsException> {

    @Override
    public Response toResponse(InvalidCredentialsException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiError("Invalid email or password"))
                .build();
    }
}
