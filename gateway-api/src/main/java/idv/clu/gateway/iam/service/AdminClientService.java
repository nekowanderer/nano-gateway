package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import idv.clu.gateway.iam.exception.RealmNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;

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
            throw new RealmNotFoundException(realmId, null);
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
            throw new RealmNotFoundException(null, realmName);
        }
    }

}
