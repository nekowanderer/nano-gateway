package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import idv.clu.gateway.iam.exception.RealmNotFoundException;
import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.keycloak.admin.client.Keycloak;
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
        Optional<RealmRepresentation> realmToDelete = keycloak.realms()
                .findAll()
                .stream()
                .filter(realm -> realm.getId().equals(realmId))
                        .findFirst();

        if (realmToDelete.isPresent()) {
            String realmName = realmToDelete.get().getRealm();
            keycloak.realm(realmName).remove();
        } else {
            throw new RealmNotFoundException(realmId);
        }
    }

    public List<UserRepresentation> listUsers(final String targetRealm) {
        final List<UserRepresentation> users = keycloak.realm(targetRealm).users().list();

        if (users == null || users.isEmpty()) {
            throw new UserNotFoundException(targetRealm);
        }

        return users;
    }

}
