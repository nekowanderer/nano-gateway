package idv.clu.api.client.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CircuitBreakerOpenExceptionTest {

    @Test
    void testGetTargetUrlReturnsCorrectValue() {
        String expectedUrl = "http://example.com";
        CircuitBreakerOpenException exception = new CircuitBreakerOpenException(expectedUrl);

        String actualUrl = exception.getTargetUrl();

        assertEquals(expectedUrl, actualUrl, "The targetUrl should match the expected value");
    }

    @Test
    void testExceptionMessageContainsTargetUrl() {
        String targetUrl = "http://example.com";
        CircuitBreakerOpenException exception = new CircuitBreakerOpenException(targetUrl);

        String actualMessage = exception.getMessage();

        assertNotNull(actualMessage, "Exception message should not be null");
        assertEquals("Circuit Breaker is OPEN for target URL: " + targetUrl + ", try next instance.", actualMessage, "Exception message should match the expected format");
    }

}