package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.UserDTO;
import idv.clu.gateway.iam.service.AdminRealmService;
import idv.clu.gateway.iam.service.AdminUserService;
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
public class AdminUserResource {

    private final AdminRealmService adminRealmService;
    private final AdminUserService adminUserService;

    @Inject
    public AdminUserResource(final AdminRealmService adminRealmService, final AdminUserService adminUserService) {
        this.adminRealmService = adminRealmService;
        this.adminUserService = adminUserService;
    }

    // TODO get user

    @POST
    @Path("/{realmId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(@PathParam("realmId") String realmId, @RequestBody UserDTO userDTO) {
        final String realmName = adminRealmService.getRealmById(realmId).getRealm();
        adminUserService.createUser(adminRealmService.getRealmByName(realmName).getRealm(),
                KeycloakRepresentationTransformer.toUserRepresentation(userDTO));

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("User: '%s' created successfully.", userDTO.username()));

        return Response.status(Response.Status.CREATED)
                .entity(responseBody)
                .build();
    }

    // TODO update user

    @DELETE
    @Path("/{realmId}/users/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserByUsername(@PathParam("realmId") String realmId, @PathParam("username") String username) {
        final String realmName = adminRealmService.getRealmById(realmId).getRealm();
        adminUserService.deleteUser(adminRealmService.getRealmByName(realmName).getRealm(), username);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("User with id: '%s' deleted successfully", username));

        return Response.status(Response.Status.OK)
                .entity(responseBody)
                .build();
    }

    /**
     * TODO implement pagination
     * @param realmId realm ID
     * @return user list
     */
    @GET
    @Path("/{realmId}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@PathParam("realmId") String realmId) {
        final String realmName = adminRealmService.getRealmById(realmId).getRealm();
        List<UserRepresentation> users = adminUserService.listUsers(realmName);
        return Response.ok(users).build();
    }

}
