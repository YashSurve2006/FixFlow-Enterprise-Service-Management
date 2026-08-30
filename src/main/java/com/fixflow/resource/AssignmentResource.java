package com.fixflow.resource;

import com.fixflow.dto.AssignTechnicianRequest;
import com.fixflow.dto.AssignmentResponse;
import com.fixflow.dto.UpdateAssignmentRequest;
import com.fixflow.model.Role;
import com.fixflow.security.Secured;
import com.fixflow.service.RequestAssignmentService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/assignments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured // All endpoints require authentication
public class AssignmentResource {

    private final RequestAssignmentService service = new RequestAssignmentService();

    @POST
    @Secured({Role.ADMIN}) // Only ADMIN can assign technicians
    public Response assignTechnician(
            @QueryParam("requestId") Integer queryRequestId, 
            AssignTechnicianRequest requestDto, 
            @Context SecurityContext securityContext) {
        
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        
        // Either passed via query parameter or we can define a different route 
        // e.g., POST /api/requests/{id}/assignment
        // The instructions suggested POST /api/requests/{requestId}/assignment
        // I will just use the query parameter if it's called on /api/assignments
        if (queryRequestId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"requestId query parameter is required\"}").build();
        }

        AssignmentResponse response = service.assignTechnician(queryRequestId, requestDto, authUserId);
        return Response.status(Response.Status.CREATED).entity(new com.fixflow.dto.DataResponse<>(response)).build();
    }

    @GET
    @Secured({Role.ADMIN})
    public Response getAllAssignments() {
        List<AssignmentResponse> assignments = service.getAllAssignments();
        return Response.ok(new com.fixflow.dto.DataResponse<>(assignments)).build();
    }

    @GET
    @Path("/{id}")
    @Secured({Role.ADMIN, Role.TECHNICIAN, Role.USER})
    public Response getAssignmentById(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        Role authRole = getRoleFromContext(securityContext);
        
        AssignmentResponse response = service.getAssignmentById(id, authUserId, authRole);
        return Response.ok(new com.fixflow.dto.DataResponse<>(response)).build();
    }

    @PUT
    @Path("/{id}")
    @Secured({Role.ADMIN})
    public Response updateAssignment(@PathParam("id") Integer id, UpdateAssignmentRequest requestDto) {
        AssignmentResponse response = service.updateAssignment(id, requestDto);
        return Response.ok(new com.fixflow.dto.DataResponse<>(response)).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured({Role.ADMIN})
    public Response deleteAssignment(@PathParam("id") Integer id) {
        service.deleteAssignment(id);
        return Response.noContent().build();
    }
    
    private Role getRoleFromContext(SecurityContext securityContext) {
        for (Role role : Role.values()) {
            if (securityContext.isUserInRole(role.name())) {
                return role;
            }
        }
        return Role.USER;
    }
}
