package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.UserNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author clu
 */
@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {

    @Override
    public Response toResponse(UserNotFoundException e) {
        final Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "User not found.");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("realm", e.getRealmName());
        errorResponse.put("userId", e.getUserId());
        errorResponse.put("username", e.getUsername());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }

}
