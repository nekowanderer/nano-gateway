package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.RealmDTO;
import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import idv.clu.gateway.iam.exception.RealmNotFoundException;
import idv.clu.gateway.iam.service.AdminRealmService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminRealmResourceTest {

    @Mock
    private AdminRealmService adminRealmService;

    private AdminRealmResource adminRealmResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminRealmResource = new AdminRealmResource(adminRealmService);
    }

    @Test
    void testCreateRealmSuccess() {
        String realmId = "test-realm-id";
        String realmName = "Test Realm";
        RealmDTO realmDTO = new RealmDTO(realmId, realmName, true);

        doNothing().when(adminRealmService).createRealm(realmId, realmName);

        Response response = adminRealmResource.createRealm(realmDTO);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(), 
                "Response status should be 201 CREATED");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(Map.class, entity, "Response entity should be a Map");

        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;

        assertTrue(entityMap.containsKey("message"), "Response should contain a message");
        assertTrue(entityMap.get("message").contains(realmName), "Message should contain the realm name");

        verify(adminRealmService).createRealm(realmId, realmName);
    }

    @Test
    void testCreateRealmThrowsRealmAlreadyExistsException() {
        String realmId = "existing-realm";
        String realmName = "Existing Realm";
        RealmDTO realmDTO = new RealmDTO(realmId, realmName, true);

        RealmAlreadyExistsException expectedException = new RealmAlreadyExistsException(realmId);
        doThrow(expectedException).when(adminRealmService).createRealm(realmId, realmName);

        assertThrows(RealmAlreadyExistsException.class, () -> adminRealmResource.createRealm(realmDTO),
                "Should throw RealmAlreadyExistsException");

        verify(adminRealmService).createRealm(realmId, realmName);
    }

    @Test
    void testDeleteRealmSuccess() {
        String realmId = "test-realm-id";

        doNothing().when(adminRealmService).deleteRealm(realmId);

        Response response = adminRealmResource.deleteRealm(realmId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), 
                "Response status should be 200 OK");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(Map.class, entity, "Response entity should be a Map");

        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;

        assertTrue(entityMap.containsKey("message"), "Response should contain a message");
        assertTrue(entityMap.get("message").contains(realmId), "Message should contain the realm ID");

        verify(adminRealmService).deleteRealm(realmId);
    }

    @Test
    void testDeleteRealmThrowsRealmNotFoundException() {
        String realmId = "non-existent-realm";
        String realmName = "non-existent-realm-name";

        RealmNotFoundException expectedException = new RealmNotFoundException(realmId, realmName);
        doThrow(expectedException).when(adminRealmService).deleteRealm(realmId);

        assertThrows(RealmNotFoundException.class, () -> adminRealmResource.deleteRealm(realmId),
                "Should throw RealmNotFoundException");

        verify(adminRealmService).deleteRealm(realmId);
    }

    @Test
    void testSearchRealmByNameSuccess() {
        String realmId = "test-realm-id";
        String realmName = "Test Realm";
        boolean isEnabled = true;

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setId(realmId);
        realmRepresentation.setRealm(realmName);
        realmRepresentation.setEnabled(isEnabled);

        when(adminRealmService.getRealmByName(realmName)).thenReturn(realmRepresentation);

        Response response = adminRealmResource.searchRealmByName(realmName);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                "Response status should be 200 OK");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(RealmDTO.class, entity, "Response entity should be a RealmDTO");

        RealmDTO realmDTO = (RealmDTO) entity;
        assertEquals(realmId, realmDTO.realmId(), "RealmDTO realmId should match");
        assertEquals(realmName, realmDTO.realmName(), "RealmDTO realmName should match");
        assertEquals(isEnabled, realmDTO.isEnabled(), "RealmDTO isEnabled should match");

        verify(adminRealmService).getRealmByName(realmName);
    }

    @Test
    void testSearchRealmByNameThrowsRealmNotFoundException() {
        String realmName = "non-existent-realm";

        RealmNotFoundException expectedException = new RealmNotFoundException(null, realmName);
        when(adminRealmService.getRealmByName(realmName)).thenThrow(expectedException);

        assertThrows(RealmNotFoundException.class, () -> adminRealmResource.searchRealmByName(realmName),
                "Should throw RealmNotFoundException");

        verify(adminRealmService).getRealmByName(realmName);
    }

}
