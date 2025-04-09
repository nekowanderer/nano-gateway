package idv.clu.gateway.iam.resource;

import io.quarkus.security.identity.SecurityIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserResourceTest {

    @Mock
    private SecurityIdentity securityIdentity;

    @Mock
    private Principal principal;

    @InjectMocks
    private UserResource userResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMe() {
        String expectedUsername = "testUser";

        when(securityIdentity.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(expectedUsername);

        UserResource.User user = userResource.me();

        assertEquals(expectedUsername, user.getUserName(), 
                "The username should match the one from the security identity");
    }

    @Test
    void testUserConstructor() {
        String expectedUsername = "testUser";

        when(securityIdentity.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(expectedUsername);

        UserResource.User user = new UserResource.User(securityIdentity);

        assertEquals(expectedUsername, user.getUserName(), 
                "The username should match the one from the security identity");
    }
}