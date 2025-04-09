package idv.clu.gateway.iam.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * @author clu
 */
@ConfigMapping(prefix = "iam.keycloak")
public interface KeycloakConfig {

    @WithName("auth-server-url")
    String authServerUrl();

    String realm();

    @WithName("client-id")
    String clientId();

    @WithName("grant-type")
    String grantType();

    String username();

    String password();

}
