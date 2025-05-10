package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.LeaveGroupFailedException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author clu
 */
class LeaveGroupFailedExceptionMapperTest {

    private LeaveGroupFailedExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LeaveGroupFailedExceptionMapper();
    }

    @Test
    void testToResponse() {
        String testRealm = "test-realm";
        String testUserId = "user-123";
        String testGroupId = "group-abc";

        LeaveGroupFailedException exception = new LeaveGroupFailedException(testRealm, testGroupId, testUserId);

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        Object entity = response.getEntity();
        assertNotNull(entity);
        assertInstanceOf(Map.class, entity);

        @SuppressWarnings("unchecked")
        Map<String, Object> entityMap = (Map<String, Object>) entity;

        assertEquals("Leave group failed.", entityMap.get("error"));
        assertEquals(exception.getMessage(), entityMap.get("message"));
        assertEquals(testRealm, entityMap.get("realm"));
        assertEquals(testUserId, entityMap.get("userId"));
        assertEquals(testGroupId, entityMap.get("groupId"));
    }

}
