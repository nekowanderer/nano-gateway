package idv.clu.api.client.model;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpResultTest {

    @Test
    void testHasResponseWhenResponseIsPresentAndNoException() {
        String endpoint = "http://example.com";
        Response mockResponse = mock(Response.class);

        HttpResult httpResult = new HttpResult(endpoint, mockResponse);

        assertTrue(httpResult.hasResponse(), "Expect hasResponse() to return true when a valid response exists without exceptions.");
    }

    @Test
    void testGetEndpointWhenResponseIsPresent() {
        String endpoint = "http://example.com";
        Response mockResponse = mock(Response.class);

        HttpResult httpResult = new HttpResult(endpoint, mockResponse);

        assertEquals(endpoint, httpResult.getEndpoint(), "Expect getEndpoint() to return the correct endpoint when response is present.");
    }

    @Test
    void testGetEndpointWhenExceptionIsPresent() {
        String endpoint = "http://example.com";
        Exception exception = new Exception("Test exception");

        HttpResult httpResult = new HttpResult(endpoint, exception);

        assertEquals(endpoint, httpResult.getEndpoint(), "Expect getEndpoint() to return the correct endpoint when exception is present.");
    }

    @Test
    void testHasResponseWhenResponseIsNullAndExceptionIsPresent() {
        String endpoint = "http://example.com";
        Exception exception = new Exception("Test exception");

        HttpResult httpResult = new HttpResult(endpoint, exception);

        assertFalse(httpResult.hasResponse(), "Expect hasResponse() to return false when response is null and exception is present.");
    }

    @Test
    void testHasResponseWhenResponseIsNullAndNoException() {
        String endpoint = "http://example.com";

        HttpResult httpResult = new HttpResult(endpoint, (Response) null);

        assertFalse(httpResult.hasResponse(), "Expect hasResponse() to return false when response is null and no exception exists.");
    }

    @Test
    void testHasResponseWhenResponseIsPresentAndExceptionIsAlsoPresent() {
        String endpoint = "http://example.com";
        Response mockResponse = mock(Response.class);
        Exception exception = new Exception("Test exception");

        HttpResult httpResult = new HttpResult(endpoint, mockResponse);
        HttpResult httpResultWithException = new HttpResult(endpoint, exception);

        assertTrue(httpResult.hasResponse(), "Expect hasResponse() to return true when only a valid response exists.");
        assertFalse(httpResultWithException.hasResponse(), "Expect hasResponse() to return false when exception is present.");
    }

}