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

    @GET
    @Path("/{realmId}/users/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByUsername(@PathParam("realmId") String realmId, @PathParam("username") String username) {
        final UserRepresentation userRepresentation =
                adminUserService.getUserByUsername(adminRealmService.getRealmById(realmId).getRealm(), username);

        return Response.status(Response.Status.OK)
                .entity(KeycloakRepresentationTransformer.toUserDTO(userRepresentation))
                .build();
    }

    // TODO get by user ID

    @POST
    @Path("/{realmId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(@PathParam("realmId") String realmId, @RequestBody UserDTO userDTO) {
        final String realmName = adminRealmService.getRealmById(realmId).getRealm();
        final String userId = adminUserService.createUser(adminRealmService.getRealmByName(realmName).getRealm(),
                KeycloakRepresentationTransformer.toUserRepresentation(userDTO));

        // Check how to return the created user elegantly.
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("User: '%s' created successfully.", userDTO.username()));
        responseBody.put("userId", userId);

        return Response.status(Response.Status.CREATED)
                .entity(responseBody)
                .build();
    }

    // TODO update user

    @DELETE
    @Path("/{realmId}/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserByUserId(@PathParam("realmId") String realmId, @PathParam("userId") String userId) {
        final String realmName = adminRealmService.getRealmById(realmId).getRealm();
        adminUserService.deleteUser(adminRealmService.getRealmByName(realmName).getRealm(), userId);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("User with id: '%s' deleted successfully", userId));

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
