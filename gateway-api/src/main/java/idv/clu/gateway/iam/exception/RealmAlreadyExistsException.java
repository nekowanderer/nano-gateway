package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class RealmAlreadyExistsException extends RuntimeException {

    private final String realmId;

    public RealmAlreadyExistsException(String realmId) {
        super(String.format("Realm with id '%s' already exists", realmId));
        this.realmId = realmId;
    }

    public String getRealmId() {
        return realmId;
    }

}
