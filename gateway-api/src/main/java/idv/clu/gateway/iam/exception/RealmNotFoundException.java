package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class RealmNotFoundException extends RuntimeException {

    private final String realmId;

    public RealmNotFoundException(String realmId) {
        super(String.format("Realm with id '%s' not found", realmId));
        this.realmId = realmId;
    }

    public String getRealmId() {
        return realmId;
    }

}
