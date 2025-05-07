package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionMapperTest {

    private UserNotFoundExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserNotFoundExceptionMapper();
    }

    @Test
    void testToResponse() {
        String testRealm = "test-realm";
        String testUsername = "username";
        UserNotFoundException exception = new UserNotFoundException(testRealm, null, testUsername);

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus(), 
                "Response status should be 404 NOT_FOUND");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(Map.class, entity, "Response entity should be a Map");

        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;

        assertEquals("User not found.", entityMap.get("error"), "Error message should match");
        assertEquals(exception.getMessage(), entityMap.get("message"), "Exception message should match");
        assertEquals(testRealm, entityMap.get("realm"), "Realm should match");
        assertEquals(testUsername, entityMap.get("username"), "Username should match");
    }

}
