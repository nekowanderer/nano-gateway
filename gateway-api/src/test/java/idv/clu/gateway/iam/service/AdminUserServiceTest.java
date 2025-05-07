package idv.clu.gateway.iam.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import idv.clu.gateway.iam.exception.UserAlreadyExistsException;
import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.ws.rs.core.Response;

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
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(response.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(response.getLocation()).thenReturn(URI.create("mockUserID/5566-7788"));

        String expectedUserId = "5566-7788";
        assertEquals(expectedUserId, adminClientService.createUser(testRealm, userRepresentation));

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

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        doNothing().when(userResource).remove();

        assertDoesNotThrow(() -> adminClientService.deleteUser(testRealm, userId),
                "Should not throw exception when user deletion is successful");

        verify(usersResource).get(userId);
        verify(userResource).remove();
    }

    @Test
    void testDeleteUserNotFound() {
        String testRealm = "test-realm";
        String userId = "nonExistentUserId";

        when(keycloak.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> adminClientService.deleteUser(testRealm, userId),
                "Should throw UserNotFoundException when user does not exist");

        assertEquals(testRealm, exception.getRealmName(), "Exception should contain correct realm name");
        assertEquals(userId, exception.getUserId(), "Exception should contain correct username");
        verify(usersResource).get(userId);
        verify(userResource, never()).remove();
    }

}
