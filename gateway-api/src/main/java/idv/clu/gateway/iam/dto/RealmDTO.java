package idv.clu.gateway.iam.dto;

import java.util.Objects;

/**
 * @author clu
 */
public class RealmDTO {

    private String realmId;
    private String realmName;

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RealmDTO realmDTO = (RealmDTO) o;
        return Objects.equals(realmId, realmDTO.realmId) && Objects.equals(realmName, realmDTO.realmName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(realmId, realmName);
    }

    @Override
    public String toString() {
        return "RealmDTO{" +
                "realmId='" + realmId + '\'' +
                ", realmName='" + realmName + '\'' +
                '}';
    }

}
