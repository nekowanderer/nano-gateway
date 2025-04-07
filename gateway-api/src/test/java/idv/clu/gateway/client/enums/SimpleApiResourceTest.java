package idv.clu.gateway.client.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleApiResourceTest {

    @Test
    void testBuildPathWithSingleResource() {
        SimpleApiResource resource = SimpleApiResource.BASE_URL;

        String result = SimpleApiResource.buildPath(resource);

        assertEquals("/simple-api", result);
    }

    @Test
    void testBuildPathWithMultipleResources() {
        SimpleApiResource resource1 = SimpleApiResource.BASE_URL;
        SimpleApiResource resource2 = SimpleApiResource.REST_RESOURCE;

        String result = SimpleApiResource.buildPath(resource1, resource2);

        assertEquals("/simple-api/rest_resource", result);
    }

    @Test
    void testBuildPathWithTrailingSlashBehaviour() {
        SimpleApiResource resource1 = SimpleApiResource.BASE_URL;
        SimpleApiResource resource2 = SimpleApiResource.REST_RESOURCE;
        SimpleApiResource resource3 = SimpleApiResource.ECHO_ENDPOINT;

        String result = SimpleApiResource.buildPath(resource1, resource2, resource3);

        assertEquals("/simple-api/rest_resource/echo", result);
    }

    @Test
    void testBuildPathWithNoResources() {
        String result = SimpleApiResource.buildPath();

        assertEquals("", result);
    }

    @Test
    void testGetSimpleApiEchoUrl() {
        String result = SimpleApiResource.getSimpleApiEchoUrl();

        assertEquals("/simple-api/rest_resource/echo", result);
    }

}