package idv.clu.api.client.exception;

/**
 * @author clu
 */
public class CircuitBreakerOpenException extends RuntimeException {

    private final String targetUrl;

    public CircuitBreakerOpenException(String targetUrl) {
        super(String.format("Circuit Breaker is OPEN for target URL: %s, try next instance.", targetUrl));
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

}
