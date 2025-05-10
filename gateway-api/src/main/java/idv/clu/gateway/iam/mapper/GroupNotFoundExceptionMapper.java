package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.GroupNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author clu
 */
@Provider
public class GroupNotFoundExceptionMapper implements ExceptionMapper<GroupNotFoundException> {

    @Override
    public Response toResponse(GroupNotFoundException exception) {
        final Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Group not found.");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("realm", exception.getRealmName());
        errorResponse.put("groupId", exception.getGroupId());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }

}
