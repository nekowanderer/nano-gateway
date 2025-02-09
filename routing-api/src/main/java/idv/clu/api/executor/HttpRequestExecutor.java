package idv.clu.api.executor;

import idv.clu.api.circuitbreaker.CircuitBreaker;
import idv.clu.api.client.exception.CircuitBreakerOpenException;
import idv.clu.api.client.exception.ClientHttpRequestException;
import idv.clu.api.client.exception.ClientTimeoutException;
import idv.clu.api.client.model.HttpResult;
import idv.clu.api.strategy.retry.RetryStrategy;
import idv.clu.api.strategy.retry.RetryStrategyType;
import idv.clu.api.strategy.routing.RoutingStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

/**
 * @author clu
 */
@ApplicationScoped
public class HttpRequestExecutor {

    private final static Logger LOG = LoggerFactory.getLogger(HttpRequestExecutor.class);
    private final static MediaType APPLICATION_JSON =  MediaType.parse(jakarta.ws.rs.core.MediaType.APPLICATION_JSON);

    @Inject
    OkHttpClient client;

    @Inject
    @RetryStrategyType(RetryStrategyType.Strategy.FIXED_DELAY)
    RetryStrategy retryStrategy;

    @Inject
    RoutingStrategy routingStrategy;

    @Inject
    CircuitBreaker circuitBreaker;

    @FunctionalInterface
    private interface RequestBuilder {
        Request build();
    }

    public HttpResult sendGetRequest(String path) {
        return sendRequest(() -> new Request.Builder()
                .url(routingStrategy.getNextTargetUrl(path))
                .get()
                .build());
    }

    public HttpResult sendPostRequest(String path, String payload) {
        return sendRequest(() -> new Request.Builder()
                .url(routingStrategy.getNextTargetUrl(path))
                .post(RequestBody.create(payload, APPLICATION_JSON))
                .build());
    }

    private HttpResult sendRequest(RequestBuilder requestBuilder) {
        final Request request = requestBuilder.build();
        final String targetUrl = request.url().toString();

        if (!circuitBreaker.allowRequest(targetUrl)) {
            throw new CircuitBreakerOpenException(targetUrl);
        }

        try {
            jakarta.ws.rs.core.Response response = retryStrategy.executeWithRetries(() -> {
                LOG.debug("Sending POST request to: {}", targetUrl);
                okhttp3.Response okHttpResponse = client.newCall(request).execute();
                return toJakartaResponse(okHttpResponse);
            });

            return new HttpResult(targetUrl, response);
        } catch (SocketTimeoutException timeoutException) {
            final String logMessage = String.format("Request to %s timed out.", targetUrl);
            LOG.warn(logMessage);
            ClientTimeoutException clientTimeoutException = new ClientTimeoutException(logMessage);
            return new HttpResult(targetUrl, clientTimeoutException);
        } catch (CircuitBreakerOpenException circuitBreakerOpenException) {
            throw circuitBreakerOpenException;
        } catch (Exception exception) {
            String logMessage = String.format("Failed to send request to %s.", targetUrl);
            LOG.error("{} Detail exception: {}.", logMessage, exception.toString());
            return new HttpResult(targetUrl, new ClientHttpRequestException(logMessage, exception));
        }
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
