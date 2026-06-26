package tech.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.dto.ConsentResponse;
import tech.dto.CreateConsentRequest;
import tech.dto.RevokeConsentRequest;
import tech.service.ConsentService;

import java.util.List;

@Path("/open-banking/consents/v2/consents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsentResource {

    @Inject
    ConsentService consentService;

    @Inject
    JsonWebToken jwt;

    @POST
    public Response create(@Valid CreateConsentRequest request) {
        var clientId = resolveClientId();
        var response = consentService.create(clientId, request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{consentId}")
    public ConsentResponse getById(@PathParam("consentId") String consentId) {
        return consentService.findByConsentId(consentId);
    }

    @GET
    public List<ConsentResponse> listByCpf(@QueryParam("cpf") String cpf) {
        var clientId = resolveClientId();
        return consentService.listByClientAndCpf(clientId, cpf);
    }

    @POST
    @Path("/{consentId}/authorise")
    public ConsentResponse authorise(@PathParam("consentId") String consentId) {
        return consentService.authorise(consentId);
    }

    @DELETE
    @Path("/{consentId}")
    public ConsentResponse revoke(
            @PathParam("consentId") String consentId,
            @Valid RevokeConsentRequest request
    ) {
        return consentService.revoke(consentId, request);
    }

    private String resolveClientId() {
        if (jwt != null && jwt.getSubject() != null) {
            return jwt.getSubject();
        }
        return "dev-client";
    }
}