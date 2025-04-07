package idv.clu.gateway.client.provider;

import idv.clu.gateway.common.RoutingConfig;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestClientProviderTest {

    @Test
    void testInitCreatesClientsSuccessfullyWithCreateClientMocked() {
        final String instance1 = "http://simple_api_1:8080";
        final String instance2 = "http://simple_api_2:8080";
        final String instance3 = "http://simple_api_3:8080";

        final RoutingConfig mockRoutingConfig = mock(RoutingConfig.class);
        final Set<String> mockInstances = new HashSet<>(List.of(instance1, instance2, instance3));
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
        assertTrue(spyProvider.clients.contains(mockClient1), "Clients list should contain mockClient1");
        assertTrue(spyProvider.clients.contains(mockClient2), "Clients list should contain mockClient2");
        assertTrue(spyProvider.clients.contains(mockClient3), "Clients list should contain mockClient3");

        verify(spyProvider, times(1)).createClient(instance1);
        verify(spyProvider, times(1)).createClient(instance2);
        verify(spyProvider, times(1)).createClient(instance3);

        SimpleApiClient client1 = spyProvider.getNextClient();
        SimpleApiClient client2 = spyProvider.getNextClient();
        SimpleApiClient client3 = spyProvider.getNextClient();
        SimpleApiClient client4 = spyProvider.getNextClient();

        List<SimpleApiClient> expectedInstances = List.of(client1, client2, client3, client4);

        assertTrue(expectedInstances.contains(client1), "Client1 should be one of the mock clients");
        assertTrue(expectedInstances.contains(client2), "Client2 should be one of the mock clients");
        assertTrue(expectedInstances.contains(client3), "Client3 should be one of the mock clients");
        assertTrue(expectedInstances.contains(client4), "Client4 should be one of the mock clients");
    }

    @Test
    void testInitThrowsExceptionWhenNoInstancesConfigured() {
        final RoutingConfig routingConfig = mock(RoutingConfig.class);
        when(routingConfig.getAvailableInstances()).thenReturn(Collections.emptySet());

        final RestClientProvider provider = new RestClientProvider();
        provider.routingConfig = routingConfig;

        IllegalStateException exception = assertThrows(IllegalStateException.class, provider::init);
        assertEquals("No simple api instances configured.", exception.getMessage());
    }

}
