package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class GroupAlreadyExistsException extends RuntimeException {

    private final String realmName;
    private final String groupName;

    public GroupAlreadyExistsException(String realmName, String groupName) {
        super(String.format("Group '%s' already exists in realm: %s", groupName, realmName));
        this.realmName = realmName;
        this.groupName = groupName;
    }

    public String getRealmName() {
        return realmName;
    }

    public String getGroupName() {
        return groupName;
    }

}
