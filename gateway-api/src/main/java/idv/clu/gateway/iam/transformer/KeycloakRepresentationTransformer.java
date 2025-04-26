package idv.clu.gateway.iam.transformer;

import idv.clu.gateway.iam.dto.RealmDTO;
import idv.clu.gateway.iam.dto.UserDTO;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * @author clu
 */
public class KeycloakRepresentationTransformer {

    public static UserRepresentation toUserRepresentation(final UserDTO userDTO) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userDTO.password());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.username());
        user.setCredentials(List.of(credential));
        user.setFirstName(userDTO.firstName());
        user.setLastName(userDTO.lastName());
        user.setEmail(userDTO.email());
        user.setEnabled(true);
        user.setRealmRoles(List.of("admin"));

        return user;
    }

    public static RealmDTO toRealmDTO(final RealmRepresentation realmRepresentation) {
        return new RealmDTO(
                realmRepresentation.getId(),
                realmRepresentation.getRealm(),
                realmRepresentation.isEnabled());
    }

}
