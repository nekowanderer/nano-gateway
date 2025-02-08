package idv.clu.api.client;

import idv.clu.api.common.RoutingConfig;
import idv.clu.api.strategy.retry.RetryStrategy;
import idv.clu.api.strategy.routing.RoutingStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author clu
 */
@ApplicationScoped
public class OkHttpClientProvider {

    private final static Logger LOG = LoggerFactory.getLogger(OkHttpClientProvider.class);
    private final static MediaType APPLICATION_JSON =  MediaType.parse(jakarta.ws.rs.core.MediaType.APPLICATION_JSON);

    @Inject
    RoutingConfig routingConfig;

    @Inject
    OkHttpClient client;

    @Inject
    RetryStrategy retryStrategy;

    @Inject
    RoutingStrategy routingStrategy;

    List<String> availableInstances;

    @PostConstruct
    public void init() {
        LOG.info("Initializing OkHttpClientProvider...");

        this.availableInstances = routingConfig.getAvailableInstances();

        if (availableInstances.isEmpty()) {
            throw new IllegalStateException("No API instances configured in routingConfig.");
        }

        LOG.info("Available API instances: {}", availableInstances);
    }

    public jakarta.ws.rs.core.Response sendGetRequest(String path) throws Exception {
        return retryStrategy.executeWithRetries(() -> {
            String targetUrl = routingStrategy.getNextTargetUrl(availableInstances) + path;
            LOG.info("Sending GET request to: {}", targetUrl);
            Request request = new Request.Builder().url(targetUrl).get().build();
            okhttp3.Response okHttpResponse = client.newCall(request).execute();
            return toJakartaResponse(okHttpResponse);
        });
    }

    public jakarta.ws.rs.core.Response sendPostRequest(String path, String payload) throws Exception {
        return retryStrategy.executeWithRetries(() -> {
            String targetUrl = routingStrategy.getNextTargetUrl(availableInstances) + path;
            LOG.info("Sending POST request to: {}", targetUrl);
            Request request = new Request.Builder()
                    .url(targetUrl)
                    .post(RequestBody.create(payload, APPLICATION_JSON))
                    .build();
            okhttp3.Response okHttpResponse = client.newCall(request).execute();
            return toJakartaResponse(okHttpResponse);
        });
    }

    jakarta.ws.rs.core.Response toJakartaResponse(Response okHttpResponse) throws IOException {
        final int statusCode = okHttpResponse.code();

        final String entity = (okHttpResponse.body() != null) ? okHttpResponse.body().string() : "";

        final String mediaType = (okHttpResponse.body() != null && okHttpResponse.body().contentType() != null)
                ? Objects.requireNonNull(okHttpResponse.body().contentType()).toString()
                : jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

        return jakarta.ws.rs.core.Response.status(statusCode)
                .entity(entity)
                .type(mediaType)
                .build();
    }

}
