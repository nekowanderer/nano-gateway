package idv.clu.api.core;

import idv.clu.api.circuitbreaker.CircuitBreaker;
import idv.clu.api.client.exception.CircuitBreakerOpenException;
import idv.clu.api.client.model.HttpResult;
import idv.clu.api.executor.HttpRequestExecutor;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ApiInvokerTest {

    @Mock
    private HttpRequestExecutor httpRequestExecutor;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private ApiInvoker apiInvoker;

    public ApiInvokerTest() {
        MockitoAnnotations.openMocks(this);
        apiInvoker.maxRetryCount = 3;
    }

    @Test
    void invokeGetSuccessfulResponse() throws Exception {
        HttpResult httpResult = mock(HttpResult.class);
        Response expectedResponse = Response.ok().build();
        when(httpRequestExecutor.sendGetRequest(anyString())).thenReturn(httpResult);
        when(httpResult.hasResponse()).thenReturn(true);
        when(httpResult.getResponse()).thenReturn(expectedResponse);
        when(httpResult.getEndpoint()).thenReturn("https://example.com");

        Response actualResponse = apiInvoker.invokeGet("/test");

        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        verify(circuitBreaker).reportSuccess("https://example.com");
    }

    @Test
    void invokeGetFailedResponseThenFallback() throws Exception {
        HttpResult httpResult = mock(HttpResult.class);
        Exception exception = new Exception("Test exception");
        when(httpRequestExecutor.sendGetRequest(anyString())).thenReturn(httpResult);
        when(httpResult.hasResponse()).thenReturn(false);
        when(httpResult.getException()).thenReturn(exception);
        when(httpResult.getEndpoint()).thenReturn("https://example.com");

        Response fallbackResponse = apiInvoker.invokeGet("/test");

        assertEquals(503, fallbackResponse.getStatus());
        verify(circuitBreaker).reportFailure("https://example.com");
    }

    @Test
    void invokeGetCircuitBreakerOpenRetries() throws Exception {
        when(httpRequestExecutor.sendGetRequest(anyString())).thenThrow(new CircuitBreakerOpenException("https://example.com"));

        Response fallbackResponse = apiInvoker.invokeGet("/test");

        assertEquals(503, fallbackResponse.getStatus());
        verify(circuitBreaker, never()).reportSuccess(anyString());
    }

    @Test
    void invokeGetUnexpectedException() throws Exception {
        when(httpRequestExecutor.sendGetRequest(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        Response fallbackResponse = apiInvoker.invokeGet("/test");

        assertEquals(503, fallbackResponse.getStatus());
        verify(circuitBreaker).reportFailure(null);
    }

}