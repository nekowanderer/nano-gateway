package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.GroupNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author clu
 */
class GroupNotFoundExceptionMapperTest {

    private GroupNotFoundExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GroupNotFoundExceptionMapper();
    }

    @Test
    void testToResponse() {
        String testRealm = "test-realm";
        String testGroupId = "group-001";

        GroupNotFoundException exception = new GroupNotFoundException(testRealm, testGroupId);

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        Object entity = response.getEntity();
        assertNotNull(entity);
        assertInstanceOf(Map.class, entity);

        @SuppressWarnings("unchecked")
        Map<String, Object> entityMap = (Map<String, Object>) entity;

        assertEquals("Group not found.", entityMap.get("error"));
        assertEquals(exception.getMessage(), entityMap.get("message"));
        assertEquals(testRealm, entityMap.get("realm"));
        assertEquals(testGroupId, entityMap.get("groupId"));
    }

}
