package idv.clu.api.circuitbreaker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CircuitBreakerTest {

    private final static long DEFAULT_DELAY_TIME_MS = 10000L;
    private final static String DEFAULT_ENDPOINT = "test-endpoint";

    @Test
    public void testAllowRequestWhenStateIsClosedShouldAllowRequest() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(DEFAULT_DELAY_TIME_MS);
        CircuitState circuitState = CircuitState.CLOSED;

        var context = new CircuitContext();
        circuitBreaker.endpointContextList.put(DEFAULT_ENDPOINT, context);
        circuitBreaker.endpointStates.put(DEFAULT_ENDPOINT, circuitState);

        boolean isAllowed = circuitBreaker.allowRequest(DEFAULT_ENDPOINT);

        assertTrue(isAllowed);
        assertEquals(CircuitState.CLOSED, circuitState);
    }

    @Test
    public void testAllowRequestWhenStateIsOpenShouldNotAllowRequest() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(1000000L);
        CircuitState circuitState = CircuitState.OPEN;

        var context = new CircuitContext();
        context.setLastFailedTime(System.currentTimeMillis());
        circuitBreaker.endpointContextList.put(DEFAULT_ENDPOINT, context);
        circuitBreaker.endpointStates.put(DEFAULT_ENDPOINT, circuitState);

        boolean isAllowed = circuitBreaker.allowRequest(DEFAULT_ENDPOINT);

        assertFalse(isAllowed);
        assertEquals(CircuitState.OPEN, circuitState);
    }

    @Test
    public void testAllowRequestWhenStateIsHalfOpenShouldAllowRequest() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(DEFAULT_DELAY_TIME_MS);
        CircuitState circuitState = CircuitState.HALF_OPEN;

        var context = new CircuitContext();
        context.setLastFailedTime(System.currentTimeMillis());
        circuitBreaker.endpointContextList.put(DEFAULT_ENDPOINT, context);
        circuitBreaker.endpointStates.put(DEFAULT_ENDPOINT, circuitState);

        boolean isAllowed = circuitBreaker.allowRequest(DEFAULT_ENDPOINT);

        assertTrue(isAllowed);
        assertEquals(CircuitState.HALF_OPEN, circuitState);
    }

    @Test
    public void testAllowRequestWhenEndpointIsNewShouldAllowRequest() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(DEFAULT_DELAY_TIME_MS);

        boolean isAllowed = circuitBreaker.allowRequest(DEFAULT_ENDPOINT);

        assertTrue(isAllowed);
        assertEquals(CircuitState.CLOSED, circuitBreaker.endpointStates.get(DEFAULT_ENDPOINT));
    }

    @Test
    public void testAllowRequestWhenCheckStateUpdatesStateShouldUpdateStateInMap() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(0L);
        CircuitState circuitState = CircuitState.OPEN;

        var context = new CircuitContext();
        context.setLastFailedTime(System.currentTimeMillis());
        circuitBreaker.endpointContextList.put(DEFAULT_ENDPOINT, context);
        circuitBreaker.endpointStates.put(DEFAULT_ENDPOINT, circuitState);

        boolean isAllowed = circuitBreaker.allowRequest(DEFAULT_ENDPOINT);

        assertTrue(isAllowed);
        assertEquals(CircuitState.HALF_OPEN, circuitBreaker.endpointStates.get(DEFAULT_ENDPOINT));
    }

    @Test
    public void testAllowRequestWhenOpenDelayNotElapsed() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(5000L);
        var context = new CircuitContext();
        context.setLastFailedTime(System.currentTimeMillis());
        circuitBreaker.endpointContextList.put(DEFAULT_ENDPOINT, context);
        circuitBreaker.endpointStates.put(DEFAULT_ENDPOINT, CircuitState.OPEN);

        assertFalse(circuitBreaker.allowRequest(DEFAULT_ENDPOINT));
        assertEquals(CircuitState.OPEN, circuitBreaker.endpointStates.get(DEFAULT_ENDPOINT));
    }

    @Test
    public void testAllowRequestWithRepeatedCallsToCheckTransitions() throws InterruptedException {
        CircuitBreaker circuitBreaker = new CircuitBreaker(100L);
        var context = new CircuitContext();
        circuitBreaker.endpointContextList.put(DEFAULT_ENDPOINT, context);

        circuitBreaker.reportFailure(DEFAULT_ENDPOINT);
        assertFalse(circuitBreaker.allowRequest(DEFAULT_ENDPOINT));
        assertEquals(CircuitState.OPEN, circuitBreaker.endpointStates.get(DEFAULT_ENDPOINT));

        Thread.sleep(250);
        assertTrue(circuitBreaker.allowRequest(DEFAULT_ENDPOINT));
        assertEquals(CircuitState.HALF_OPEN, circuitBreaker.endpointStates.get(DEFAULT_ENDPOINT));

        circuitBreaker.reportSuccess(DEFAULT_ENDPOINT);
        assertTrue(circuitBreaker.allowRequest(DEFAULT_ENDPOINT));
        assertEquals(CircuitState.CLOSED, circuitBreaker.endpointStates.get(DEFAULT_ENDPOINT));
    }

    @Test
    public void testAllowRequestHandlesMultipleEndpointsIndependently() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(DEFAULT_DELAY_TIME_MS);

        String endpoint1 = "endpoint-1";
        String endpoint2 = "endpoint-2";

        assertTrue(circuitBreaker.allowRequest(endpoint1));
        assertTrue(circuitBreaker.allowRequest(endpoint2));

        circuitBreaker.reportFailure(endpoint1);

        assertFalse(circuitBreaker.allowRequest(endpoint1));
        assertTrue(circuitBreaker.allowRequest(endpoint2));

        circuitBreaker.reportSuccess(endpoint2);
        assertTrue(circuitBreaker.allowRequest(endpoint2));
        assertEquals(CircuitState.CLOSED, circuitBreaker.endpointStates.get(endpoint2));
    }

}