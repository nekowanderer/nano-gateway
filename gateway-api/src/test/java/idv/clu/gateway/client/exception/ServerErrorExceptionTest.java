package idv.clu.gateway.client.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author clu
 */
class ServerErrorExceptionTest {

    @Test
    void testGetStatusCodeReturnsCorrectValue() {
        String errorMessage = "An error occurred on the server.";
        int statusCode = 500;

        ServerErrorException exception = new ServerErrorException(errorMessage, statusCode);

        assertEquals(statusCode, exception.getStatusCode(), "The status code should match the value provided.");
    }

    @Test
    void testGetStatusCodeForDifferentStatusCode() {
        String errorMessage = "Another server error.";
        int statusCode = 503;

        ServerErrorException exception = new ServerErrorException(errorMessage, statusCode);

        assertEquals(statusCode, exception.getStatusCode(), "The status code should match the provided HTTP status code.");
    }

}