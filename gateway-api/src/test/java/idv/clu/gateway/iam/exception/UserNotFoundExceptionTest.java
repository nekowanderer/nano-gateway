package idv.clu.gateway.iam.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserNotFoundExceptionTest {

    @Test
    void testConstructorAndGetRealm() {
        String testRealm = "test-realm";

        UserNotFoundException exception = new UserNotFoundException(testRealm);

        assertEquals(testRealm, exception.getRealm(), "The realm should match the one provided in the constructor");

        String expectedMessage = String.format("User not found in realm: %s", testRealm);
        assertEquals(expectedMessage, exception.getMessage(), "The message should be formatted correctly");
    }

}