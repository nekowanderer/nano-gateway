package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * @author clu
 */
@Provider
public class UserAlreadyExistsExceptionMapper implements ExceptionMapper<UserAlreadyExistsException> {

    @Override
    public Response toResponse(UserAlreadyExistsException e) {
        return Response.status(Response.Status.CONFLICT)
                .entity(Map.of(
                        "error", "User already exists.",
                        "message", e.getMessage(),
                        "realm", e.getRealmName(),
                        "username", e.getUsername()
                ))
                .build();
    }

}
