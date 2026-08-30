package com.fixflow.resource;

import com.fixflow.dto.AuthResponse;
import com.fixflow.dto.LoginRequest;
import com.fixflow.dto.RegisterRequest;
import com.fixflow.service.AuthService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService = new AuthService();

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(new com.fixflow.dto.DataResponse<>(response)).build();
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        AuthResponse response = authService.login(request);
        return Response.ok(new com.fixflow.dto.DataResponse<>(response)).build();
    }
}
