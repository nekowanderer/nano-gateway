package idv.clu.gateway.iam.transformer;

import idv.clu.gateway.iam.dto.RealmDTO;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author clu
 */
class KeycloakRepresentationTransformerTest {

    @Test
    void testToRealmDTO() {
        String realmId = "test-realm-id";
        String realmName = "Test Realm";
        boolean isEnabled = true;

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setId(realmId);
        realmRepresentation.setRealm(realmName);
        realmRepresentation.setEnabled(isEnabled);

        RealmDTO realmDTO = KeycloakRepresentationTransformer.toRealmDTO(realmRepresentation);

        assertNotNull(realmDTO, "RealmDTO should not be null");
        assertEquals(realmId, realmDTO.realmId(), "RealmDTO realmId should match RealmRepresentation id");
        assertEquals(realmName, realmDTO.realmName(), "RealmDTO realmName should match RealmRepresentation realm");
        assertEquals(isEnabled, realmDTO.isEnabled(), "RealmDTO isEnabled should match RealmRepresentation enabled");
    }

}