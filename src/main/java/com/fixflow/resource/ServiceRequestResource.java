package com.fixflow.resource;

import com.fixflow.dto.CreateServiceRequestRequest;
import com.fixflow.dto.PatchServiceRequestRequest;
import com.fixflow.dto.ServiceRequestResponse;
import com.fixflow.dto.UpdateServiceRequestRequest;
import com.fixflow.model.Role;
import com.fixflow.security.Secured;
import com.fixflow.security.UserSecurityContext;
import com.fixflow.service.ServiceRequestService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured // Requires authentication for all endpoints below
public class ServiceRequestResource {

    private final ServiceRequestService service = new ServiceRequestService();

    @GET
    public Response getAllRequests(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit,
            @QueryParam("status") String statusStr,
            @QueryParam("priority") String priorityStr,
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("location") String location,
            @QueryParam("search") String search,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortOrder") String sortOrder,
            @QueryParam("fromDate") String fromDate,
            @QueryParam("toDate") String toDate,
            @Context SecurityContext securityContext) {
            
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        Role authRole = getRoleFromContext(securityContext);
        
        com.fixflow.dto.RequestFilterDTO filter = new com.fixflow.dto.RequestFilterDTO();
        filter.setPage(page);
        filter.setLimit(limit);
        if (statusStr != null) {
            try { filter.setStatus(com.fixflow.model.RequestStatus.valueOf(statusStr.toUpperCase())); }
            catch (IllegalArgumentException e) { return Response.status(400).entity("{\"error\":\"Invalid status\"}").build(); }
        }
        if (priorityStr != null) {
            try { filter.setPriority(com.fixflow.model.Priority.valueOf(priorityStr.toUpperCase())); }
            catch (IllegalArgumentException e) { return Response.status(400).entity("{\"error\":\"Invalid priority\"}").build(); }
        }
        filter.setCategoryId(categoryId);
        filter.setLocation(location);
        filter.setSearch(search);
        filter.setSortBy(sortBy);
        filter.setSortOrder(sortOrder);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        
        com.fixflow.dto.PaginatedResponse<ServiceRequestResponse> response = service.getFilteredRequests(filter, authUserId, authRole);
        return Response.ok(response).build();
    }

    @GET
    @Path("/statistics")
    @Secured({Role.ADMIN})
    public Response getStatistics(@Context SecurityContext securityContext) {
        Role authRole = getRoleFromContext(securityContext);
        com.fixflow.dto.StatisticsDTO stats = service.getStatistics(authRole);
        return Response.ok(new com.fixflow.dto.DataResponse<>(stats)).build();
    }

    @GET
    @Path("/{id}")
    public Response getRequestById(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        Role authRole = getRoleFromContext(securityContext);
        
        ServiceRequestResponse response = service.getRequestById(id, authUserId, authRole);
        return Response.ok(new com.fixflow.dto.DataResponse<>(response)).build();
    }

    @POST
    @Secured({Role.USER, Role.ADMIN}) // Technician shouldn't create requests
    public Response createRequest(CreateServiceRequestRequest requestDto, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        
        ServiceRequestResponse response = service.createRequest(requestDto, authUserId);
        return Response.status(Response.Status.CREATED).entity(new com.fixflow.dto.DataResponse<>(response)).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateRequest(@PathParam("id") Integer id, UpdateServiceRequestRequest requestDto, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        Role authRole = getRoleFromContext(securityContext);
        
        ServiceRequestResponse response = service.updateRequest(id, requestDto, authUserId, authRole);
        return Response.ok(new com.fixflow.dto.DataResponse<>(response)).build();
    }

    @PATCH
    @Path("/{id}")
    public Response patchRequest(@PathParam("id") Integer id, PatchServiceRequestRequest requestDto, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        Role authRole = getRoleFromContext(securityContext);
        
        ServiceRequestResponse response = service.patchRequest(id, requestDto, authUserId, authRole);
        return Response.ok(new com.fixflow.dto.DataResponse<>(response)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRequest(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        Role authRole = getRoleFromContext(securityContext);
        
        service.deleteRequest(id, authUserId, authRole);
        return Response.noContent().build();
    }
    
    @PATCH
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") Integer id, com.fixflow.dto.UpdateStatusRequest requestDto, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        Role authRole = getRoleFromContext(securityContext);
        
        ServiceRequestResponse response = service.updateStatus(id, requestDto.getStatus(), authUserId, authRole);
        return Response.ok(new com.fixflow.dto.DataResponse<>(response)).build();
    }
    
    @GET
    @Path("/{id}/assignment")
    public Response getRequestAssignments(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        Role authRole = getRoleFromContext(securityContext);
        
        com.fixflow.service.RequestAssignmentService assignmentService = new com.fixflow.service.RequestAssignmentService();
        List<com.fixflow.dto.AssignmentResponse> assignments = assignmentService.getAssignmentsByRequestId(id, authUserId, authRole);
        return Response.ok(new com.fixflow.dto.DataResponse<>(assignments)).build();
    }
    
    @POST
    @Path("/{id}/assignment")
    @Secured({Role.ADMIN})
    public Response assignTechnicianToRequest(@PathParam("id") Integer id, com.fixflow.dto.AssignTechnicianRequest requestDto, @Context SecurityContext securityContext) {
        Integer authUserId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        
        com.fixflow.service.RequestAssignmentService assignmentService = new com.fixflow.service.RequestAssignmentService();
        com.fixflow.dto.AssignmentResponse response = assignmentService.assignTechnician(id, requestDto, authUserId);
        return Response.status(Response.Status.CREATED).entity(new com.fixflow.dto.DataResponse<>(response)).build();
    }
    
    private Role getRoleFromContext(SecurityContext securityContext) {
        for (Role role : Role.values()) {
            if (securityContext.isUserInRole(role.name())) {
                return role;
            }
        }
        return Role.USER; // fallback
    }
}
