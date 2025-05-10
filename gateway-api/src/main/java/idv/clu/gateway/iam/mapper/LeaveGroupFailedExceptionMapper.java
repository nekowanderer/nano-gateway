package idv.clu.gateway.iam.mapper;

import idv.clu.gateway.iam.exception.LeaveGroupFailedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author clu
 */
public class LeaveGroupFailedExceptionMapper implements ExceptionMapper<LeaveGroupFailedException> {

    @Override
    public Response toResponse(LeaveGroupFailedException exception) {
        final Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Leave group failed.");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("realm", exception.getRealmName());
        errorResponse.put("groupId", exception.getGroupId());
        errorResponse.put("userId", exception.getUserId());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }

}
