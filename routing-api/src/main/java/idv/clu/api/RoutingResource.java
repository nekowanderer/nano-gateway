package idv.clu.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/process")
public class RoutingResource {

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public String route(String requestPayload) {
        return "Route";
    }

}
