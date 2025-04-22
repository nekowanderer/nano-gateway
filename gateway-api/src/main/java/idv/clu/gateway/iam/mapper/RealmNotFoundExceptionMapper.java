package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.RealmNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * @author clu
 */
@Provider
public class RealmNotFoundExceptionMapper implements ExceptionMapper<RealmNotFoundException> {

    @Override
    public Response toResponse(RealmNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                        "error", "realm_not_found",
                        "message", e.getMessage(),
                        "realmId", e.getRealmId()
                ))
                .build();
    }

}