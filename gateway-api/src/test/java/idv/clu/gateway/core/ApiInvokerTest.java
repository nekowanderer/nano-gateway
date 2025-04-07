package idv.clu.gateway.core;

import idv.clu.gateway.circuitbreaker.CircuitBreaker;
import idv.clu.gateway.client.exception.CircuitBreakerOpenException;
import idv.clu.gateway.client.exception.ClientTimeoutException;
import idv.clu.gateway.client.exception.ServerErrorException;
import idv.clu.gateway.client.model.HttpResult;
import idv.clu.gateway.executor.HttpRequestExecutor;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void testInvokeGetSuccessfulResponse() throws Exception {
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
    void testInvokePostSuccessfulResponse() throws Exception {
        HttpResult httpResult = mock(HttpResult.class);
        Response expectedResponse = Response.ok().build();
        when(httpRequestExecutor.sendPostRequest(anyString(), anyString())).thenReturn(httpResult);
        when(httpResult.hasResponse()).thenReturn(true);
        when(httpResult.getResponse()).thenReturn(expectedResponse);
        when(httpResult.getEndpoint()).thenReturn("https://example.com");

        Response actualResponse = apiInvoker.invokePost("/test", "test payload");

        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        verify(circuitBreaker).reportSuccess("https://example.com");
    }

    @Test
    void testInvokePostWithClientTimeoutExceptionAndFallbackResponse() throws Exception {
        String path = "/test";
        String payload = "{\"key\":\"value\"}";
        String expectedErrorMessage = "Timeout everytime.";

        when(httpRequestExecutor.sendPostRequest(path, payload))
                .thenThrow(new ClientTimeoutException(expectedErrorMessage));

        ClientTimeoutException clientTimeoutException =
                assertThrows(ClientTimeoutException.class, () -> apiInvoker.invokePost(path, payload));

        assertEquals(expectedErrorMessage, clientTimeoutException.getMessage());

        verify(httpRequestExecutor, times(3)).sendPostRequest(path, payload);
    }

    @Test
    void testInvokePostWithServerErrorExceptionAndFallbackResponse() throws Exception {
        String path = "/test";
        String payload = "{\"key\":\"value\"}";
        String expectedErrorMessage = "Timeout everytime.";
        int expectedStatusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        when(httpRequestExecutor.sendPostRequest(path, payload))
                .thenThrow(new ServerErrorException(expectedErrorMessage, expectedStatusCode));

        ServerErrorException serverErrorException =
                assertThrows(ServerErrorException.class, () -> apiInvoker.invokePost(path, payload));

        assertEquals(expectedErrorMessage, serverErrorException.getMessage());
        assertEquals(expectedStatusCode, serverErrorException.getStatusCode());

        verify(httpRequestExecutor, times(3)).sendPostRequest(path, payload);
    }

    @Test
    void testInvokeGetFailedResponseThenFallback() throws Exception {
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
    void testInvokeGetThrowCircuitBreakerOpenRetries() throws Exception {
        String expectedTargetUrl = "https://example.com/test";
        when(httpRequestExecutor.sendGetRequest(anyString())).thenThrow(new CircuitBreakerOpenException(expectedTargetUrl));

        CircuitBreakerOpenException circuitBreakerOpenException =
                assertThrows(CircuitBreakerOpenException.class, () -> apiInvoker.invokeGet("/test"));

        assertEquals(expectedTargetUrl, circuitBreakerOpenException.getTargetUrl());
        verify(circuitBreaker, never()).reportSuccess(anyString());
    }

    @Test
    void testInvokeGetThrowUnexpectedException() throws Exception {
        when(httpRequestExecutor.sendGetRequest(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        Response fallbackResponse = apiInvoker.invokeGet("/test");

        assertEquals(503, fallbackResponse.getStatus());
        verify(circuitBreaker).reportFailure(null);
    }

}