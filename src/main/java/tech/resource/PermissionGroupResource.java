package tech.resource;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.service.PermissionGroupService;

import java.util.List;

@Path("/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionGroupResource {

    @Inject
    PermissionGroupService service;

    @GET
    public Response listAll() {
        return Response.ok(service.listActive()).build();
    }

    @GET
    @Path("/{groupName}")
    public Response findByName(@PathParam("groupName") String groupName) {
        return Response.ok(service.findByName(groupName)).build();
    }

    @POST
    @Path("/resolve")
    public Response resolve(@NotEmpty List<String> permissions) {
        return Response.ok(service.resolve(permissions)).build();
    }
}