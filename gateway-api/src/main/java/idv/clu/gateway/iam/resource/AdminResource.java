package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.RealmDTO;
import idv.clu.gateway.iam.service.AdminClientService;
import idv.clu.gateway.iam.transformer.KeycloakRepresentationTransformer;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author clu
 */
@Path("/iam/admin/realms")
public class AdminResource {

    private final AdminClientService adminClientService;

    @Inject
    public AdminResource(AdminClientService adminClientService) {
        this.adminClientService = adminClientService;
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchRealmByName(@QueryParam("name") String realmName) {
        return Response.ok()
                .entity(KeycloakRepresentationTransformer
                        .toRealmDTO(adminClientService.getRealmByName(realmName)))
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRealm(@RequestBody RealmDTO realmDTO) {
        adminClientService.createRealm(realmDTO.realmId(), realmDTO.realmName());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("Realm: '%s' created successfully.", realmDTO.realmName()));

        return Response.status(Response.Status.CREATED)
                .entity(responseBody)
                .build();
    }

    @DELETE
    @Path("/{realmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRealm(@PathParam("realmId") String realmId) {
        adminClientService.deleteRealm(realmId);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("Realm with id: '%s' deleted successfully", realmId));

        return Response.status(Response.Status.OK).entity(responseBody).build();
    }

    // TODO User CRUD can be move to a dedicated class
    // TODO Group CRUD (create another class) + put user to group + remove user from group
    // TODO Maybe extract all search functionalities to other classes

}
