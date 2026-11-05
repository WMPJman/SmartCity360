package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.core.complaint.ComplaintWorkflowException;
import com.wmpj.smartcity360.web.dto.ApiError;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ComplaintWorkflowExceptionMapper
        implements ExceptionMapper<ComplaintWorkflowException> {

    @Override
    public Response toResponse(ComplaintWorkflowException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiError(exception.getMessage()))
                .build();
    }
}
