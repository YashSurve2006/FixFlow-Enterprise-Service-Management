package com.fixflow.resource;

import com.fixflow.dto.DataResponse;
import com.fixflow.dto.ServiceCategoryRequest;
import com.fixflow.model.Role;
import com.fixflow.model.ServiceCategory;
import com.fixflow.security.Secured;
import com.fixflow.service.ServiceCategoryService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceCategoryResource {

    private final ServiceCategoryService service = new ServiceCategoryService();

    @GET
    @Secured({Role.USER, Role.TECHNICIAN, Role.ADMIN})
    public Response getAllCategories() {
        List<ServiceCategory> categories = service.getAllCategories();
        return Response.ok(new DataResponse<>(categories)).build();
    }

    @GET
    @Path("/{id}")
    @Secured({Role.USER, Role.TECHNICIAN, Role.ADMIN})
    public Response getCategoryById(@PathParam("id") Integer id) {
        ServiceCategory category = service.getCategoryById(id);
        return Response.ok(new DataResponse<>(category)).build();
    }

    @POST
    @Secured({Role.ADMIN})
    public Response createCategory(ServiceCategoryRequest requestDto) {
        ServiceCategory category = service.createCategory(requestDto);
        return Response.status(Response.Status.CREATED).entity(new DataResponse<>(category)).build();
    }

    @PUT
    @Path("/{id}")
    @Secured({Role.ADMIN})
    public Response updateCategory(@PathParam("id") Integer id, ServiceCategoryRequest requestDto) {
        ServiceCategory category = service.updateCategory(id, requestDto);
        return Response.ok(new DataResponse<>(category)).build();
    }

    @PATCH
    @Path("/{id}")
    @Secured({Role.ADMIN})
    public Response patchCategory(@PathParam("id") Integer id, ServiceCategoryRequest requestDto) {
        ServiceCategory category = service.patchCategory(id, requestDto);
        return Response.ok(new DataResponse<>(category)).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured({Role.ADMIN})
    public Response deleteCategory(@PathParam("id") Integer id) {
        service.deleteCategory(id);
        return Response.noContent().build();
    }
}
