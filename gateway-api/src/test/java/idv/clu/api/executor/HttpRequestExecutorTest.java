package idv.clu.api.executor;

import idv.clu.api.circuitbreaker.CircuitBreaker;
import idv.clu.api.client.enums.SimpleApiResource;
import idv.clu.api.client.exception.CircuitBreakerOpenException;
import idv.clu.api.client.exception.ClientHttpRequestException;
import idv.clu.api.client.exception.ClientTimeoutException;
import idv.clu.api.client.exception.ServerErrorException;
import idv.clu.api.client.model.HttpResult;
import idv.clu.api.strategy.retry.RetryStrategy;
import idv.clu.api.strategy.routing.RoutingStrategy;
import jakarta.ws.rs.core.Response;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("HttpRequestExecutor Test Suite")
class HttpRequestExecutorTest {

    private final static MediaType APPLICATION_JSON = MediaType.get(jakarta.ws.rs.core.MediaType.APPLICATION_JSON);
    private final static List<String> MOCK_INSTANCES =
            List.of("http://example.com", "http://example2.com", "http://example3.com");
    private final static String ROUTING_PATH = SimpleApiResource.getSimpleApiEchoUrl();
    private final static String MOCK_VALID_REQUEST_PAYLOAD = "{\"game\": \"Mobile Legends\", \"gamerID\": \"GYUTDTE\", \"points\": 20}";
    private final static String MOCK_INVALID_REQUEST_PAYLOAD = "{\"invalid\": \"Mobile Legends\"\"}";

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private RetryStrategy retryStrategy;

    @Mock
    private RoutingStrategy routingStrategy;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private HttpRequestExecutor httpRequestExecutor;

    private final static String MOCK_TARGET_URL = MOCK_INSTANCES.get(0) + ROUTING_PATH;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(retryStrategy.executeWithRetries(any()))
                .thenAnswer(invocation -> {
                    Callable<Response> callable = invocation.getArgument(0);
                    return callable.call();
                });

        when(routingStrategy.getNextTargetUrl(anyString()))
                .thenAnswer(invocation -> MOCK_TARGET_URL);

        when(circuitBreaker.allowRequest(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("Test successful execution of a GET request")
    void testSendGetRequestWithSuccessful() throws Exception {
        int expectedStatusCode = Response.Status.OK.getStatusCode();
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(expectedStatusCode)
                .message("OK")
                .body(ResponseBody.create(MOCK_VALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();

        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(realResponse);

        HttpResult httpResult = httpRequestExecutor.sendGetRequest(ROUTING_PATH);

        assertNotNull(httpResult);
        assertEquals(expectedStatusCode, httpResult.getResponse().getStatus());
        assertEquals(MOCK_VALID_REQUEST_PAYLOAD, httpResult.getResponse().getEntity().toString());

        verify(okHttpClient, times(1)).newCall(any(Request.class));
        verify(callMock, times(1)).execute();
        verify(routingStrategy, times(1)).getNextTargetUrl(ROUTING_PATH);
        verify(circuitBreaker, times(1)).allowRequest(MOCK_TARGET_URL);
    }

    @Test
    @DisplayName("Test GET request with a failed response")
    void testSendGetRequestWithError() throws Exception {
        int expectedStatusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(expectedStatusCode)
                .message("Internal Server Error")
                .body(ResponseBody.create("Error occurred", APPLICATION_JSON))
                .build();

        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(realResponse);

        ServerErrorException serverErrorException =
                assertThrows(ServerErrorException.class, () -> httpRequestExecutor.sendGetRequest(ROUTING_PATH));

        assertEquals(expectedStatusCode, serverErrorException.getStatusCode());
        String expectedMessage = String.format("Received server error code (%d) from %s.",
                expectedStatusCode, MOCK_INSTANCES.get(0).concat(ROUTING_PATH));
        assertEquals(expectedMessage, serverErrorException.getMessage());

        verify(okHttpClient, times(1)).newCall(any(Request.class));
        verify(callMock, times(1)).execute();
        verify(routingStrategy, times(1)).getNextTargetUrl(ROUTING_PATH);
        verify(circuitBreaker, times(1)).allowRequest(MOCK_TARGET_URL);
    }

    @Test
    @DisplayName("Test successful execution of a POST request")
    void testSendPostRequestWithSuccessful() throws Exception {
        int expectedStatusCode = Response.Status.CREATED.getStatusCode();
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .post(RequestBody.create(MOCK_VALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(expectedStatusCode)
                .message("Created")
                .body(ResponseBody.create(MOCK_VALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();

        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(realResponse);

        HttpResult httpResult = httpRequestExecutor.sendPostRequest(ROUTING_PATH, MOCK_VALID_REQUEST_PAYLOAD);

        assertNotNull(httpResult);
        assertEquals(expectedStatusCode, httpResult.getResponse().getStatus());
        assertEquals(MOCK_VALID_REQUEST_PAYLOAD, httpResult.getResponse().getEntity().toString());

        verify(okHttpClient, times(1)).newCall(any(Request.class));
        verify(callMock, times(1)).execute();
        verify(routingStrategy, times(1)).getNextTargetUrl(ROUTING_PATH);
        verify(circuitBreaker, times(1)).allowRequest(MOCK_TARGET_URL);
    }

    @Test
    @DisplayName("Test POST request with a failed response")
    void testSendPostRequestWithError() throws Exception {
        int expectedStatusCode = Response.Status.NOT_FOUND.getStatusCode();
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .post(RequestBody.create(MOCK_INVALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(expectedStatusCode)
                .message("Bad Request")
                .body(ResponseBody.create(MOCK_INVALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();

        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(realResponse);

        HttpResult httpResult = httpRequestExecutor.sendPostRequest(ROUTING_PATH, MOCK_INVALID_REQUEST_PAYLOAD);

        assertNotNull(httpResult);
        assertEquals(expectedStatusCode, httpResult.getResponse().getStatus());
        assertEquals(MOCK_INVALID_REQUEST_PAYLOAD, httpResult.getResponse().getEntity().toString());

        verify(okHttpClient, times(1)).newCall(any(Request.class));
        verify(callMock, times(1)).execute();
        verify(routingStrategy, times(1)).getNextTargetUrl(ROUTING_PATH);
        verify(circuitBreaker, times(1)).allowRequest(MOCK_TARGET_URL);
    }

    @Test
    @DisplayName("Test handling of SocketTimeoutException during POST request")
    void testHandleSocketTimeoutException() throws Exception {
        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenThrow(new SocketTimeoutException("Socket timeout exception"));

        assertThrows(ClientTimeoutException.class,
                () -> httpRequestExecutor.sendPostRequest(ROUTING_PATH, MOCK_VALID_REQUEST_PAYLOAD));
        verify(circuitBreaker, times(1)).allowRequest(MOCK_TARGET_URL);
        verify(circuitBreaker, times(1)).reportFailure(MOCK_TARGET_URL);
    }

    @Test
    @DisplayName("Test handling of CircuitBreakerOpenException during POST request")
    void testHandleCircuitBreakerOpenException() {
        when(circuitBreaker.allowRequest(anyString())).thenReturn(false);

        assertThrows(CircuitBreakerOpenException.class,
                () -> httpRequestExecutor.sendPostRequest(ROUTING_PATH, MOCK_VALID_REQUEST_PAYLOAD));
    }

    @Test
    @DisplayName("Test handling of ClientHttpRequestException during POST request")
    void testHandleClientHttpRequestException() throws Exception {
        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenThrow(new IllegalStateException("IllegalState exception"));

        HttpResult httpResult = httpRequestExecutor.sendPostRequest(ROUTING_PATH, MOCK_VALID_REQUEST_PAYLOAD);

        assertNotNull(httpResult);
        assertInstanceOf(ClientHttpRequestException.class, httpResult.getException());
    }

    @Test
    void testToJakartaResponseWhenOkHttpResponseBodyIsNull() throws IOException {
        Request request = new Request.Builder()
                .url(MOCK_INSTANCES.get(0))
                .post(RequestBody.create(MOCK_INVALID_REQUEST_PAYLOAD, APPLICATION_JSON))
                .build();
        okhttp3.Response realResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(200)
                .build();

        Response actual = httpRequestExecutor.toJakartaResponse(realResponse);

        assertEquals(200, actual.getStatus());
        assertEquals("", actual.getEntity());
        assertEquals(jakarta.ws.rs.core.MediaType.APPLICATION_JSON.toString(), actual.getMediaType().toString());
    }

}
