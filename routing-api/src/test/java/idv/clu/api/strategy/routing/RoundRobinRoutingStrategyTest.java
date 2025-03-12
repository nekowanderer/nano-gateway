package idv.clu.api.strategy.routing;

import idv.clu.api.client.enums.SimpleApiResource;
import idv.clu.api.common.RoutingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author clu
 */
public class RoundRobinRoutingStrategyTest {

    private final static Set<String> MOCK_INSTANCES_URLS =
            new HashSet<>(List.of("http://example1.com", "http://example2.com", "http://example3.com"));
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

        String url1 = routingStrategy.getNextTargetUrl(MOCK_ROUTING_PATH);
        String url2 = routingStrategy.getNextTargetUrl(MOCK_ROUTING_PATH);
        String url3 = routingStrategy.getNextTargetUrl(MOCK_ROUTING_PATH);
        String url4 = routingStrategy.getNextTargetUrl(MOCK_ROUTING_PATH);

        verifyValidUrl(url1);
        verifyValidUrl(url2);
        verifyValidUrl(url3);
        verifyValidUrl(url4);
    }

    private void verifyValidUrl(String url) {
        boolean isValid = false;
        for (String instance : MOCK_INSTANCES_URLS) {
            if (url.equals(instance + EXPECTED_PATH_SUFFIX)) {
                isValid = true;
                break;
            }
        }
        assertTrue(isValid, "URL should be a valid combination of a mock instance and the expected suffix");
    }

    @Test
    void testGetNextTargetUrlEmptyInstances() {
        when(routingConfig.getAvailableInstances()).thenReturn(Collections.emptySet());

        assertThrows(IllegalStateException.class, () -> routingStrategy.init());
    }

    @Test
    void testGetNextTargetUrlWhenAvailableInstancesIsEmpty() {
        RoundRobinRoutingStrategy routingStrategy = new RoundRobinRoutingStrategy();
        routingStrategy.availableInstances = Collections.emptySet();

        assertThrows(IllegalArgumentException.class, () -> routingStrategy.getNextTargetUrl(""));
    }

    @Test
    void testGetNextTargetUrlWhenAvailableInstancesIsNull() {
        RoundRobinRoutingStrategy routingStrategy = new RoundRobinRoutingStrategy();

        assertThrows(IllegalArgumentException.class, () -> routingStrategy.getNextTargetUrl(""));
    }

}
