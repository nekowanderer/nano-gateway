package idv.clu.gateway.circuitbreaker;

/**
 * @author clu
 */
public class CircuitContext {

    private long lastFailedTime;

    public long getLastFailedTime() {
        return lastFailedTime;
    }

    public void setLastFailedTime(long lastFailedTime) {
        this.lastFailedTime = lastFailedTime;
    }

}
