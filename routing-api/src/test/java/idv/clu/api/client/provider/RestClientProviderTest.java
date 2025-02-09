package idv.clu.api.client.provider;

import idv.clu.api.common.RoutingConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestClientProviderTest {

    @Test
    void testInitCreatesClientsSuccessfullyWithCreateClientMocked() {
        final String instance1 = "http://simple_api_1:8080";
        final String instance2 = "http://simple_api_2:8080";
        final String instance3 = "http://simple_api_3:8080";

        final RoutingConfig mockRoutingConfig = mock(RoutingConfig.class);
        final List<String> mockInstances = List.of(instance1, instance2, instance3);
        when(mockRoutingConfig.getAvailableInstances()).thenReturn(mockInstances);

        final RestClientProvider spyProvider = spy(new RestClientProvider());
        spyProvider.routingConfig = mockRoutingConfig;

        final SimpleApiClient mockClient1 = mock(SimpleApiClient.class);
        final SimpleApiClient mockClient2 = mock(SimpleApiClient.class);
        final SimpleApiClient mockClient3 = mock(SimpleApiClient.class);

        doReturn(mockClient1).when(spyProvider).createClient(instance1);
        doReturn(mockClient2).when(spyProvider).createClient(instance2);
        doReturn(mockClient3).when(spyProvider).createClient(instance3);

        spyProvider.init();

        assertNotNull(spyProvider.clients);
        assertEquals(3, spyProvider.clients.size());
        assertSame(mockClient1, spyProvider.clients.get(0));
        assertSame(mockClient2, spyProvider.clients.get(1));
        assertSame(mockClient3, spyProvider.clients.get(2));

        verify(spyProvider, times(1)).createClient(instance1);
        verify(spyProvider, times(1)).createClient(instance2);
        verify(spyProvider, times(1)).createClient(instance3);

        assertEquals(mockClient1, spyProvider.getNextClient());
        assertEquals(mockClient2, spyProvider.getNextClient());
        assertEquals(mockClient3, spyProvider.getNextClient());
        assertEquals(mockClient1, spyProvider.getNextClient());
    }

    @Test
    void testInitThrowsExceptionWhenNoInstancesConfigured() {
        final RoutingConfig routingConfig = mock(RoutingConfig.class);
        when(routingConfig.getAvailableInstances()).thenReturn(List.of());

        final RestClientProvider provider = new RestClientProvider();
        provider.routingConfig = routingConfig;

        IllegalStateException exception = assertThrows(IllegalStateException.class, provider::init);
        assertEquals("No simple api instances configured.", exception.getMessage());
    }

}