package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class JoinGroupFailedException extends RuntimeException {

    private final String realmName;
    private final String groupId;
    private final String userId;

    public JoinGroupFailedException(String realmName, String groupId, String userId) {
        super(String.format("Failed to join user %s to group: %s in realm: %s", userId, groupId, realmName));
        this.realmName = realmName;
        this.groupId = groupId;
        this.userId = userId;
    }

    public String getRealmName() {
        return realmName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getUserId() {
        return userId;
    }

}
