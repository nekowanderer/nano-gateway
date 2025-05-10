package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.GroupDTO;
import idv.clu.gateway.iam.exception.GroupAlreadyExistsException;
import idv.clu.gateway.iam.exception.GroupNotFoundException;
import idv.clu.gateway.iam.service.AdminGroupService;
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

/**
 * @author clu
 */
public class AdminGroupResourceTest {

    @Mock
    private AdminRealmService adminRealmService;

    @Mock
    private AdminGroupService adminGroupService;

    private AdminGroupResource adminGroupResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminGroupResource = new AdminGroupResource(adminRealmService, adminGroupService);
    }

    @Test
    void testCreateGroupSuccess() {
        String testRealmId = "realm-1";
        String testRealmName = "Test Realm";
        String testGroupId = "group-123";
        GroupDTO groupDTO = new GroupDTO("Developers");

        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realm);
        when(adminGroupService.createGroup(testRealmName, groupDTO.groupName())).thenReturn(testGroupId);

        try (Response response = adminGroupResource.createGroup(testRealmId, groupDTO)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

            Map<String, String> body = (Map<String, String>) response.getEntity();
            assertEquals("Group: 'Developers' created successfully.", body.get("message"));
            assertEquals(testGroupId, body.get("groupId"));
        }

        verify(adminGroupService).createGroup(testRealmName, groupDTO.groupName());
    }

    @Test
    void testCreateGroupAlreadyExists() {
        String testRealmId = "realm-1";
        String testRealmName = "Test Realm";
        GroupDTO groupDTO = new GroupDTO("Admins");

        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realm);
        when(adminGroupService.createGroup(testRealmName, groupDTO.groupName()))
                .thenThrow(new GroupAlreadyExistsException(testRealmName, groupDTO.groupName()));

        assertThrows(GroupAlreadyExistsException.class,
                () -> adminGroupResource.createGroup(testRealmId, groupDTO));

        verify(adminGroupService).createGroup(testRealmName, groupDTO.groupName());
    }

    @Test
    void testDeleteGroupSuccess() {
        String testRealmId = "realm-1";
        String testRealmName = "Test Realm";
        String testGroupId = "group-123";

        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realm);
        doNothing().when(adminGroupService).deleteGroup(testRealmName, testGroupId);

        try (Response response = adminGroupResource.deleteGroup(testRealmId, testGroupId)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            Map<String, String> body = (Map<String, String>) response.getEntity();
            assertEquals("Group with id: 'group-123' deleted successfully", body.get("message"));
        }

        verify(adminGroupService).deleteGroup(testRealmName, testGroupId);
    }

    @Test
    void testDeleteGroupNotFound() {
        String testRealmId = "realm-1";
        String testRealmName = "Test Realm";
        String testGroupId = "group-999";

        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(testRealmName);

        when(adminRealmService.getRealmById(testRealmId)).thenReturn(realm);
        doThrow(new GroupNotFoundException(testRealmName, testGroupId))
                .when(adminGroupService).deleteGroup(testRealmName, testGroupId);

        assertThrows(GroupNotFoundException.class,
                () -> adminGroupResource.deleteGroup(testRealmId, testGroupId));

        verify(adminGroupService).deleteGroup(testRealmName, testGroupId);
    }

}
