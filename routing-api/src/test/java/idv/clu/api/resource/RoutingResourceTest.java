package idv.clu.api.resource;

import idv.clu.api.client.OkHttpClientProvider;
import idv.clu.api.client.SimpleApiResource;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RoutingResourceTest {

    @Test
    void testSimpleApiRoute_SuccessfulResponse() throws Exception {
        OkHttpClientProvider mockClientProvider = mock(OkHttpClientProvider.class);
        RoutingResource routingResource = new RoutingResource();
        routingResource.okHttpClientProvider = mockClientProvider;

        String requestPayload = "{\"key\": \"value\"}";
        Response mockResponse = Response.ok("{\"response\": \"success\"}", "application/json").build();

        when(mockClientProvider.sendPostRequest(eq(SimpleApiResource.getSimpleApiEchoUrl()), anyString())).thenReturn(mockResponse);

        Response response = routingResource.simpleApiRoute(requestPayload);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"response\": \"success\"}", response.getEntity().toString());

        verify(mockClientProvider, times(1)).sendPostRequest(eq(SimpleApiResource.getSimpleApiEchoUrl()), eq(requestPayload));
    }

    @Test
    void testSimpleApiRoute_InternalServerError() throws Exception {
        OkHttpClientProvider mockClientProvider = mock(OkHttpClientProvider.class);
        RoutingResource routingResource = new RoutingResource();
        routingResource.okHttpClientProvider = mockClientProvider;

        String requestPayload = "{\"key\": \"value\"}";

        when(mockClientProvider.sendPostRequest(eq(SimpleApiResource.getSimpleApiEchoUrl()), anyString()))
                .thenThrow(new RuntimeException("Target instance error"));

        Response response = routingResource.simpleApiRoute(requestPayload);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"error\": \"Error occurred while calling target instance.\"}", response.getEntity().toString());

        verify(mockClientProvider, times(1)).sendPostRequest(eq(SimpleApiResource.getSimpleApiEchoUrl()), eq(requestPayload));
    }

}