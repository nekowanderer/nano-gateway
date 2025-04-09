package idv.clu.gateway.iam.resource;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

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