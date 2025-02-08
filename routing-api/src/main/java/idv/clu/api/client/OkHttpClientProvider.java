package idv.clu.api.client;

import idv.clu.api.circuitbreaker.CircuitBreaker;
import idv.clu.api.strategy.retry.RetryStrategy;
import idv.clu.api.strategy.routing.RoutingStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import okhttp3.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * @author clu
 */
@ApplicationScoped
public class OkHttpClientProvider {

    private final static Logger LOG = LoggerFactory.getLogger(OkHttpClientProvider.class);
    private final static MediaType APPLICATION_JSON =  MediaType.parse(jakarta.ws.rs.core.MediaType.APPLICATION_JSON);
    private final static int MAX_ATTEMPTS = 3;

    @Inject
    @ConfigProperty(name = "client.timeout.threshold", defaultValue = "10000")
    long clientTimeoutThreshold;

    @Inject
    OkHttpClient client;

    @Inject
    RetryStrategy retryStrategy;

    @Inject
    RoutingStrategy routingStrategy;

    @Inject
    CircuitBreaker circuitBreaker;

    public jakarta.ws.rs.core.Response sendGetRequest(String path) throws Exception {
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            final String targetUrl = routingStrategy.getNextTargetUrl(path);

            if (!circuitBreaker.allowRequest(targetUrl)) {
                LOG.warn("Request blocked by circuit breaker: {}", targetUrl);
                attempts++;
                continue;
            }

            try {
                return retryStrategy.executeWithRetries(() -> {
                    LOG.info("Sending GET request to: {}", targetUrl);

                    long startTime = System.currentTimeMillis();
                    Request request = new Request.Builder().url(targetUrl).get().build();
                    okhttp3.Response okHttpResponse = client.newCall(request).execute();
                    long responseTime = System.currentTimeMillis() - startTime;

                    if (responseTime > clientTimeoutThreshold) {
                        LOG.warn("Request to {} exceeded timeout threshold ({} ms).", targetUrl, clientTimeoutThreshold);
                        circuitBreaker.reportFailure(targetUrl);
                        return fallbackResponse();
                    }

                    circuitBreaker.reportSuccess(targetUrl);
                    return toJakartaResponse(okHttpResponse);
                });
            } catch (Exception e) {
                LOG.error("Request to {} failed: {}", targetUrl, e.getMessage());
                circuitBreaker.reportFailure(targetUrl);
                throw e;
            }
        }

        LOG.error("All target URLs are either blocked or failed.");
        return fallbackResponse();
    }

    public jakarta.ws.rs.core.Response sendPostRequest(String path, String payload) throws Exception {
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            final String targetUrl = routingStrategy.getNextTargetUrl(path);

            if (!circuitBreaker.allowRequest(targetUrl)) {
                LOG.warn("Request blocked by circuit breaker: {}", targetUrl);
                attempts++;
                continue;
            }

            try {
                return retryStrategy.executeWithRetries(() -> {
                    LOG.info("Sending POST request to: {}", targetUrl);

                    long startTime = System.currentTimeMillis();
                    Request request = new Request.Builder()
                            .url(targetUrl)
                            .post(RequestBody.create(payload, APPLICATION_JSON))
                            .build();
                    okhttp3.Response okHttpResponse = client.newCall(request).execute();
                    long responseTime = System.currentTimeMillis() - startTime;

                    if (responseTime > clientTimeoutThreshold) {
                        LOG.warn("Request to {} exceeded timeout threshold ({} ms).", targetUrl, clientTimeoutThreshold);
                        circuitBreaker.reportFailure(targetUrl);
                        return fallbackResponse();
                    }

                    circuitBreaker.reportSuccess(targetUrl);
                    return toJakartaResponse(okHttpResponse);
                });
            } catch (Exception e) {
                LOG.error("Request to {} failed: {}", targetUrl, e.getMessage());
                circuitBreaker.reportFailure(targetUrl);
                throw e;
            }
        }

        LOG.error("All target URLs are either blocked or failed.");
        return fallbackResponse();
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

    private jakarta.ws.rs.core.Response fallbackResponse() {
        return jakarta.ws.rs.core.Response
                .status(503)
                .entity("{\"message\":\"Service unavailable\"}")
                .build();
    }

}
