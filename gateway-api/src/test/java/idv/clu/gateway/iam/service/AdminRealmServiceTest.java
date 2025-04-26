package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.RealmAlreadyExistsException;
import idv.clu.gateway.iam.exception.RealmNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminRealmServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private RealmsResource realmsResource;

    private AdminRealmService adminRealmService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminRealmService = new AdminRealmService(keycloak);
        when(keycloak.realms()).thenReturn(realmsResource);
    }

    @Test
    void testCreateRealmSuccess() {
        String testRealmId = "test-realm-id";
        String testRealmName = "Test Realm";
        List<RealmRepresentation> existingRealms = new ArrayList<>();
        RealmRepresentation existingRealm = new RealmRepresentation();
        existingRealm.setId("existing-realm");
        existingRealms.add(existingRealm);

        when(realmsResource.findAll()).thenReturn(existingRealms);
        doNothing().when(realmsResource).create(any(RealmRepresentation.class));

        adminRealmService.createRealm(testRealmId, testRealmName);

        ArgumentCaptor<RealmRepresentation> realmCaptor = ArgumentCaptor.forClass(RealmRepresentation.class);
        verify(realmsResource).create(realmCaptor.capture());

        RealmRepresentation capturedRealm = realmCaptor.getValue();
        assertEquals(testRealmId, capturedRealm.getId(), "The realm ID should match");
        assertEquals(testRealmName, capturedRealm.getRealm(), "The realm name should match");
        assertEquals(true, capturedRealm.isEnabled(), "The realm should be enabled");
    }

    @Test
    void testCreateRealmAlreadyExists() {
        String testRealmId = "existing-realm";
        String testRealmName = "Existing Realm";
        List<RealmRepresentation> existingRealms = new ArrayList<>();
        RealmRepresentation existingRealm = new RealmRepresentation();
        existingRealm.setId(testRealmId);
        existingRealms.add(existingRealm);

        when(realmsResource.findAll()).thenReturn(existingRealms);

        RealmAlreadyExistsException exception = assertThrows(RealmAlreadyExistsException.class,
                () -> adminRealmService.createRealm(testRealmId, testRealmName),
                "Should throw RealmAlreadyExistsException when realm already exists");

        assertEquals(testRealmId, exception.getRealmId(), "The exception realm ID should match the test realm ID");
    }

    @Test
    void testDeleteRealmSuccess() {
        String testRealmId = "test-realm-id";
        String testRealmName = "Test Realm";

        List<RealmRepresentation> existingRealms = new ArrayList<>();
        RealmRepresentation existingRealm = new RealmRepresentation();
        existingRealm.setId(testRealmId);
        existingRealm.setRealm(testRealmName);
        existingRealms.add(existingRealm);

        when(realmsResource.findAll()).thenReturn(existingRealms);
        when(keycloak.realm(testRealmName)).thenReturn(realmResource);
        doNothing().when(realmResource).remove();

        adminRealmService.deleteRealm(testRealmId);

        verify(keycloak).realm(testRealmName);
        verify(realmResource).remove();
    }

    @Test
    void testDeleteRealmNotFound() {
        String testRealmId = "non-existent-realm";
        List<RealmRepresentation> existingRealms = new ArrayList<>();
        RealmRepresentation existingRealm = new RealmRepresentation();
        existingRealm.setId("different-realm");
        existingRealms.add(existingRealm);

        when(realmsResource.findAll()).thenReturn(existingRealms);

        RealmNotFoundException exception = assertThrows(RealmNotFoundException.class,
                () -> adminRealmService.deleteRealm(testRealmId),
                "Should throw RealmNotFoundException when realm does not exist");

        assertEquals(testRealmId, exception.getRealmId(), "The exception realm ID should match the test realm ID");
    }

}
