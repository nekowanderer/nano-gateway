package idv.clu.api.client;

import idv.clu.api.common.RoutingConfig;
import jakarta.ws.rs.core.Response;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OkHttpClientProviderTest {

    @Mock
    private RoutingConfig routingConfig;

    @Mock
    private OkHttpClient okHttpClient;

    @InjectMocks
    private OkHttpClientProvider okHttpClientProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitForSuccessfulInitialization() {
        when(routingConfig.getSimpleApiInstances()).thenReturn(Arrays.asList("http://localhost:8080", "http://localhost:8081"));
        okHttpClientProvider.init();

        assertEquals(2, okHttpClientProvider.availableInstances.size());
        assertEquals(new AtomicInteger(0).get(), okHttpClientProvider.roundRobinIndex.get(), "Initial roundRobinIndex should be 0");
    }

    @Test
    void testInitForNoInstancesConfigured() {
        when(routingConfig.getSimpleApiInstances()).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, okHttpClientProvider::init);
    }

    @Test
    void testSendGetRequestWithSuccessful() throws IOException {
        when(routingConfig.getSimpleApiInstances()).thenReturn(List.of("http://localhost:8080"));
        okHttpClientProvider.init();

        String path = "/api/test";
        ResponseBody mockBody = mock(ResponseBody.class);
        when(mockBody.string()).thenReturn("{}");
        when(mockBody.contentType()).thenReturn(MediaType.get("application/json"));
        okhttp3.Response mockResponse = new okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost:8080" + path).build())
                .body(mockBody)
                .build();

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);

        when(okHttpClient.newCall(any())).thenReturn(mockCall);

        Response jakartaResponse = okHttpClientProvider.sendGetRequest(path);

        assertEquals(200, jakartaResponse.getStatus());
        assertEquals("{}", jakartaResponse.getEntity().toString());
    }

    @Test
    void testSendGetRequestWithError() throws IOException {
        when(routingConfig.getSimpleApiInstances()).thenReturn(List.of("http://localhost:8080"));
        okHttpClientProvider.init();

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));

        when(okHttpClient.newCall(any())).thenReturn(mockCall);

        assertThrows(IOException.class, () -> okHttpClientProvider.sendGetRequest("/api/test"));
    }

    @Test
    void testSendPostRequestWithSuccessful() throws IOException {
        when(routingConfig.getSimpleApiInstances()).thenReturn(List.of("http://localhost:8080"));
        okHttpClientProvider.init();

        String path = "/api/test";
        String payload = "{\"key\":\"value\"}";

        ResponseBody mockBody = mock(ResponseBody.class);
        when(mockBody.string()).thenReturn("{\"result\":\"success\"}");
        when(mockBody.contentType()).thenReturn(MediaType.get("application/json"));
        okhttp3.Response mockResponse = new okhttp3.Response.Builder()
                .code(201)
                .message("Created")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost:8080" + path).build())
                .body(mockBody)
                .build();

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);

        when(okHttpClient.newCall(any())).thenReturn(mockCall);

        Response jakartaResponse = okHttpClientProvider.sendPostRequest(path, payload);

        assertEquals(201, jakartaResponse.getStatus());
        assertEquals("{\"result\":\"success\"}", jakartaResponse.getEntity().toString());
    }

    @Test
    void testSendPostRequestWithError() throws IOException {
        when(routingConfig.getSimpleApiInstances()).thenReturn(List.of("http://localhost:8080"));
        okHttpClientProvider.init();

        String path = "/api/test";
        String payload = "{\"key\":\"value\"}";

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));

        when(okHttpClient.newCall(any())).thenReturn(mockCall);

        assertThrows(IOException.class, () -> okHttpClientProvider.sendPostRequest(path, payload));
    }

}