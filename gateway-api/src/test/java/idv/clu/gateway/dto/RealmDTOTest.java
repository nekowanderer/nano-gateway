package idv.clu.gateway.dto;

import idv.clu.gateway.iam.dto.RealmDTO;
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

        RealmDTO realmDTO = new RealmDTO();
        realmDTO.setRealmId(testRealmId);
        realmDTO.setRealmName(testRealmName);

        assertEquals(testRealmId, realmDTO.getRealmId(), "getRealmId should return the set value");
        assertEquals(testRealmName, realmDTO.getRealmName(), "getRealmName should return the set value");
    }

    @Test
    public void testEqualsWithSameValues() {
        RealmDTO realm1 = new RealmDTO();
        realm1.setRealmId("realm-1");
        realm1.setRealmName("Realm One");

        RealmDTO realm2 = new RealmDTO();
        realm2.setRealmId("realm-1");
        realm2.setRealmName("Realm One");

        assertEquals(realm1, realm2, "Objects with same values should be equal");
        assertEquals(realm2, realm1, "equals method should be symmetric");
    }

    @Test
    public void testEqualsWithDifferentValues() {
        RealmDTO realm1 = new RealmDTO();
        realm1.setRealmId("realm-1");
        realm1.setRealmName("Realm One");

        RealmDTO realm2 = new RealmDTO();
        realm2.setRealmId("realm-2");
        realm2.setRealmName("Realm Two");

        assertNotEquals(realm1, realm2, "Objects with different values should not be equal");
    }

    @Test
    public void testEqualsWithNull() {
        RealmDTO realm = new RealmDTO();
        realm.setRealmId("realm-1");
        realm.setRealmName("Realm One");

        assertNotEquals(null, realm, "Comparing with null should return false");
    }

    @Test
    public void testEqualsWithDifferentClass() {
        RealmDTO realm = new RealmDTO();
        realm.setRealmId("realm-1");
        realm.setRealmName("Realm One");

        assertNotEquals("Not a RealmDTO", realm, "Comparing with different class should return false");
    }

    @Test
    public void testHashCode() {
        RealmDTO realm1 = new RealmDTO();
        realm1.setRealmId("realm-1");
        realm1.setRealmName("Realm One");

        RealmDTO realm2 = new RealmDTO();
        realm2.setRealmId("realm-1");
        realm2.setRealmName("Realm One");
        
        assertEquals(realm1.hashCode(), realm2.hashCode(), "Objects with same values should have same hashCode");
    }

    @Test
    public void testToString() {
        RealmDTO realm = new RealmDTO();
        realm.setRealmId("test-realm");
        realm.setRealmName("Test Realm");
        
        String toStringResult = realm.toString();
        
        assertTrue(toStringResult.contains("test-realm"), "toString result should contain realmId");
        assertTrue(toStringResult.contains("Test Realm"), "toString result should contain realmName");
    }

    @Test
    public void testNullValues() {
        RealmDTO realm = new RealmDTO();
        
        assertNull(realm.getRealmId(), "getRealmId should return null if not set");
        assertNull(realm.getRealmName(), "getRealmName should return null if not set");
        
        RealmDTO anotherRealm = new RealmDTO();
        assertEquals(realm, anotherRealm, "Two objects with null values should be equal");
        assertEquals(realm.hashCode(), anotherRealm.hashCode(), "Two objects with null values should have same hashCode");
    }
    
}