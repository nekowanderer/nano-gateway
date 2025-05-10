package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.UserDTO;
import idv.clu.gateway.iam.exception.UserNotFoundException;
import idv.clu.gateway.iam.service.AdminRealmService;
import idv.clu.gateway.iam.service.AdminUserService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author clu
 */
public class AdminUserResourceTest {

    @Mock
    private AdminRealmService adminRealmService;

    @Mock
    private AdminUserService adminUserService;

    private AdminUserResource adminUserResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminUserResource = new AdminUserResource(adminRealmService, adminUserService);
    }

    @Test
    void testCreateUserSuccess() {
        String testRealmId = "test-realm-id";
        String testRealmName = "test-realm";
        UserDTO testUserDTO = new UserDTO(
                "testuser",
                "password123",
                "Test",
                "User",
                "test.user@example.com"
        );
        String createdUserId = "5566-7788";

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminRealmService.getRealmByName(testRealmName)).thenReturn(realmRepresentation);
        when(adminUserService.createUser(anyString(), any(UserRepresentation.class))).thenReturn(createdUserId);

        try (Response response = adminUserResource.createUser(testRealmId, testUserDTO)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
                    "Response status should be 201 CREATED");

            Map<String, String> responseBody = (Map<String, String>) response.getEntity();
            assertEquals(String.format("User: '%s' created successfully.", testUserDTO.username()),
                    responseBody.get("message"),
                    "Response message should indicate successful creation");
            assertEquals(createdUserId, responseBody.get("userId"), "Response should contain created user id");

            verify(adminUserService).createUser(anyString(), any(UserRepresentation.class));
        }
    }

    @Test
    void testDeleteUserByUsernameSuccess() {
        String testRealmId = "test-realm-id";
        String testRealmName = "test-realm";
        String testUsername = "testuser";

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminRealmService.getRealmByName(testRealmName)).thenReturn(realmRepresentation);
        doNothing().when(adminUserService).deleteUser(anyString(), anyString());

        try (Response response = adminUserResource.deleteUserByUserId(testRealmId, testUsername)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                    "Response status should be 200 OK");

            Map<String, String> responseBody = (Map<String, String>) response.getEntity();
            assertEquals(String.format("User with id: '%s' deleted successfully", testUsername),
                    responseBody.get("message"),
                    "Response message should indicate successful deletion");

            verify(adminUserService).deleteUser(anyString(), eq(testUsername));
        }
    }

    @Test
    void testGetUserByIdSuccess() {
        String testRealmId = "test-realm-id";
        String testRealmName = "test-realm";
        String testUserId = "test-user-id";

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(testUserId);
        userRepresentation.setUsername("testuser");

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminUserService.getUserById(testRealmName, testUserId)).thenReturn(userRepresentation);

        try (Response response = adminUserResource.getUserById(testRealmId, testUserId)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                    "Response status should be 200 OK");

            UserRepresentation actualUser = (UserRepresentation) response.getEntity();
            assertEquals(userRepresentation, actualUser, "Returned user should match the expected user");

            verify(adminRealmService).getRealmById(testRealmId);
            verify(adminUserService).getUserById(testRealmName, testUserId);
        }
    }

    @Test
    void testGetUserByIdUserNotFound() {
        String testRealmId = "test-realm-id";
        String testRealmName = "test-realm";
        String testUserId = "non-existent-id";

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminUserService.getUserById(testRealmName, testUserId)).thenThrow(new UserNotFoundException(testRealmName, testUserId, null));

        assertThrows(UserNotFoundException.class, () -> adminUserResource.getUserById(testRealmId, testUserId));

        verify(adminRealmService).getRealmById(testRealmId);
        verify(adminUserService).getUserById(testRealmName, testUserId);
    }

    @Test
    void testGetUserByUsernameSuccess() {
        String testRealmId = "test-realm-id";
        String testRealmName = "test-realm";
        String testUsername = "testuser";

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(testUsername);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminUserService.getUserByUsername(testRealmName, testUsername)).thenReturn(userRepresentation);

        try (Response response = adminUserResource.getUserByUsername(testRealmId, testUsername)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                    "Response status should be 200 OK");

            Object actualEntity = response.getEntity();
            assertNotNull(actualEntity, "Response entity should not be null");
            verify(adminRealmService).getRealmById(testRealmId);
            verify(adminUserService).getUserByUsername(testRealmName, testUsername);
        }
    }

    @Test
    void testGetUserByUsernameNotFound() {
        String testRealmId = "test-realm-id";
        String testRealmName = "test-realm";
        String testUsername = "nonexistent";

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminUserService.getUserByUsername(testRealmName, testUsername))
                .thenThrow(new UserNotFoundException(testRealmName, null, testUsername));

        assertThrows(UserNotFoundException.class, () -> adminUserResource.getUserByUsername(testRealmId, testUsername));

        verify(adminRealmService).getRealmById(testRealmId);
        verify(adminUserService).getUserByUsername(testRealmName, testUsername);
    }

    @Test
    void testGetUserByUsernameBadRequest() {
        String testRealmId = "test-realm-id";

        try (Response response = adminUserResource.getUserByUsername(testRealmId, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(),
                    "Response status should be 400 BAD_REQUEST");

            verify(adminRealmService, never()).getRealmById(anyString());
            verify(adminUserService, never()).getUserByUsername(anyString(), anyString());
        }
    }

    @Test
    void testListUsersSuccess() {
        String testRealmId = "test-realm-id";
        String testRealmName = "test-realm";
        List<UserRepresentation> expectedUsers = new ArrayList<>();
        UserRepresentation user1 = new UserRepresentation();
        user1.setId("user1-id");
        user1.setUsername("user1");
        expectedUsers.add(user1);

        org.keycloak.representations.idm.RealmRepresentation realmRepresentation = new org.keycloak.representations.idm.RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminUserService.listUsers(testRealmName)).thenReturn(expectedUsers);

        try (Response response = adminUserResource.listUsers(testRealmId)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                    "Response status should be 200 OK");
            assertSame(expectedUsers, response.getEntity(),
                    "Response entity should be the same list of users returned by the service");
        }
    }

    @Test
    void testListUsersReturnsEmptyList() {
        String testRealmId = "test-realm-id";
        String testRealmName = "test-realm";

        org.keycloak.representations.idm.RealmRepresentation realmRepresentation = new org.keycloak.representations.idm.RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminUserService.listUsers(testRealmName)).thenReturn(Collections.emptyList());

        try (Response response = adminUserResource.listUsers(testRealmId)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                    "Response status should be 200 OK");
            assertEquals(Collections.emptyList(), response.getEntity(),
                    "Response entity should be an empty list");
        }
    }

}
