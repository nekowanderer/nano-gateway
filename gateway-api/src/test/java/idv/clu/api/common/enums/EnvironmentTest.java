package idv.clu.api.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvironmentTest {

    @Test
    void testIsTestEnvironmentWithDev() {
        String environment = "DEV";

        boolean result = Environment.isTestEnvironment(environment);

        assertTrue(result, "Expected DEV to be identified as a test environment.");
    }

    @Test
    void testIsTestEnvironmentWithContainer() {
        String environment = "CONTAINER";

        boolean result = Environment.isTestEnvironment(environment);

        assertTrue(result, "Expected CONTAINER to be identified as a test environment.");
    }

    @Test
    void testIsTestEnvironmentWithProduction() {
        String environment = "PRODUCTION";

        boolean result = Environment.isTestEnvironment(environment);

        assertFalse(result, "Expected PRODUCTION not to be identified as a test environment.");
    }

    @Test
    void testIsTestEnvironmentWithLowercaseInput() {
        String environment = "dev";

        boolean result = Environment.isTestEnvironment(environment);

        assertTrue(result, "Expected 'dev' to be identified as a test environment.");
    }

    @Test
    void testIsTestEnvironmentWithInvalidEnvironment() {
        String environment = "INVALID";

        assertThrows(IllegalArgumentException.class,
                () -> Environment.isTestEnvironment(environment),
                "Expected an IllegalArgumentException for an invalid environment.");
    }

    @Test
    void testIsTestEnvironmentWithNullInput() {
        String environment = null;

        assertThrows(NullPointerException.class,
                () -> Environment.isTestEnvironment(environment),
                "Expected a NullPointerException for null input.");
    }

    @Test
    void testIsContainerEnvironmentWithContainer() {
        String environment = "CONTAINER";

        boolean result = Environment.isContainerEnvironment(environment);

        assertTrue(result, "Expected CONTAINER to be identified as a container environment.");
    }

    @Test
    void testIsContainerEnvironmentWithDev() {
        String environment = "DEV";

        boolean result = Environment.isContainerEnvironment(environment);

        assertFalse(result, "Expected DEV not to be identified as a container environment.");
    }

    @Test
    void testIsContainerEnvironmentWithInvalidEnvironment() {
        String environment = "INVALID";

        assertThrows(IllegalArgumentException.class,
                () -> Environment.isContainerEnvironment(environment),
                "Expected an IllegalArgumentException for an invalid environment.");
    }

    @Test
    void testIsContainerEnvironmentWithLowercaseInput() {
        String environment = "container";

        boolean result = Environment.isContainerEnvironment(environment);

        assertTrue(result, "Expected 'container' to be identified as a container environment.");
    }

    @Test
    void testIsContainerEnvironmentWithNullInput() {
        String environment = null;

        assertThrows(NullPointerException.class,
                () -> Environment.isContainerEnvironment(environment),
                "Expected a NullPointerException for null input.");
    }

}