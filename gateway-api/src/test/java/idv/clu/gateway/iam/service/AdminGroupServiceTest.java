package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.GroupAlreadyExistsException;
import idv.clu.gateway.iam.exception.GroupNotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author clu
 */
class AdminGroupServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private GroupsResource groupsResource;

    @Mock
    private GroupResource groupResource;

    private AdminGroupService adminGroupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminGroupService = new AdminGroupService(keycloak);
        when(keycloak.realm(any())).thenReturn(realmResource);
        when(realmResource.groups()).thenReturn(groupsResource);
    }

    @Test
    void testCreateGroupSuccess() {
        String realmName = "test-realm";
        String groupName = "Developers";

        Response mockResponse = mock(Response.class);
        when(groupsResource.add(any(GroupRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockResponse.getLocation()).thenReturn(java.net.URI.create("http://localhost/groups/123"));

        String createdId = adminGroupService.createGroup(realmName, groupName);

        assertEquals("123", createdId, "Created group ID should match");
        verify(groupsResource).add(argThat(g -> groupName.equals(g.getName())));
    }

    @Test
    void testCreateGroupConflict() {
        String realmName = "test-realm";
        String groupName = "Developers";

        Response mockResponse = mock(Response.class);
        when(groupsResource.add(any(GroupRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(Response.Status.CONFLICT.getStatusCode());

        GroupAlreadyExistsException ex = assertThrows(GroupAlreadyExistsException.class,
                () -> adminGroupService.createGroup(realmName, groupName));

        assertEquals(groupName, ex.getGroupName());
        assertEquals(realmName, ex.getRealmName());
    }

    @Test
    void testCreateGroupUnexpectedFailure() {
        String realmName = "test-realm";
        String groupName = "Developers";

        Response mockResponse = mock(Response.class);
        when(groupsResource.add(any(GroupRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adminGroupService.createGroup(realmName, groupName));

        assertTrue(ex.getMessage().contains("Failed to create group"));
    }

    @Test
    void testDeleteGroupSuccess() {
        String realmName = "test-realm";
        String groupId = "group-123";

        when(groupsResource.group(groupId)).thenReturn(groupResource);
        doNothing().when(groupResource).remove();

        assertDoesNotThrow(() -> adminGroupService.deleteGroup(realmName, groupId));
        verify(groupResource).remove();
    }

    @Test
    void testDeleteGroupNotFound() {
        String realmName = "test-realm";
        String groupId = "missing-group";

        WebApplicationException notFoundException = new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        when(groupsResource.group(groupId)).thenReturn(groupResource);
        doThrow(notFoundException).when(groupResource).remove();

        GroupNotFoundException ex = assertThrows(GroupNotFoundException.class,
                () -> adminGroupService.deleteGroup(realmName, groupId));

        assertEquals(groupId, ex.getGroupId());
        assertEquals(realmName, ex.getRealmName());
    }

    @Test
    void testDeleteGroupUnexpectedFailure() {
        String realmName = "test-realm";
        String groupId = "error-group";

        when(groupsResource.group(groupId)).thenReturn(groupResource);
        doThrow(new RuntimeException("boom")).when(groupResource).remove();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adminGroupService.deleteGroup(realmName, groupId));

        assertTrue(ex.getMessage().contains("Failed to delete group"));
    }

}
