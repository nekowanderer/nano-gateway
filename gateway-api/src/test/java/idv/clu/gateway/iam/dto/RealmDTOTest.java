package idv.clu.gateway.iam.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author clu
 */
public class RealmDTOTest {

    @Test
    public void testGetterAndSetter() {
        String testRealmId = "test-realm-id";
        String testRealmName = "Test Realm Name";

        RealmDTO realmDTO = new RealmDTO(testRealmId, testRealmName, true);

        assertEquals(testRealmId, realmDTO.realmId(), "getRealmId should return the set value");
        assertEquals(testRealmName, realmDTO.realmName(), "getRealmName should return the set value");
    }

    @Test
    public void testEqualsWithSameValues() {
        RealmDTO realm1 = new RealmDTO("realm-1", "Realm One", true);
        RealmDTO realm2 = new RealmDTO("realm-1", "Realm One", true);

        assertEquals(realm1, realm2, "Objects with same values should be equal");
        assertEquals(realm2, realm1, "equals method should be symmetric");
    }

    @Test
    public void testEqualsWithDifferentValues() {
        RealmDTO realm1 = new RealmDTO("realm-1", "Realm One", true);
        RealmDTO realm2 = new RealmDTO("realm-2", "Realm Two", true);

        assertNotEquals(realm1, realm2, "Objects with different values should not be equal");
    }

    @Test
    public void testEqualsWithNull() {
        RealmDTO realm = new RealmDTO("realm-1", "Realm One", true);

        assertNotEquals(null, realm, "Comparing with null should return false");
    }

    @Test
    public void testEqualsWithDifferentClass() {
        RealmDTO realm = new RealmDTO("realm-1", "Realm One", true);

        assertNotEquals("Not a RealmDTO", realm, "Comparing with different class should return false");
    }

    @Test
    public void testHashCode() {
        RealmDTO realm1 = new RealmDTO("realm-1", "Realm One", true);
        RealmDTO realm2 = new RealmDTO("realm-1", "Realm One", true);

        assertEquals(realm1.hashCode(), realm2.hashCode(), "Objects with same values should have same hashCode");
    }

    @Test
    public void testToString() {
        RealmDTO realm = new RealmDTO("test-realm", "Test Realm", true);
        
        String toStringResult = realm.toString();
        
        assertTrue(toStringResult.contains("test-realm"), "toString result should contain realmId");
        assertTrue(toStringResult.contains("Test Realm"), "toString result should contain realmName");
    }

    @Test
    public void testNullValues() {
        RealmDTO realm = new RealmDTO(null, null, false);
        
        assertNull(realm.realmId(), "getRealmId should return null if not set");
        assertNull(realm.realmName(), "getRealmName should return null if not set");
        
        RealmDTO anotherRealm = new RealmDTO(null, null, false);
        assertEquals(realm, anotherRealm, "Two objects with null values should be equal");
        assertEquals(realm.hashCode(), anotherRealm.hashCode(), "Two objects with null values should have same hashCode");
    }
    
}