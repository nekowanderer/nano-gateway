package idv.clu.api.client.exception;

/**
 * @author clu
 *
 * For handling 5xx errors.
 */
public class ServerErrorException extends RuntimeException {

    private final int statusCode;

    public ServerErrorException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}

