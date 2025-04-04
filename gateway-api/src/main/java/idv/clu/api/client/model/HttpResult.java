package idv.clu.api.client.model;

import jakarta.ws.rs.core.Response;

/**
 * @author clu
 */
public class HttpResult {

    private final String endpoint;
    private final Response response;
    private final Exception exception;

    public HttpResult(String endpoint, Response response) {
        this.endpoint = endpoint;
        this.response = response;
        this.exception = null;
    }

    public HttpResult(String endpoint, Exception exception) {
        this.endpoint = endpoint;
        this.response = null;
        this.exception = exception;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Response getResponse() {
        return response;
    }

    public Exception getException() {
        return exception;
    }

    public boolean hasResponse() {
        return response != null && exception == null;
    }

}
