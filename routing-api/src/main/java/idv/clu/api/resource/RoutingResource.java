package idv.clu.api.resource;

import idv.clu.api.common.SimpleApiClient;
import idv.clu.api.common.SimpleApiClientProvider;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/route")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoutingResource {

    private final static Logger LOG = LoggerFactory.getLogger(RoutingResource.class);

    @Inject
    SimpleApiClientProvider simpleApiClientProvider;

    @POST
    @Path("/simple_api")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response simpleApiRoute(String requestPayload) {
        try {
            final SimpleApiClient client = simpleApiClientProvider.getNextClient();

            LOG.info("Route request to target instance: {}", client);

            Response response = client.echo(requestPayload);
            return Response
                    .status(response.getStatus())
                    .entity(response.getEntity())
                    .type(response.getMediaType())
                    .build();
        } catch (Exception e) {
            LOG.error("Error occurred while calling target instance: {}", e.getMessage());
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error occurred while calling target instance.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        }
    }

}
