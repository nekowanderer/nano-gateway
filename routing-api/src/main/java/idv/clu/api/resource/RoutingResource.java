package idv.clu.api.resource;

import ide.clu.api.common.RoutingConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/route")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoutingResource {

    @Inject
    RoutingConfig routingConfig;

    private final AtomicInteger index = new AtomicInteger(0);

    @POST
    @Path("/simple_api")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response simpleApiRoute(String requestPayload) {
        final List<String> instances = routingConfig.getSimpleApiInstances();
        if (instances.isEmpty()) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"No instances configured for simple api\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String targetUrl = instances.get(index.getAndUpdate(i -> (i + 1) % instances.size()));

        return Response
                .ok()
                .entity("{\"route_to\": \"" + targetUrl + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
