package idv.clu.gateway.iam.resource;

import idv.clu.gateway.iam.service.AdminClientService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * @author clu
 */
@Path("/iam/admin-resource/")
public class AdminResource {

    private final AdminClientService adminClientService;

    @Inject
    public AdminResource(AdminClientService adminClientService) {
        this.adminClientService = adminClientService;
    }

    // TODO error handling/not found
    @GET
    @Path("list-users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@QueryParam("realm") String realm) {
        List<UserRepresentation> users = adminClientService.listUsers(realm);
        return Response.ok(users).build();
    }

}
