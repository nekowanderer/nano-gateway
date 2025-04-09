package idv.clu.gateway.iam.client;

import idv.clu.gateway.iam.config.KeycloakConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

/**
 * @author clu
 */
@ApplicationScoped
public class KeycloakAdminClientProducer {

    private final KeycloakConfig keycloakConfig;

    @Inject
    public KeycloakAdminClientProducer(KeycloakConfig keycloakConfig) {
        this.keycloakConfig = keycloakConfig;
    }

    @Produces
    @ApplicationScoped
    @Named("masterRealmClient")
    public Keycloak produceKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakConfig.authServerUrl())
                .realm(keycloakConfig.realm())
                .clientId(keycloakConfig.clientId())
                .grantType(keycloakConfig.grantType())
                .username(keycloakConfig.username())
                .password(keycloakConfig.password())
                .build();
    }

}
