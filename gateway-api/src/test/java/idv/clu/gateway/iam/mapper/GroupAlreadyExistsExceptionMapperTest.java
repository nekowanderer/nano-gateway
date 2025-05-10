package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.GroupAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author clu
 */
class GroupAlreadyExistsExceptionMapperTest {

    private GroupAlreadyExistsExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GroupAlreadyExistsExceptionMapper();
    }

    @Test
    void testToResponse() {
        String testRealm = "test-realm";
        String testGroupName = "developers";

        GroupAlreadyExistsException exception = new GroupAlreadyExistsException(testRealm, testGroupName);

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());

        Object entity = response.getEntity();
        assertNotNull(entity);
        assertInstanceOf(Map.class, entity);

        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;

        assertEquals("Group already exists.", entityMap.get("error"));
        assertEquals(exception.getMessage(), entityMap.get("message"));
        assertEquals(testRealm, entityMap.get("realm"));
        assertEquals(testGroupName, entityMap.get("groupName"));
    }

}

