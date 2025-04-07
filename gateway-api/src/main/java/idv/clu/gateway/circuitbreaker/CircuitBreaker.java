package idv.clu.gateway.circuitbreaker;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author clu
 */
@ApplicationScoped
public class CircuitBreaker {

    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreaker.class);

    final ConcurrentHashMap<String, CircuitContext> endpointContextList = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, CircuitState> endpointStates = new ConcurrentHashMap<>();

    @Inject
    @ConfigProperty(name = "circuitbreaker.reset.delay.ms", defaultValue = "10000")
    long circuitBreakerResetDelayMs;

    @SuppressWarnings("unused")
    public CircuitBreaker() {}

    public CircuitBreaker(long circuitBreakerResetDelayMs) {
        this.circuitBreakerResetDelayMs = circuitBreakerResetDelayMs;
    }

    public boolean allowRequest(String endpoint) {
        CircuitContext context = endpointContextList.computeIfAbsent(endpoint, k -> new CircuitContext());
        CircuitState state = endpointStates.computeIfAbsent(endpoint, k -> CircuitState.CLOSED);

        long currentTime = System.currentTimeMillis();

        CircuitState newState = state.checkState(currentTime, circuitBreakerResetDelayMs, context);
        endpointStates.put(endpoint, newState);

        return newState != CircuitState.OPEN;
    }

    public void reportSuccess(String endpoint) {
        endpointContextList.computeIfAbsent(endpoint, k -> new CircuitContext());
        CircuitState state = endpointStates.getOrDefault(endpoint, CircuitState.CLOSED);

        CircuitState newState = state.recordSuccess();
        endpointStates.put(endpoint, newState);
    }

    public void reportFailure(String endpoint) {
        CircuitContext context = endpointContextList.computeIfAbsent(endpoint, k -> new CircuitContext());
        CircuitState state = endpointStates.getOrDefault(endpoint, CircuitState.CLOSED);

        CircuitState newState = state.recordFailure(System.currentTimeMillis(), context);
        endpointStates.put(endpoint, newState);

        LOG.debug("Circuit breaker enabled for endpoint: {}", endpoint);
    }

}
