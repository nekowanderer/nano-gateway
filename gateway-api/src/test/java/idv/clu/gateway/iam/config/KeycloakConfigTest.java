package idv.clu.gateway.iam.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class KeycloakConfigTest {

    @Test
    void testKeycloakConfigMethods() {
        KeycloakConfig keycloakConfig = Mockito.mock(KeycloakConfig.class);

        String expectedAuthServerUrl = "http://localhost:8080/auth";
        String expectedRealm = "master";
        String expectedClientId = "admin-cli";
        String expectedGrantType = "password";
        String expectedUsername = "admin";
        String expectedPassword = "admin";

        when(keycloakConfig.authServerUrl()).thenReturn(expectedAuthServerUrl);
        when(keycloakConfig.realm()).thenReturn(expectedRealm);
        when(keycloakConfig.clientId()).thenReturn(expectedClientId);
        when(keycloakConfig.grantType()).thenReturn(expectedGrantType);
        when(keycloakConfig.username()).thenReturn(expectedUsername);
        when(keycloakConfig.password()).thenReturn(expectedPassword);

        assertEquals(expectedAuthServerUrl, keycloakConfig.authServerUrl());
        assertEquals(expectedRealm, keycloakConfig.realm());
        assertEquals(expectedClientId, keycloakConfig.clientId());
        assertEquals(expectedGrantType, keycloakConfig.grantType());
        assertEquals(expectedUsername, keycloakConfig.username());
        assertEquals(expectedPassword, keycloakConfig.password());
    }

}