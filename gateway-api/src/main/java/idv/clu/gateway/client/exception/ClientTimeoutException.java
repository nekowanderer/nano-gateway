package idv.clu.gateway.client.exception;

/**
 * @author clu
 */
public class ClientTimeoutException extends RuntimeException {

    public ClientTimeoutException(String message) {
        super(message);
    }

}
