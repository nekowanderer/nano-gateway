package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RealmAlreadyExistsExceptionMapperTest {

    private RealmAlreadyExistsExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RealmAlreadyExistsExceptionMapper();
    }

    @Test
    void testToResponse() {
        String testRealmId = "test-realm";
        RealmAlreadyExistsException exception = new RealmAlreadyExistsException(testRealmId);

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus(), 
                "Response status should be 409 CONFLICT");

        Object entity = response.getEntity();
        assertNotNull(entity, "Response entity should not be null");
        assertInstanceOf(Map.class, entity, "Response entity should be a Map");
        
        @SuppressWarnings("unchecked")
        Map<String, String> entityMap = (Map<String, String>) entity;
        
        assertEquals("realm_already_exists", entityMap.get("error"), "Error message should match");
        assertEquals(exception.getMessage(), entityMap.get("message"), "Exception message should match");
        assertEquals(testRealmId, entityMap.get("realmId"), "RealmId should match");
    }

}