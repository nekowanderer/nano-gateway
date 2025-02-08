package idv.clu.api.resource;

import idv.clu.api.client.ApiInvoker;
import idv.clu.api.client.SimpleApiResource;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RoutingResourceTest {

    @Test
    void testSimpleApiRoute_SuccessfulResponse() throws Exception {
        ApiInvoker mockApiInvoker = mock(ApiInvoker.class);
        RoutingResource routingResource = new RoutingResource();
        routingResource.apiInvoker = mockApiInvoker;

        String requestPayload = "{\"key\": \"value\"}";
        Response mockResponse = Response.ok("{\"response\": \"success\"}", "application/json").build();

        when(mockApiInvoker.invokePost(eq(SimpleApiResource.getSimpleApiEchoUrl()), anyString())).thenReturn(mockResponse);

        Response response = routingResource.simpleApiEchoRoute(requestPayload);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"response\": \"success\"}", response.getEntity().toString());

        verify(mockApiInvoker, times(1)).invokePost(eq(SimpleApiResource.getSimpleApiEchoUrl()), eq(requestPayload));
    }

    @Test
    void testSimpleApiRoute_InternalServerError() throws Exception {
        ApiInvoker mockApiInvoker = mock(ApiInvoker.class);
        RoutingResource routingResource = new RoutingResource();
        routingResource.apiInvoker = mockApiInvoker;

        String requestPayload = "{\"key\": \"value\"}";

        when(mockApiInvoker.invokePost(eq(SimpleApiResource.getSimpleApiEchoUrl()), anyString()))
                .thenThrow(new RuntimeException("Target instance error"));

        Response response = routingResource.simpleApiEchoRoute(requestPayload);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"error\": \"Error occurred while calling target instance.\"}", response.getEntity().toString());

        verify(mockApiInvoker, times(1)).invokePost(eq(SimpleApiResource.getSimpleApiEchoUrl()), eq(requestPayload));
    }

    @Test
    void testSimpleApiDelayRoute_SuccessfulResponse() throws Exception {
        ApiInvoker mockApiInvoker = mock(ApiInvoker.class);
        RoutingResource routingResource = new RoutingResource();
        routingResource.apiInvoker = mockApiInvoker;

        String requestPayload = "{\"key\": \"value\"}";
        Response mockResponse = Response.ok("{\"response\": \"delayed success\"}", "application/json").build();

        when(mockApiInvoker.invokePost(eq(SimpleApiResource.getSimpleApiDelayUrl()), anyString())).thenReturn(mockResponse);

        Response response = routingResource.simpleApiDelayRoute(requestPayload);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"response\": \"delayed success\"}", response.getEntity().toString());

        verify(mockApiInvoker, times(1)).invokePost(eq(SimpleApiResource.getSimpleApiDelayUrl()), eq(requestPayload));
    }

    @Test
    void testSimpleApiDelayRoute_InternalServerError() throws Exception {
        ApiInvoker mockApiInvoker = mock(ApiInvoker.class);
        RoutingResource routingResource = new RoutingResource();
        routingResource.apiInvoker = mockApiInvoker;

        String requestPayload = "{\"key\": \"value\"}";

        when(mockApiInvoker.invokePost(eq(SimpleApiResource.getSimpleApiDelayUrl()), anyString()))
                .thenThrow(new RuntimeException("Target instance error"));

        Response response = routingResource.simpleApiDelayRoute(requestPayload);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"error\": \"Error occurred while calling target instance.\"}", response.getEntity().toString());

        verify(mockApiInvoker, times(1)).invokePost(eq(SimpleApiResource.getSimpleApiDelayUrl()), eq(requestPayload));
    }

}