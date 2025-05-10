package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.GroupNotFoundException;
import idv.clu.gateway.iam.exception.JoinGroupFailedException;
import idv.clu.gateway.iam.exception.LeaveGroupFailedException;
import idv.clu.gateway.iam.exception.UserAlreadyExistsException;
import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service class for managing and interacting with users in a Keycloak realm.
 * This class provides functionality to retrieve, create, and delete users,
 * as well as list all users within a specified realm. It uses the Keycloak
 * Admin API to perform operations.
 *
 * The service relies on a Keycloak client injected at runtime for
 * communication with the Keycloak server. It handles exceptions and provides
 * meaningful error messages when entities are not found or already exist.
 *
 * Key features supported:
 * - Retrieve user details by user ID or username.
 * - Create new users within a realm.
 * - Delete users from a realm.
 * - List all users within a specified realm.
 *
 * Intended to be used as an application-scoped CDI bean.
 */
@ApplicationScoped
public class AdminUserService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AdminUserService.class);

    private final Keycloak keycloak;

    @Inject
    public AdminUserService(final @Named("masterRealmClient") Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Retrieves a user by their unique identifier within a specified realm.
     *
     * @param realmName the name of the realm where the user is being searched
     * @param userId the unique identifier of the user to retrieve
     * @return the {@code UserRepresentation} object containing the user's details
     * @throws UserNotFoundException if no user with the specified identifier is found in the given realm
     * @throws RuntimeException if the retrieval process fails due to an unexpected error
     */
    public UserRepresentation getUserById(final String realmName, final String userId) {
        try {
            return keycloak.realm(realmName).users().get(userId).toRepresentation();
        } catch (Exception e) {
            if (e instanceof WebApplicationException webEx
                    && webEx.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                int status = webEx.getResponse().getStatus();
                LOGGER.warn("Get user failed: realm={}, userId={}, status={}", realmName, userId, status);
                throw new UserNotFoundException(realmName, userId, null);
            } else {
                throw new RuntimeException(String.format("Failed to get user by Id: %s", userId));
            }
        }
    }

    /**
     * Retrieves a user by their username within a specified realm.
     *
     * @param realmName the name of the realm where the user is being searched
     * @param username the username of the user to retrieve
     * @return the {@code UserRepresentation} object containing the user's details
     * @throws UserNotFoundException if no user with the specified username is found in the specified realm
     */
    public UserRepresentation getUserByUsername(final String realmName, final String username) {
        return keycloak
                .realm(realmName)
                .users()
                .searchByUsername(username, true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(realmName, null, username));
    }

    /**
     * Creates a new user in the specified realm.
     *
     * @param realmName the name of the realm where the user will be created
     * @param userRepresentation the {@code UserRepresentation} object containing
     *                            the user's details such as username, email, and other information
     * @return the ID of the newly created user as a {@code String}
     * @throws UserAlreadyExistsException if a user with the same username already exists in the specified realm
     * @throws RuntimeException if the user creation fails for other reasons
     */
    public String createUser(final String realmName, final UserRepresentation userRepresentation) {
        final UsersResource usersResource = keycloak.realm(realmName).users();

        try (Response response = usersResource.create(userRepresentation)) {
            if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                throw new UserAlreadyExistsException(realmName, userRepresentation.getUsername());
            } else if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new RuntimeException("Failed to create user on realm: " + realmName);
            }

            return CreatedResponseUtil.getCreatedId(response);
        }
    }

    /**
     * Assigns a user to a group within a specified realm.
     *
     * @param realmName the name of the realm where the group and user are located
     * @param groupId the unique identifier of the group to which the user will be assigned
     * @param userId the unique identifier of the user to assign to the group
     * @throws GroupNotFoundException if the specified group is not found in the given realm
     * @throws JoinGroupFailedException if the assignment of the user to the group fails for other reasons
     */
    public void assignUserToGroup(final String realmName, final String groupId, final String userId) {
        try {
            keycloak.realm(realmName).users().get(userId).joinGroup(groupId);
        } catch (Exception e) {
            if (e instanceof WebApplicationException webEx
                    && webEx.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                int status = webEx.getResponse().getStatus();
                LOGGER.warn("Add user to group failed: realm={}, groupId={}, userId={}, status={}",
                        realmName, groupId, userId, status);
                throw new GroupNotFoundException(realmName, groupId);
            } else {
                throw new JoinGroupFailedException(realmName, groupId, userId);
            }
        }
    }

    /**
     * Removes a user from a specific group within the given realm.
     *
     * @param realmName the name of the realm where the operation is performed
     * @param groupId the unique identifier of the group from which the user will be removed
     * @param userId the unique identifier of the user to be removed from the group
     * @throws GroupNotFoundException if the specified group is not found in the realm
     * @throws LeaveGroupFailedException if the operation to remove the user from the group fails
     */
    public void removeUserFromGroup(final String realmName, final String groupId, final String userId) {
        try {
            keycloak.realm(realmName).users().get(userId).leaveGroup(groupId);
        } catch (Exception e) {
            if (e instanceof WebApplicationException webEx
                    && webEx.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                int status = webEx.getResponse().getStatus();
                LOGGER.warn("Remove user from group failed: realm={}, groupId={}, userId={}, status={}",
                        realmName, groupId, userId, status);
                throw new GroupNotFoundException(realmName, groupId);
            } else {
                throw new LeaveGroupFailedException(realmName, groupId, userId);
            }
        }
    }

    /**
     * Deletes a user identified by their unique identifier within a specific realm.
     *
     * @param realmName the name of the realm from which the user will be deleted
     * @param userId the unique identifier of the user to delete
     * @throws UserNotFoundException if the user with the specified identifier does not exist in the given realm
     * @throws RuntimeException if the deletion process fails due to an unexpected error
     */
    public void deleteUser(final String realmName, final String userId) {
        try {
            keycloak.realm(realmName).users().get(userId).remove();
        } catch (Exception e) {
            if (e instanceof WebApplicationException webEx
                    && webEx.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                int status = webEx.getResponse().getStatus();
                LOGGER.warn("Delete user failed: realm={}, userId={}, status={}", realmName, userId, status);
                throw new UserNotFoundException(realmName, userId, null);
            } else {
                throw new RuntimeException("Failed to delete user: " + userId, e);
            }
        }
    }

    /**
     * Retrieves a list of users within the specified target realm.
     *
     * @param targetRealm the name of the realm from which to retrieve the users
     * @return a list of {@code UserRepresentation} objects representing the users in the specified realm
     */
    public List<UserRepresentation> listUsers(final String targetRealm) {
        return keycloak.realm(targetRealm).users().list();
    }

}
