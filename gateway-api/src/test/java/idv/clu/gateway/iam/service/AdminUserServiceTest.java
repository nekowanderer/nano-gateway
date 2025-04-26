package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.UserAlreadyExistsException;
import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author clu
 */
public class AdminUserServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private RealmsResource realmsResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private Response response;

    private AdminUserService adminClientService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        adminClientService = new AdminUserService(keycloak);
        when(keycloak.realms()).thenReturn(realmsResource);
    }

    @Test
    void testListUsersSuccess() {
        String testRealm = "test-realm";
        List<UserRepresentation> expectedUsers = new ArrayList<>();
        UserRepresentation user1 = new UserRepresentation();
        user1.setId("user1-id");
        user1.setUsername("user1");
        expectedUsers.add(user1);

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(expectedUsers);

        List<UserRepresentation> actualUsers = adminClientService.listUsers(testRealm);

        assertEquals(expectedUsers, actualUsers, "The returned users should match the expected users");
    }

    @Test
    void testListUsersEmptyList() {
        String testRealm = "test-realm";

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(Collections.emptyList());

        List<UserRepresentation> actualUsers = adminClientService.listUsers(testRealm);

        assertEquals(Collections.emptyList(), actualUsers, "Should return empty list when no users are found");
    }

    @Test
    void testListUsersNullList() {
        String testRealm = "test-realm";

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(null);

        List<UserRepresentation> actualUsers = adminClientService.listUsers(testRealm);

        assertNull(actualUsers, "Should return null when users list is null");
    }

    @Test
    void testCreateUserSuccess() {
        String testRealm = "test-realm";
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("testUser");

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(userRepresentation)).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());

        assertDoesNotThrow(() -> adminClientService.createUser(testRealm, userRepresentation),
                "Should not throw exception when user creation is successful");

        verify(usersResource).create(userRepresentation);
        verify(response).close();
    }

    @Test
    void testCreateUserAlreadyExists() {
        String testRealm = "test-realm";
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("existingUser");

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(userRepresentation)).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.CONFLICT.getStatusCode());

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> adminClientService.createUser(testRealm, userRepresentation),
                "Should throw UserAlreadyExistsException when user already exists");

        assertEquals(testRealm, exception.getRealmName(), "Exception should contain correct realm name");
        assertEquals("existingUser", exception.getUsername(), "Exception should contain correct username");
        verify(usersResource).create(userRepresentation);
        verify(response).close();
    }

    @Test
    void testCreateUserOtherFailure() {
        String testRealm = "test-realm";
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("testUser");

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(userRepresentation)).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminClientService.createUser(testRealm, userRepresentation),
                "Should throw RuntimeException for non-CREATED, non-CONFLICT status");

        assertTrue(exception.getMessage().contains(testRealm), "Exception message should contain realm name");
        verify(usersResource).create(userRepresentation);
        verify(response).close();
    }

    @Test
    void testDeleteUserSuccess() {
        String testRealm = "test-realm";
        String username = "testUser";
        String userId = "user-id-123";

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId);
        userRepresentation.setUsername(username);
        List<UserRepresentation> foundUsers = Collections.singletonList(userRepresentation);

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(username, true)).thenReturn(foundUsers);
        when(usersResource.get(userId)).thenReturn(userResource);

        assertDoesNotThrow(() -> adminClientService.deleteUser(testRealm, username),
                "Should not throw exception when user deletion is successful");

        verify(usersResource).searchByUsername(username, true);
        verify(usersResource).get(userId);
        verify(userResource).remove();
    }

    @Test
    void testDeleteUserNotFound() {
        String testRealm = "test-realm";
        String username = "nonExistentUser";

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(username, true)).thenReturn(Collections.emptyList());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> adminClientService.deleteUser(testRealm, username),
                "Should throw UserNotFoundException when user does not exist");

        assertEquals(testRealm, exception.getRealmName(), "Exception should contain correct realm name");
        assertEquals(username, exception.getUsername(), "Exception should contain correct username");
        verify(usersResource).searchByUsername(username, true);
        verify(usersResource, never()).get(anyString());
        verify(userResource, never()).remove();
    }

}
