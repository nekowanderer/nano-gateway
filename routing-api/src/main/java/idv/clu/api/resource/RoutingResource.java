package idv.clu.api.resource;

import idv.clu.api.client.OkHttpClientProvider;
import idv.clu.api.client.SimpleApiResource;
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
    OkHttpClientProvider okHttpClientProvider;

    @POST
    @Path("/simple_api")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response simpleApiRoute(String requestPayload) {
        try {
            return okHttpClientProvider.sendPostRequest(SimpleApiResource.getSimpleApiEchoUrl(), requestPayload);
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
