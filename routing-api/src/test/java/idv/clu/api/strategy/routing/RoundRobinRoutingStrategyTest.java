package idv.clu.api.strategy.routing;

import idv.clu.api.client.enums.SimpleApiResource;
import idv.clu.api.common.RoutingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author clu
 */
public class RoundRobinRoutingStrategyTest {

    private final static List<String> MOCK_INSTANCES_URLS =
            List.of("http://example1.com", "http://example2.com", "http://example3.com");
    private final static String MOCK_ROUTING_PATH = SimpleApiResource.getSimpleApiEchoUrl();
    private final static String EXPECTED_PATH_SUFFIX = "/simple-api/rest_resource/echo";


    @Mock
    private RoutingConfig routingConfig;

    @InjectMocks
    private RoundRobinRoutingStrategy routingStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetNextTargetUrl() {
        when(routingConfig.getAvailableInstances()).thenReturn(MOCK_INSTANCES_URLS);
        routingStrategy.init();

        assertEquals(MOCK_INSTANCES_URLS.get(0).concat(EXPECTED_PATH_SUFFIX), routingStrategy.getNextTargetUrl(MOCK_ROUTING_PATH));
        assertEquals(MOCK_INSTANCES_URLS.get(1).concat(EXPECTED_PATH_SUFFIX), routingStrategy.getNextTargetUrl(MOCK_ROUTING_PATH));
        assertEquals(MOCK_INSTANCES_URLS.get(2).concat(EXPECTED_PATH_SUFFIX), routingStrategy.getNextTargetUrl(MOCK_ROUTING_PATH));
        assertEquals(MOCK_INSTANCES_URLS.get(0).concat(EXPECTED_PATH_SUFFIX), routingStrategy.getNextTargetUrl(MOCK_ROUTING_PATH));
    }

    @Test
    void testGetNextTargetUrlEmptyInstances() {
        when(routingConfig.getAvailableInstances()).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> routingStrategy.init());
    }

    @Test
    void testGetNextTargetUrlWhenAvailableInstancesIsEmpty() {
        RoundRobinRoutingStrategy routingStrategy = new RoundRobinRoutingStrategy();
        routingStrategy.availableInstances = Collections.emptyList();

        assertThrows(IllegalArgumentException.class, () -> routingStrategy.getNextTargetUrl(""));
    }

    @Test
    void testGetNextTargetUrlWhenAvailableInstancesIsNull() {
        RoundRobinRoutingStrategy routingStrategy = new RoundRobinRoutingStrategy();

        assertThrows(IllegalArgumentException.class, () -> routingStrategy.getNextTargetUrl(""));
    }

}
