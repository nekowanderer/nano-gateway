package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserAlreadyExistsExceptionMapperTest {

    private UserAlreadyExistsExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserAlreadyExistsExceptionMapper();
    }

    @Test
    void testToResponse() {
        String testRealm = "test-realm";
        String testUsername = "username";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(testRealm, testUsername);

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus(), 
                "Response status should be 409 CONFLICT");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(Map.class, entity, "Response entity should be a Map");

        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;

        assertEquals("User already exists.", entityMap.get("error"), "Error message should match");
        assertEquals(exception.getMessage(), entityMap.get("message"), "Exception message should match");
        assertEquals(testRealm, entityMap.get("realm"), "Realm should match");
        assertEquals(testUsername, entityMap.get("username"), "Username should match");
    }

}