package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.dto.GroupDTO;
import idv.clu.gateway.iam.service.AdminGroupService;
import idv.clu.gateway.iam.service.AdminRealmService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author clu
 */
@Path("/iam/admin/realms")
public class AdminGroupResource {

    private final AdminRealmService adminRealmService;
    private final AdminGroupService adminGroupService;

    @Inject
    public AdminGroupResource(AdminRealmService adminRealmService, AdminGroupService adminGroupService) {
        this.adminRealmService = adminRealmService;
        this.adminGroupService = adminGroupService;
    }

    @POST
    @Path("/{realmId}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createGroup(@PathParam("realmId") String realmId, @RequestBody GroupDTO groupDTO) {
        final String realmName = adminRealmService.getRealmById(realmId).getRealm();
        final String groupId =adminGroupService.createGroup(realmName, groupDTO.groupName());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("Group: '%s' created successfully.", groupDTO.groupName()));
        responseBody.put("groupId", groupId);

        return Response.status(Response.Status.CREATED)
                .entity(responseBody)
                .build();
    }

    @DELETE
    @Path("/{realmId}/groups/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGroup(@PathParam("realmId") String realmId, @PathParam("groupId") String groupId) {
        final String realmName = adminRealmService.getRealmById(realmId).getRealm();
        adminGroupService.deleteGroup(realmName, groupId);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", String.format("Group with id: '%s' deleted successfully", groupId));

        return Response.status(Response.Status.OK)
                .entity(responseBody)
                .build();
    }

}
