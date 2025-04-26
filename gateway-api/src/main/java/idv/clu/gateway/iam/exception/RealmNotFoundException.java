package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class RealmNotFoundException extends RuntimeException {

    private final String realmId;
    private final String realmName;

    public RealmNotFoundException(String realmId, String realmName) {
        super(String.format("Realm with id '%s' and name '%s' not found.", realmId, realmName));
        this.realmId = realmId;
        this.realmName = realmName;
    }

    public String getRealmId() {
        return realmId;
    }

    public String getRealmName() {
        return realmName;
    }

}
