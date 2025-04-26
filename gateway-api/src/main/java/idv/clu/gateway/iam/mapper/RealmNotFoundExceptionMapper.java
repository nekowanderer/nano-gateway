package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.RealmNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author clu
 */
@Provider
public class RealmNotFoundExceptionMapper implements ExceptionMapper<RealmNotFoundException> {

    @Override
    public Response toResponse(RealmNotFoundException e) {
        final Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "realm_not_found");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("realmId", e.getRealmId());
        errorResponse.put("realmName", e.getRealmName());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }

}