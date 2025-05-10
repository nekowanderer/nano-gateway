package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class GroupNotFoundException extends RuntimeException {

    private final String realmName;
    private final String groupId;

    public GroupNotFoundException(String realmName, String groupId) {
        super(String.format("Group not found in realm: %s, groupId: %s", realmName, groupId));
        this.realmName = realmName;
        this.groupId = groupId;
    }

    public String getRealmName() {
        return realmName;
    }

    public String getGroupId() {
        return groupId;
    }

}
