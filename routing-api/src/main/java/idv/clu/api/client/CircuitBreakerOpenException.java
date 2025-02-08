package idv.clu.api.client;

/**
 * @author clu
 */
public class CircuitBreakerOpenException extends RuntimeException {

    private final String targetUrl;

    public CircuitBreakerOpenException(String targetUrl) {
        super("Circuit Breaker is OPEN for target URL: " + targetUrl);
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

}
