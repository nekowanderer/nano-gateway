package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.RealmDTO;
import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import idv.clu.gateway.iam.exception.RealmNotFoundException;
import idv.clu.gateway.iam.exception.UserNotFoundException;
import idv.clu.gateway.iam.service.AdminClientService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
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
        RealmDTO realmDTO = new RealmDTO();
        realmDTO.setRealmId(realmId);
        realmDTO.setRealmName(realmName);

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
        RealmDTO realmDTO = new RealmDTO();
        realmDTO.setRealmId(realmId);
        realmDTO.setRealmName(realmName);

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

        RealmNotFoundException expectedException = new RealmNotFoundException(realmId);
        doThrow(expectedException).when(adminClientService).deleteRealm(realmId);

        assertThrows(RealmNotFoundException.class, () -> adminResource.deleteRealm(realmId),
                "Should throw RealmNotFoundException");

        verify(adminClientService).deleteRealm(realmId);
    }

    @Test
    void testListUsersSuccess() {
        String testRealm = "test-realm";
        List<UserRepresentation> expectedUsers = new ArrayList<>();
        UserRepresentation user1 = new UserRepresentation();
        user1.setId("user1-id");
        user1.setUsername("user1");
        expectedUsers.add(user1);

        when(adminClientService.listUsers(testRealm)).thenReturn(expectedUsers);

        try (Response response = adminResource.listUsers(testRealm)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                    "Response status should be 200 OK");
            assertSame(expectedUsers, response.getEntity(),
                    "Response entity should be the same list of users returned by the service");
        }
    }

    @Test
    void testListUsersThrowsUserNotFoundException() {
        String testRealm = "test-realm";
        UserNotFoundException expectedException = new UserNotFoundException(testRealm);

        when(adminClientService.listUsers(testRealm)).thenThrow(expectedException);

        try (Response ignored = adminResource.listUsers(testRealm)) {
            org.junit.jupiter.api.Assertions.fail("Expected UserNotFoundException to be thrown, but it was not");
        } catch (UserNotFoundException thrown) {
            assertEquals(testRealm, thrown.getRealm(), "Exception should contain correct realm");
        }
    }

}
