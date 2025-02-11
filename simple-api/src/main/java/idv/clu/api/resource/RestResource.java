package idv.clu.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @author clu
 */
@Path("/rest_resource")
public class RestResource {

    @Inject
    ObjectMapper objectMapper;

    private final AtomicLong delayInMsec = new AtomicLong(0L);

    private final AtomicBoolean serverDown = new AtomicBoolean(false);

    private final AtomicLong recoverAfterMillis = new AtomicLong(0);

    private final ScheduledExecutorService recoverScheduler = Executors.newSingleThreadScheduledExecutor();

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response echo(String requestPayload) {
        try {
            Response serverDownResponse = checkServerStatus();
            if (serverDownResponse != null) {
                return serverDownResponse;
            }

            Thread.sleep(delayInMsec.get());

            return processPayload(requestPayload, parsedPayload -> parsedPayload);
        } catch (InterruptedException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Thread interrupted\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @POST
    @Path("/delay")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setDelay(String requestPayload) {
        Response serverDownResponse = checkServerStatus();
        if (serverDownResponse != null) {
            return serverDownResponse;
        }

        return processPayload(requestPayload, parsedPayload -> {
            this.delayInMsec.set(Long.parseLong(parsedPayload.get("delay").toString()));
            if (this.delayInMsec.get() < 0) {
                this.delayInMsec.set(0L);
            }

            return Map.of("delayUpdateInformation", String.format("Delay updated to %d ms", delayInMsec.get()));
        });
    }

    @POST
    @Path("/simulate_error")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response simulateServerError(String requestPayload) {
        return processPayload(requestPayload, parsedPayload -> {
            this.serverDown.set(Boolean.parseBoolean(parsedPayload.get("serverDown").toString()));
            this.recoverAfterMillis.set(Long.parseLong(parsedPayload.get("recoverAfterMillis").toString()));

            if (this.serverDown.get()) {
                scheduleRecovery();
            }

            return Map.of("message", "Server status updated.");
        });
    }

    private Response processPayload(String requestPayload, Function<Map<String, Object>, Map<String, Object>> handler) {
        try {
            final Map<String, Object> parsedPayload =
                    objectMapper.readValue(
                            requestPayload, new TypeReference<>() {
                            });
            final Map<String, Object> result = handler.apply(parsedPayload);

            return Response.ok(objectMapper.writeValueAsString(result))
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

    private Response checkServerStatus() {
        if (this.serverDown.get()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Server is down. Try again later.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return null;
    }

    private void scheduleRecovery() {
        recoverScheduler.schedule(() ->
                serverDown.set(false), this.recoverAfterMillis.get(), TimeUnit.MILLISECONDS);
    }

}
