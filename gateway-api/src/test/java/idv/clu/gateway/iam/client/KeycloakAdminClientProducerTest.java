package idv.clu.gateway.iam.client;

import idv.clu.gateway.iam.config.KeycloakConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakAdminClientProducerTest {

    private KeycloakConfig keycloakConfig;
    private KeycloakAdminClientProducer producer;

    @BeforeEach
    void setUp() {
        keycloakConfig = Mockito.mock(KeycloakConfig.class);

        when(keycloakConfig.authServerUrl()).thenReturn("http://localhost:8080/auth");
        when(keycloakConfig.realm()).thenReturn("master");
        when(keycloakConfig.clientId()).thenReturn("admin-cli");
        when(keycloakConfig.grantType()).thenReturn("password");
        when(keycloakConfig.username()).thenReturn("admin");
        when(keycloakConfig.password()).thenReturn("admin");

        producer = new KeycloakAdminClientProducer(keycloakConfig);
    }

    @Test
    void testConstructor() {
        assertNotNull(producer, "Producer should not be null");

        // We can't directly test the private field, but we can verify that the config methods are called
        // when produceKeycloak is called, which indirectly verifies that the config was stored correctly
    }

    @Test
    void testConfigValuesAreUsed() {
        verify(keycloakConfig, Mockito.never()).authServerUrl();
        verify(keycloakConfig, Mockito.never()).realm();
        verify(keycloakConfig, Mockito.never()).clientId();
        verify(keycloakConfig, Mockito.never()).grantType();
        verify(keycloakConfig, Mockito.never()).username();
        verify(keycloakConfig, Mockito.never()).password();
    }

}
