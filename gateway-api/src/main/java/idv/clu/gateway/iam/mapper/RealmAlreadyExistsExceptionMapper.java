package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * @author clu
 */
@Provider
public class RealmAlreadyExistsExceptionMapper implements ExceptionMapper<RealmAlreadyExistsException> {

    @Override
    public Response toResponse(RealmAlreadyExistsException e) {
        return Response.status(Response.Status.CONFLICT)
                .entity(Map.of(
                        "error", "realm_already_exists",
                        "message", e.getMessage(),
                        "realmId", e.getRealmId()
                ))
                .build();
    }

}