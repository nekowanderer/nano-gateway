package idv.clu.gateway.iam.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author clu
 */
public class GroupDTOTest {

    @Test
    public void testGetter() {
        String testGroupName = "Developers";

        GroupDTO groupDTO = new GroupDTO(testGroupName);

        assertEquals(testGroupName, groupDTO.groupName(), "getGroupName should return the set value");
    }

    @Test
    public void testEqualsWithSameValues() {
        GroupDTO group1 = new GroupDTO("Admins");
        GroupDTO group2 = new GroupDTO("Admins");

        assertEquals(group1, group2, "Objects with same groupName should be equal");
        assertEquals(group2, group1, "equals method should be symmetric");
    }

    @Test
    public void testEqualsWithDifferentValues() {
        GroupDTO group1 = new GroupDTO("Admins");
        GroupDTO group2 = new GroupDTO("Users");

        assertNotEquals(group1, group2, "Objects with different groupName should not be equal");
    }

    @Test
    public void testEqualsWithNull() {
        GroupDTO group = new GroupDTO("Test");

        assertNotEquals(null, group, "Comparing with null should return false");
    }

    @Test
    public void testEqualsWithDifferentClass() {
        GroupDTO group = new GroupDTO("Test");

        assertNotEquals("Test", group, "Comparing with different class should return false");
    }

    @Test
    public void testHashCode() {
        GroupDTO group1 = new GroupDTO("Team A");
        GroupDTO group2 = new GroupDTO("Team A");

        assertEquals(group1.hashCode(), group2.hashCode(), "Objects with same groupName should have same hashCode");
    }

    @Test
    public void testToString() {
        GroupDTO group = new GroupDTO("Engineering");

        String toStringResult = group.toString();

        assertTrue(toStringResult.contains("Engineering"), "toString result should contain groupName");
    }

    @Test
    public void testNullValue() {
        GroupDTO group = new GroupDTO(null);

        assertNull(group.groupName(), "getGroupName should return null if not set");

        GroupDTO anotherGroup = new GroupDTO(null);
        assertEquals(group, anotherGroup, "Two objects with null groupName should be equal");
        assertEquals(group.hashCode(), anotherGroup.hashCode(), "HashCodes should match for null groupName");
    }

}
