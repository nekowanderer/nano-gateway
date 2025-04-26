package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.UserDTO;
import idv.clu.gateway.iam.service.AdminClientService;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author clu
 */
public class AdminUserResourceTest {

    @Mock
    private AdminClientService adminClientService;

    @Mock
    private AdminUserService adminUserService;

    private AdminUserResource adminUserResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminUserResource = new AdminUserResource(adminClientService, adminUserService);
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

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(testRealmName);

        when(adminClientService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminClientService.getRealmByName(testRealmName)).thenReturn(realmRepresentation);
        doNothing().when(adminUserService).createUser(anyString(), any(UserRepresentation.class));

        try (Response response = adminUserResource.createUser(testRealmId, testUserDTO)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
                    "Response status should be 201 CREATED");

            Map<String, String> responseBody = (Map<String, String>) response.getEntity();
            assertEquals(String.format("User: '%s' created successfully.", testUserDTO.username()),
                    responseBody.get("message"),
                    "Response message should indicate successful creation");

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

        when(adminClientService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminClientService.getRealmByName(testRealmName)).thenReturn(realmRepresentation);
        doNothing().when(adminUserService).deleteUser(anyString(), anyString());

        try (Response response = adminUserResource.deleteUserByUsername(testRealmId, testUsername)) {
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

        when(adminClientService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
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

        when(adminClientService.getRealmById(testRealmId)).thenReturn(realmRepresentation);
        when(adminUserService.listUsers(testRealmName)).thenReturn(Collections.emptyList());

        try (Response response = adminUserResource.listUsers(testRealmId)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                    "Response status should be 200 OK");
            assertEquals(Collections.emptyList(), response.getEntity(),
                    "Response entity should be an empty list");
        }
    }

}
