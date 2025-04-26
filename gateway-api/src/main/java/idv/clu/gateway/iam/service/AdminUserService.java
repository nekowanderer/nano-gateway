package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.UserAlreadyExistsException;
import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * @author clu
 */
@ApplicationScoped
public class AdminUserService {

    private final Keycloak keycloak;

    @Inject
    public AdminUserService(@Named("masterRealmClient") Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void createUser(final String realmName, final UserRepresentation userRepresentation) {
        final UsersResource usersResource = keycloak.realm(realmName).users();

        try (Response response = usersResource.create(userRepresentation)) {
            if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                throw new UserAlreadyExistsException(realmName, userRepresentation.getUsername());
            } else if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new RuntimeException("Failed to create user on realm: " + realmName);
            }
        }
    }

    public void deleteUser(final String realmName, final String username) {
        final UsersResource usersResource = keycloak.realm(realmName).users();
        final String userId = usersResource
                .searchByUsername(username, true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(realmName, username))
                .getId();
        final UserResource userResource = usersResource.get(userId);
        userResource.remove();
    }

    public List<UserRepresentation> listUsers(final String targetRealm) {
        return keycloak.realm(targetRealm).users().list();
    }

}
