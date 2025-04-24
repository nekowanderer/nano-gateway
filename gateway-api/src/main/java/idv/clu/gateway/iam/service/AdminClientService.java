package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.dto.UserDTO;
import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import idv.clu.gateway.iam.exception.RealmNotFoundException;
import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Optional;

/**
 * @author clu
 */
@ApplicationScoped
public class AdminClientService {

    private final Keycloak keycloak;

    @Inject
    public AdminClientService(@Named("masterRealmClient") Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void createRealm(final String realmId, final String realmName) {
        boolean realmExists = keycloak.realms()
                .findAll()
                .stream()
                .anyMatch(realm -> realm.getId().equals(realmId));

        if (realmExists) {
            throw new RealmAlreadyExistsException(realmId);
        }

        final RealmRepresentation newRealm = new RealmRepresentation();
        newRealm.setId(realmId);
        newRealm.setRealm(realmName);
        newRealm.setEnabled(true);

        keycloak.realms().create(newRealm);
    }

    public void deleteRealm(final String realmId) {
        keycloak.realm(getRealmById(realmId).getRealm()).remove();
    }

    public RealmRepresentation getRealmById(final String realmId) {
        Optional<RealmRepresentation> realm = keycloak.realms()
                .findAll()
                .stream()
                .filter(realmRepresentation -> realmRepresentation.getId().equals(realmId))
                .findFirst();

        if (realm.isPresent()) {
            return realm.get();
        } else {
            throw new RealmNotFoundException(realmId);
        }
    }

    public RealmRepresentation getRealmByName(final String realmName) {
        Optional<RealmRepresentation> realm = keycloak.realms()
                .findAll()
                .stream()
                .filter(realmRepresentation -> realmRepresentation.getRealm().equals(realmName))
                .findFirst();

        if (realm.isPresent()) {
            return realm.get();
        } else {
            throw new RealmNotFoundException(realmName);
        }
    }

    public void createUser(final String realmName, final UserRepresentation userRepresentation) {
        final UsersResource usersResource = keycloak.realm(realmName).users();

        try (Response response = usersResource.create(userRepresentation)) {
            if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                throw new RuntimeException("User already exists on realm: " + realmName);
            } else if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new RuntimeException("Failed to create user on realm: " + realmName);
            }
        }
    }

    public void deleteUserOnRealm(final String realmName, final String username) {
        final UsersResource usersResource = keycloak.realm(realmName).users();
        final String userId = usersResource.searchByUsername(username, true).stream().findFirst().orElseThrow().getId();
        final UserResource userResource = usersResource.get(userId);
        userResource.remove();
    }

    public List<UserRepresentation> listUsers(final String targetRealm) {
        final List<UserRepresentation> users = keycloak.realm(targetRealm).users().list();

        if (users == null || users.isEmpty()) {
            throw new UserNotFoundException(targetRealm);
        }

        return users;
    }

}
