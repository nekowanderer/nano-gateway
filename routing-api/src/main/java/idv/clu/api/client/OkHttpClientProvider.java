package idv.clu.api.client;

import idv.clu.api.common.RoutingConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author clu
 */
@ApplicationScoped
public class OkHttpClientProvider {

    private final static Logger LOG = LoggerFactory.getLogger(OkHttpClientProvider.class);
    private final static String APPLICATION_JSON = jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

    @Inject
    RoutingConfig routingConfig;

    @Inject
    OkHttpClient client;

    List<String> availableInstances;
    AtomicInteger roundRobinIndex;

    @PostConstruct
    public void init() {
        LOG.info("Initializing OkHttpClientProvider...");

        this.availableInstances = routingConfig.getSimpleApiInstances();
        if (availableInstances.isEmpty()) {
            throw new IllegalStateException("No API instances configured in routingConfig");
        }

        this.roundRobinIndex = new AtomicInteger(0);

        LOG.info("Available API instances: {}", availableInstances);
    }

    private String getNextTargetUrl() {
        final int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % availableInstances.size());
        final String nextUrl = availableInstances.get(index);
        LOG.debug("Next target URL selected by round robin: {}", nextUrl);
        return nextUrl;

    }

    public jakarta.ws.rs.core.Response sendGetRequest(String path) throws IOException {
        String targetUrl = getNextTargetUrl();
        String fullUrl = targetUrl + path;

        LOG.info("Sending GET request to: {}", fullUrl);

        Request request = new Request.Builder()
                .url(fullUrl)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            return toJakartaResponse(response);
        }
    }

    public jakarta.ws.rs.core.Response sendPostRequest(String path, String requestPayload) throws IOException {
        String targetUrl = getNextTargetUrl();
        String fullUrl = targetUrl + path;

        LOG.info("Sending POST request to: {}", fullUrl);

        RequestBody body = RequestBody.create(
                requestPayload,
                MediaType.parse(APPLICATION_JSON)
        );

        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            return toJakartaResponse(response);
        }
    }

    jakarta.ws.rs.core.Response toJakartaResponse(Response okHttpResponse) throws IOException {
        final int statusCode = okHttpResponse.code();

        final String entity = (okHttpResponse.body() != null) ? okHttpResponse.body().string() : "";

        final String mediaType = (okHttpResponse.body() != null && okHttpResponse.body().contentType() != null)
                ? Objects.requireNonNull(okHttpResponse.body().contentType()).toString()
                : APPLICATION_JSON;

        return jakarta.ws.rs.core.Response.status(statusCode)
                .entity(entity)
                .type(mediaType)
                .build();
    }

}
