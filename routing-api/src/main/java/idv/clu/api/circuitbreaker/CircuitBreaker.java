package idv.clu.api.circuitbreaker;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author clu
 */
@ApplicationScoped
public class CircuitBreaker {

    final ConcurrentHashMap<String, CircuitContext> endpointContextList = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, CircuitState> endpointStates = new ConcurrentHashMap<>();

    @Inject
    @ConfigProperty(name = "circuitbreaker.delay.ms", defaultValue = "10000")
    long delayTimeMs;

    @SuppressWarnings("unused")
    public CircuitBreaker() {}

    public CircuitBreaker(long delayTimeMs) {
        this.delayTimeMs = delayTimeMs;
    }

    public boolean allowRequest(String endpoint) {
        CircuitContext context = endpointContextList.computeIfAbsent(endpoint, k -> new CircuitContext());
        CircuitState state = endpointStates.computeIfAbsent(endpoint, k -> CircuitState.CLOSED);

        long currentTime = System.currentTimeMillis();

        CircuitState newState = state.checkState(currentTime, delayTimeMs, context);
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
    }

}
