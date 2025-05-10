package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.GroupAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * @author clu
 */
@Provider
public class GroupAlreadyExistsExceptionMapper implements ExceptionMapper<GroupAlreadyExistsException> {
    @Override
    public Response toResponse(GroupAlreadyExistsException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(Map.of(
                        "error", "Group already exists.",
                        "message", exception.getMessage(),
                        "realm", exception.getRealmName(),
                        "groupName", exception.getGroupName()
                ))
                .build();
    }

}
