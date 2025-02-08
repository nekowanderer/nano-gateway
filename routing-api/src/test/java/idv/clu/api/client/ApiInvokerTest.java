package idv.clu.api.client;

import idv.clu.api.circuitbreaker.CircuitBreaker;
import jakarta.ws.rs.core.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ApiInvokerTest {

    @Mock
    private OkHttpClientProvider httpClientProvider;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private ApiInvoker apiInvoker;

    public ApiInvokerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void invokeGet_SuccessfulResponse() throws Exception {
        HttpResult httpResult = mock(HttpResult.class);
        Response expectedResponse = Response.ok().build();
        when(httpClientProvider.sendGetRequest(anyString())).thenReturn(httpResult);
        when(httpResult.hasResponse()).thenReturn(true);
        when(httpResult.getResponse()).thenReturn(expectedResponse);
        when(httpResult.getEndpoint()).thenReturn("https://example.com");

        Response actualResponse = apiInvoker.invokeGet("/test");

        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        verify(circuitBreaker).reportSuccess("https://example.com");
    }

    @Test
    void invokeGet_FailedResponseThenFallback() throws Exception {
        HttpResult httpResult = mock(HttpResult.class);
        Exception exception = new Exception("Test exception");
        when(httpClientProvider.sendGetRequest(anyString())).thenReturn(httpResult);
        when(httpResult.hasResponse()).thenReturn(false);
        when(httpResult.getException()).thenReturn(exception);
        when(httpResult.getEndpoint()).thenReturn("https://example.com");

        Response fallbackResponse = apiInvoker.invokeGet("/test");

        assertEquals(503, fallbackResponse.getStatus());
        verify(circuitBreaker).reportFailure("https://example.com");
    }

    @Test
    void invokeGet_CircuitBreakerOpenRetries() throws Exception {
        when(httpClientProvider.sendGetRequest(anyString())).thenThrow(new CircuitBreakerOpenException("https://example.com"));

        Response fallbackResponse = apiInvoker.invokeGet("/test");

        assertEquals(503, fallbackResponse.getStatus());
        verify(circuitBreaker, never()).reportSuccess(anyString());
    }

    @Test
    void invokeGet_UnexpectedException() throws Exception {
        when(httpClientProvider.sendGetRequest(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        Response fallbackResponse = apiInvoker.invokeGet("/test");

        assertEquals(503, fallbackResponse.getStatus());
        verify(circuitBreaker).reportFailure(null);
    }

}