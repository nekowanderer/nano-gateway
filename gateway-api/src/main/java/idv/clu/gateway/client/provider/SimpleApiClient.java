package idv.clu.gateway.client.provider;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author clu
 *
 * This component can't discover docker sontainer under docker compose env,
 * not sure if it's a lib bug or configuration issue, need to investigate further.
 */
@RegisterRestClient
@Path("/simple-api/rest_resource")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SimpleApiClient {

    @POST
    @Path("/echo")
    Response echo(String requestPayload);

}
