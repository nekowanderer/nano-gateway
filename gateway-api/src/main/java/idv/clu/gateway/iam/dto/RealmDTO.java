package idv.clu.gateway.iam.dto;

/**
 * @author clu
 */
public record RealmDTO(
        String realmId,
        String realmName,
        boolean isEnabled
) {

}
