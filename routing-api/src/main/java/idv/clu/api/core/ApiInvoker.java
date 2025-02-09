package idv.clu.api.core;

import idv.clu.api.circuitbreaker.CircuitBreaker;
import idv.clu.api.client.exception.CircuitBreakerOpenException;
import idv.clu.api.client.exception.ClientTimeoutException;
import idv.clu.api.client.model.HttpResult;
import idv.clu.api.executor.HttpRequestExecutor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author clu
 */
@ApplicationScoped
public class ApiInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(ApiInvoker.class);

    @Inject
    @ConfigProperty(name = "api.invoker.retry.max.count", defaultValue = "3")
    int maxRetryCount;

    @Inject
    HttpRequestExecutor httpRequestExecutor;

    @Inject
    CircuitBreaker circuitBreaker;

    public Response invokeGet(String path) throws Exception {
        return invoke(() -> httpRequestExecutor.sendGetRequest(path));
    }

    public Response invokePost(String path, String payload) throws Exception {
        return invoke(() -> httpRequestExecutor.sendPostRequest(path, payload));
    }

    private Response invoke(ApiCall apiCall) throws Exception {
        HttpResult result;
        String endpoint = null;
        int retries = 0;

        while (retries < maxRetryCount) {
            try {
                result = apiCall.call();
                endpoint = result.getEndpoint();

                if (result.hasResponse()) {
                    circuitBreaker.reportSuccess(endpoint);
                    return result.getResponse();
                } else {
                    Exception exception = result.getException();
                    circuitBreaker.reportFailure(endpoint);
                    return handleFailure(exception, endpoint);
                }
            } catch (ClientTimeoutException clientTimeoutException) {
                retries++;
                if (retries == maxRetryCount) {
                    LOG.error("Request failed after {} retries for endpoint: {}", maxRetryCount, endpoint);
                    circuitBreaker.reportFailure(endpoint);
                    throw clientTimeoutException;
                }
            } catch (CircuitBreakerOpenException circuitBreakerOpenException) {
                LOG.error("Circuit breaker is OPEN for endpoint: {}, try next instance.", circuitBreakerOpenException.getTargetUrl());
                retries++;
            } catch (Exception exception) {
                circuitBreaker.reportFailure(endpoint);
                return handleFailure(exception, endpoint);
            }
        }

        return fallbackResponse();
    }

    private Response handleFailure(Exception exception, String endpoint) throws Exception {
        switch (exception.getClass().getSimpleName()) {
            case "ClientHttpRequestException":
                LOG.error("Request failed for endpoint: {}", endpoint);
                break;
            case "ClientTimeoutException":
                LOG.error("Request timed out for endpoint: {}", endpoint);
                throw exception;
            default:
                LOG.error("Unexpected error for endpoint: {}", endpoint);
                break;
        }
        return fallbackResponse();
    }


    private Response fallbackResponse() {
        return Response
                .status(503)
                .entity("{\"message\":\"Service unavailable\"}")
                .build();
    }

    @FunctionalInterface
    private interface ApiCall {
        HttpResult call();
    }

}
