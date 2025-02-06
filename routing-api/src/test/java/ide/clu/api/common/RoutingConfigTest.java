package ide.clu.api.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutingConfigTest {

    @Test
    void testGetSimpleApiInstancesWithValidInput() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "instance1,instance2,instance3";

        List<String> result = routingConfig.getSimpleApiInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Expected 3 instances in the list");
        assertEquals("instance1", result.get(0), "First instance mismatch");
        assertEquals("instance2", result.get(1), "Second instance mismatch");
        assertEquals("instance3", result.get(2), "Third instance mismatch");
    }

    @Test
    void testGetSimpleApiInstancesWithSingleInstance() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "singleInstance";

        List<String> result = routingConfig.getSimpleApiInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Expected 1 instance in the list");
        assertEquals("singleInstance", result.get(0), "Instance value mismatch");
    }

    @Test
    void testGetSimpleApiInstancesWithEmptyString() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "";

        List<String> result = routingConfig.getSimpleApiInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Expected 1 element in the list for empty string");
        assertEquals("", result.get(0), "Element value mismatch for empty string");
    }

    @Test
    void testGetSimpleApiInstancesWithWhitespace() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "   ";

        List<String> result = routingConfig.getSimpleApiInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Expected 1 element in the list for whitespace");
        assertEquals("   ", result.get(0), "Element value mismatch for whitespace");
    }

    @Test
    void testGetSimpleApiInstancesWithTrailingCommas() {
        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.apiInstances = "instance1,instance2,";

        List<String> result = routingConfig.getSimpleApiInstances();

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Expected 2 elements");
        assertEquals("instance1", result.get(0), "First instance mismatch");
        assertEquals("instance2", result.get(1), "Second instance mismatch");
    }
}