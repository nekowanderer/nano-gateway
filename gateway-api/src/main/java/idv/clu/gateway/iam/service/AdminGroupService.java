package idv.clu.gateway.iam.service;

import idv.clu.gateway.iam.exception.GroupAlreadyExistsException;
import idv.clu.gateway.iam.exception.GroupNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author clu
 */
@ApplicationScoped
public class AdminGroupService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AdminGroupService.class);

    private final Keycloak keycloak;

    @Inject
    public AdminGroupService(@Named("masterRealmClient") Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public String createGroup(final String realmName, final String groupName) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);
        try (Response response = keycloak.realm(realmName).groups().add(group)) {
            if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                throw new GroupAlreadyExistsException(realmName, groupName);
            } else if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new RuntimeException("Failed to create group on realm: " + realmName);
            }
            return CreatedResponseUtil.getCreatedId(response);
        }
    }

    public void deleteGroup(final String realmName, final String groupId) {
        try {
            keycloak.realm(realmName).groups().group(groupId).remove();
        } catch (Exception e) {
            if (e instanceof WebApplicationException webEx
                    && webEx.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                int status = webEx.getResponse().getStatus();
                LOGGER.warn("Delete group failed: realm={}, groupId={}, status={}", realmName, groupId, status);
                throw new GroupNotFoundException(realmName, groupId);
            } else {
                throw new RuntimeException("Failed to delete group: " + groupId, e);
            }
        }
    }

}
