package idv.clu.api.resource;

import idv.clu.api.client.ApiInvoker;
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
    ApiInvoker apiInvoker;

    @POST
    @Path("/simple_api/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response simpleApiEchoRoute(String requestPayload) {
        try {
            return apiInvoker.invokePost(SimpleApiResource.getSimpleApiEchoUrl(), requestPayload);
        } catch (Exception e) {
            LOG.error("Error occurred while calling target instance: {}", e.getMessage());
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error occurred while calling target instance.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @POST
    @Path("/simple_api/delay")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response simpleApiDelayRoute(String requestPayload) {
        try {
            return apiInvoker.invokePost(SimpleApiResource.getSimpleApiDelayUrl(), requestPayload);
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
