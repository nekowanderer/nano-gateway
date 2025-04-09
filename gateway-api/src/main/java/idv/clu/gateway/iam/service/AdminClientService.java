package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

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

    public List<UserRepresentation> listUsers(String targetRealm) {
        List<UserRepresentation> users = keycloak.realm(targetRealm).users().list();

        if (users == null || users.isEmpty()) {
            throw new UserNotFoundException(targetRealm);
        }

        return users;
    }

}
