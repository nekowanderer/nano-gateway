package idv.clu.gateway.iam.transformer;

import idv.clu.gateway.iam.dto.UserDTO;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * @author clu
 */
public class KeycloakRepresentationTransformer {

    public static UserRepresentation toUserRepresentation(final UserDTO userDTO) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userDTO.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setCredentials(List.of(credential));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setRealmRoles(List.of("admin"));

        return user;
    }

}
