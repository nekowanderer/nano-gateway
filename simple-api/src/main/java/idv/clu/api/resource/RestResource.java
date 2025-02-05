package idv.clu.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * @author clu
 */
@Path("/rest_resource")
public class RestResource {

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response echo(String requestPayload) {
        try {
            final Map<String, Object> parsedPayload =
                    objectMapper.readValue(
                            requestPayload, new TypeReference<>() {
            });

            return Response
                    .ok(objectMapper.writeValueAsString(parsedPayload))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (JsonProcessingException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid JSON\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

}
