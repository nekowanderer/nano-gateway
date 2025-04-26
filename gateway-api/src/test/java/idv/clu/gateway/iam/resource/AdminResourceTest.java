package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.RealmDTO;
import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import idv.clu.gateway.iam.exception.RealmNotFoundException;
import idv.clu.gateway.iam.service.AdminClientService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminResourceTest {

    @Mock
    private AdminClientService adminClientService;

    private AdminResource adminResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminResource = new AdminResource(adminClientService);
    }

    @Test
    void testCreateRealmSuccess() {
        String realmId = "test-realm-id";
        String realmName = "Test Realm";
        RealmDTO realmDTO = new RealmDTO(realmId, realmName, true);

        doNothing().when(adminClientService).createRealm(realmId, realmName);

        Response response = adminResource.createRealm(realmDTO);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(), 
                "Response status should be 201 CREATED");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(Map.class, entity, "Response entity should be a Map");

        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;

        assertTrue(entityMap.containsKey("message"), "Response should contain a message");
        assertTrue(entityMap.get("message").contains(realmName), "Message should contain the realm name");

        verify(adminClientService).createRealm(realmId, realmName);
    }

    @Test
    void testCreateRealmThrowsRealmAlreadyExistsException() {
        String realmId = "existing-realm";
        String realmName = "Existing Realm";
        RealmDTO realmDTO = new RealmDTO(realmId, realmName, true);

        RealmAlreadyExistsException expectedException = new RealmAlreadyExistsException(realmId);
        doThrow(expectedException).when(adminClientService).createRealm(realmId, realmName);

        assertThrows(RealmAlreadyExistsException.class, () -> adminResource.createRealm(realmDTO),
                "Should throw RealmAlreadyExistsException");

        verify(adminClientService).createRealm(realmId, realmName);
    }

    @Test
    void testDeleteRealmSuccess() {
        String realmId = "test-realm-id";

        doNothing().when(adminClientService).deleteRealm(realmId);

        Response response = adminResource.deleteRealm(realmId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), 
                "Response status should be 200 OK");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(Map.class, entity, "Response entity should be a Map");

        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;

        assertTrue(entityMap.containsKey("message"), "Response should contain a message");
        assertTrue(entityMap.get("message").contains(realmId), "Message should contain the realm ID");

        verify(adminClientService).deleteRealm(realmId);
    }

    @Test
    void testDeleteRealmThrowsRealmNotFoundException() {
        String realmId = "non-existent-realm";
        String realmName = "non-existent-realm-name";

        RealmNotFoundException expectedException = new RealmNotFoundException(realmId, realmName);
        doThrow(expectedException).when(adminClientService).deleteRealm(realmId);

        assertThrows(RealmNotFoundException.class, () -> adminResource.deleteRealm(realmId),
                "Should throw RealmNotFoundException");

        verify(adminClientService).deleteRealm(realmId);
    }

}
