package idv.clu.api.strategy.routing;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author clu
 */
public class RoundRobinRoutingStrategyTest {

    @Test
    void testGetNextTargetUrl() {
        RoutingStrategy routingStrategy = new RoundRobinRoutingStrategy();
        List<String> instances = Arrays.asList("http://instance1", "http://instance2", "http://instance3");

        assertEquals("http://instance1", routingStrategy.getNextTargetUrl(instances));
        assertEquals("http://instance2", routingStrategy.getNextTargetUrl(instances));
        assertEquals("http://instance3", routingStrategy.getNextTargetUrl(instances));
        assertEquals("http://instance1", routingStrategy.getNextTargetUrl(instances));
    }

    @Test
    void testGetNextTargetUrlEmptyInstances() {
        RoutingStrategy routingStrategy = new RoundRobinRoutingStrategy();
        List<String> emptyInstances = List.of();

        assertThrows(IllegalArgumentException.class, () -> routingStrategy.getNextTargetUrl(emptyInstances));
    }

}
