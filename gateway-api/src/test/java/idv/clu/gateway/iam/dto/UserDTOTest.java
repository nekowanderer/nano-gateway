package idv.clu.gateway.iam.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author clu
 */
public class UserDTOTest {

    @Test
    public void testGetterAndSetter() {
        String testUsername = "testuser";
        String testPassword = "password123";
        String testFirstName = "Test";
        String testLastName = "User";
        String testEmail = "test.user@example.com";

        UserDTO userDTO = new UserDTO(testUsername, testPassword, testFirstName, testLastName, testEmail);

        assertEquals(testUsername, userDTO.username(), "username should return the set value");
        assertEquals(testPassword, userDTO.password(), "password should return the set value");
        assertEquals(testFirstName, userDTO.firstName(), "firstName should return the set value");
        assertEquals(testLastName, userDTO.lastName(), "lastName should return the set value");
        assertEquals(testEmail, userDTO.email(), "email should return the set value");
    }

    @Test
    public void testEqualsWithSameValues() {
        UserDTO user1 = new UserDTO("user1", "pass1", "First", "User", "first.user@example.com");
        UserDTO user2 = new UserDTO("user1", "pass1", "First", "User", "first.user@example.com");

        assertEquals(user1, user2, "Objects with same values should be equal");
        assertEquals(user2, user1, "equals method should be symmetric");
    }

    @Test
    public void testEqualsWithDifferentValues() {
        UserDTO user1 = new UserDTO("user1", "pass1", "First", "User", "first.user@example.com");
        UserDTO user2 = new UserDTO("user2", "pass2", "Second", "User", "second.user@example.com");

        assertNotEquals(user1, user2, "Objects with different values should not be equal");
    }

    @Test
    public void testEqualsWithNull() {
        UserDTO user = new UserDTO("user1", "pass1", "First", "User", "first.user@example.com");

        assertNotEquals(null, user, "Comparing with null should return false");
    }

    @Test
    public void testEqualsWithDifferentClass() {
        UserDTO user = new UserDTO("user1", "pass1", "First", "User", "first.user@example.com");

        assertNotEquals("Not a UserDTO", user, "Comparing with different class should return false");
    }

    @Test
    public void testHashCode() {
        UserDTO user1 = new UserDTO("user1", "pass1", "First", "User", "first.user@example.com");
        UserDTO user2 = new UserDTO("user1", "pass1", "First", "User", "first.user@example.com");

        assertEquals(user1.hashCode(), user2.hashCode(), "Objects with same values should have same hashCode");
    }

    @Test
    public void testToString() {
        UserDTO user = new UserDTO("testuser", "testpass", "Test", "User", "test.user@example.com");
        
        String toStringResult = user.toString();
        
        assertTrue(toStringResult.contains("testuser"), "toString result should contain username");
        assertTrue(toStringResult.contains("testpass"), "toString result should contain password");
        assertTrue(toStringResult.contains("Test"), "toString result should contain firstName");
        assertTrue(toStringResult.contains("User"), "toString result should contain lastName");
        assertTrue(toStringResult.contains("test.user@example.com"), "toString result should contain email");
    }

    @Test
    public void testNullValues() {
        UserDTO user = new UserDTO(null, null, null, null, null);
        
        assertNull(user.username(), "username should return null if not set");
        assertNull(user.password(), "password should return null if not set");
        assertNull(user.firstName(), "firstName should return null if not set");
        assertNull(user.lastName(), "lastName should return null if not set");
        assertNull(user.email(), "email should return null if not set");
        
        UserDTO anotherUser = new UserDTO(null, null, null, null, null);
        assertEquals(user, anotherUser, "Two objects with null values should be equal");
        assertEquals(user.hashCode(), anotherUser.hashCode(), "Two objects with null values should have same hashCode");
    }
    
}