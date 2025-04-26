package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.RealmNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RealmNotFoundExceptionMapperTest {

    private RealmNotFoundExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RealmNotFoundExceptionMapper();
    }

    @Test
    void testToResponse() {
        String testRealmId = "test-realm";
        String testRealmName = "test realm name";
        RealmNotFoundException exception = new RealmNotFoundException(testRealmId, testRealmName);

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus(), 
                "Response status should be 404 NOT_FOUND");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(Map.class, entity, "Response entity should be a Map");
        
        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;
        
        assertEquals("realm_not_found", entityMap.get("error"), "Error message should match");
        assertEquals(exception.getMessage(), entityMap.get("message"), "Exception message should match");
        assertEquals(testRealmId, entityMap.get("realmId"), "RealmId should match");
    }

}