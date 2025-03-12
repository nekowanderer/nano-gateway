package idv.clu.api.common;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoutingConfigTest {

    @Test
    void testGetAvailableInstancesWithValidInput() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "instance1,instance2,instance3";

        Set<String> result = routingConfig.getAvailableInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Expected 3 instances in the set");
        assertTrue(result.contains("instance1"), "Set should contain instance1");
        assertTrue(result.contains("instance2"), "Set should contain instance2");
        assertTrue(result.contains("instance3"), "Set should contain instance3");
    }

    @Test
    void testGetAvailableInstancesWithSingleInstance() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "singleInstance";

        Set<String> result = routingConfig.getAvailableInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Expected 1 instance in the set");
        assertTrue(result.contains("singleInstance"), "Set should contain singleInstance");
    }

    @Test
    void testGetAvailableInstancesWithEmptyString() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "";

        Set<String> result = routingConfig.getAvailableInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Expected 1 element in the set for empty string");
        assertTrue(result.contains(""), "Set should contain empty string");
    }

    @Test
    void testGetAvailableInstancesWithWhitespace() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "   ";

        Set<String> result = routingConfig.getAvailableInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Expected 1 element in the set for whitespace");
        assertTrue(result.contains("   "), "Set should contain whitespace");
    }

    @Test
    void testGetAvailableInstancesWithTrailingCommas() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "instance1,instance2,";

        Set<String> result = routingConfig.getAvailableInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Expected 2 elements");
        assertTrue(result.contains("instance1"), "Set should contain instance1");
        assertTrue(result.contains("instance2"), "Set should contain instance2");
    }

}
