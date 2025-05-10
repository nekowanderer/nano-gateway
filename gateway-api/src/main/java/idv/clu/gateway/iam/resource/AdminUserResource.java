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
 * REST resource for managing users within administrative realms.
 * This class provides endpoints for creating, retrieving, listing,
 * and deleting users within a specific realm. It interacts with
 * {@code AdminRealmService} and {@code AdminUserService} to manage realm
 * and user-related operations.
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

    /**
     * Retrieves a user by their unique identifier within a specific realm.
     *
     * @param realmId the ID of the realm to which the user belongs
     * @param userId the unique identifier of the user to retrieve
     * @return a {@code Response} object containing the user's details if found,
     *         or a corresponding error response if the user or realm does not exist
     */
    @GET
    @Path("/{realmId}/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("realmId") String realmId, @PathParam("userId") String userId) {
        return Response.status(Response.Status.OK)
                .entity(adminUserService.getUserById(adminRealmService.getRealmById(realmId).getRealm(), userId))
                .build();
    }

    /**
     * Retrieves a user by their username within a specified realm.
     *
     * @param realmId the ID of the realm where the user is being searched
     * @param username the username of the user to retrieve
     * @return a {@code Response} object containing the user's details
     *         if found, or an appropriate error message otherwise
     */
    @GET
    @Path("/{realmId}/users/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByUsername(@PathParam("realmId") String realmId, @PathParam("username") String username) {
        if (username != null) {
            final UserRepresentation userRepresentation =
                    adminUserService.getUserByUsername(adminRealmService.getRealmById(realmId).getRealm(), username);
            return Response.status(Response.Status.OK)
                    .entity(KeycloakRepresentationTransformer.toUserDTO(userRepresentation))
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing required query param: username").build();
        }
    }

    /**
     * Retrieves a list of users within a specified realm.
     *
     * @param realmId the ID of the realm from which to retrieve the users
     * @return a {@code Response} containing the list of users in the specified realm
     */
    @GET
    @Path("/{realmId}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@PathParam("realmId") String realmId) {
        final String realmName = adminRealmService.getRealmById(realmId).getRealm();
        List<UserRepresentation> users = adminUserService.listUsers(realmName);
        return Response.ok(users).build();
    }

    /**
     * Creates a new user within the specified realm.
     *
     * @param realmId the ID of the realm where the user will be created
     * @param userDTO the user data transfer object containing user details
     *                such as username, password, first name, last name, and email
     * @return a {@code Response} object containing a success message
     *         and the ID of the newly created user if successful,
     *         or an appropriate error response in case of failure
     */
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

    /**
     * Deletes a user by their unique identifier within a specified realm.
     *
     * @param realmId the ID of the realm to which the user belongs
     * @param userId the unique identifier of the user to delete
     * @return a {@code Response} object containing a success message if the deletion is successful,
     *         or an error response if the realm or user does not exist
     */
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

}
