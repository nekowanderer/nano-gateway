package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * @author clu
 */
@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {

    @Override
    public Response toResponse(UserNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                        "error", "User not found.",
                        "message", e.getMessage(),
                        "realm", e.getRealm()
                ))
                .build();
    }

}
