package com.fixflow.exception;

import com.fixflow.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ResourceNotFoundException) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    "Not Found",
                    exception.getMessage()
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof ValidationException) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    "Bad Request",
                    exception.getMessage()
            );
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof UnauthorizedException) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Unauthorized",
                    exception.getMessage()
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof ForbiddenException) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.FORBIDDEN.getStatusCode(),
                    "Forbidden",
                    exception.getMessage()
            );
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof ConflictException) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.CONFLICT.getStatusCode(),
                    "Conflict",
                    exception.getMessage()
            );
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            ErrorResponse errorResponse = new ErrorResponse(
                    webEx.getResponse().getStatus(),
                    webEx.getResponse().getStatusInfo().getReasonPhrase(),
                    webEx.getMessage()
            );
            return Response.status(webEx.getResponse().getStatus())
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // For all other unhandled exceptions (e.g. database errors)
        exception.printStackTrace(); // Log internally
        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                "An unexpected error occurred."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
