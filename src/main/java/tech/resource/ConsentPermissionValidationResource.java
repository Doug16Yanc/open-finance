package tech.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.service.ConsentPermissionValidationService;

@Path("/consents")
@Produces(MediaType.APPLICATION_JSON)
public class ConsentPermissionValidationResource {

    @Inject
    ConsentPermissionValidationService service;

    @GET
    @Path("/{consentId:.+}/permission-validation")
    public Response findByConsent(@PathParam("consentId") String consentId) {
        return Response.ok(service.findByConsentId(consentId)).build();
    }

    @GET
    @Path("/{consentId:.+}/permission-validation/approved")
    public Response isApproved(@PathParam("consentId") String consentId) {
        var approved = service.isApproved(consentId);
        return Response.ok(new ApprovalResponse(consentId, approved)).build();
    }

    public record ApprovalResponse(String consentId, boolean approved) {}
}