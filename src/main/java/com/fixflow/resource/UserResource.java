package com.fixflow.resource;

import com.fixflow.dto.CreateUserRequest;
import com.fixflow.dto.PatchUserRequest;
import com.fixflow.dto.UpdateUserRequest;
import com.fixflow.dto.UserResponse;
import com.fixflow.exception.ForbiddenException;
import com.fixflow.model.Role;
import com.fixflow.security.Secured;
import com.fixflow.service.UserService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserService userService = new UserService();

    @GET
    @Secured({Role.ADMIN}) // ADMIN-only
    public Response getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        // Since User filtering isn't requested in Stage 5, we'll just wrap the collection in DataResponse for consistency
        return Response.ok(new com.fixflow.dto.DataResponse<>(users)).build();
    }

    @GET
    @Path("/{id}")
    @Secured // Accessible by authenticated users
    public Response getUserById(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        // Only ADMIN or the user themselves can view this profile
        Integer currentUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        boolean isAdmin = securityContext.isUserInRole(Role.ADMIN.name());
        
        if (!isAdmin && !currentUserId.equals(id)) {
            throw new ForbiddenException("You do not have permission to view this profile.");
        }
        
        UserResponse user = userService.getUserById(id);
        return Response.ok(new com.fixflow.dto.DataResponse<>(user)).build();
    }

    @POST
    @Secured({Role.ADMIN}) // ADMIN-only
    public Response createUser(CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return Response.status(Response.Status.CREATED).entity(new com.fixflow.dto.DataResponse<>(user)).build();
    }

    @PUT
    @Path("/{id}")
    @Secured({Role.ADMIN}) // ADMIN-only
    public Response updateUser(@PathParam("id") Integer id, UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return Response.ok(new com.fixflow.dto.DataResponse<>(user)).build();
    }

    @PATCH
    @Path("/{id}")
    @Secured({Role.ADMIN}) // ADMIN-only
    public Response patchUser(@PathParam("id") Integer id, PatchUserRequest request) {
        UserResponse user = userService.patchUser(id, request);
        return Response.ok(new com.fixflow.dto.DataResponse<>(user)).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured({Role.ADMIN}) // ADMIN-only
    public Response deleteUser(@PathParam("id") Integer id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }
}
