package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.RealmDTO;
import idv.clu.gateway.iam.dto.UserDTO;
import idv.clu.gateway.iam.service.AdminClientService;
import idv.clu.gateway.iam.transformer.KeycloakRepresentationTransformer;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.HashMap;
import java.util.List;
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRealm(@RequestBody RealmDTO realmDTO) {
        adminClientService.createRealm(realmDTO.getRealmId(), realmDTO.getRealmName());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("Realm: '%s' created successfully.", realmDTO.getRealmName()));

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

    @POST
    @Path("/{realmId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(@PathParam("realmId") String realmId, @RequestBody UserDTO userDTO) {
        final String realmName = adminClientService.getRealmById(realmId).getRealm();
        adminClientService.createUser(realmName, KeycloakRepresentationTransformer.toUserRepresentation(userDTO));

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("User: '%s' created successfully.", userDTO.getUsername()));

        return Response.status(Response.Status.CREATED)
                .entity(responseBody)
                .build();
    }

    @DELETE
    @Path("/{realmId}/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("realmId") String realmId, @PathParam("userId") String userId) {
        final String realmName = adminClientService.getRealmById(realmId).getRealm();
        adminClientService.deleteUserOnRealm(realmName, userId);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("User with id: '%s' deleted successfully", userId));

        return Response.status(Response.Status.OK)
                .entity(responseBody)
                .build();
    }

    @GET
    @Path("/{realm}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@PathParam("realm") String realm) {
        List<UserRepresentation> users = adminClientService.listUsers(realm);
        return Response.ok(users).build();
    }

}
