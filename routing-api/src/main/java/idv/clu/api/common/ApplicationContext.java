package idv.clu.api.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import okhttp3.OkHttpClient;

/**
 * @author clu
 */
@ApplicationScoped
public class ApplicationContext {

    @Produces
    @ApplicationScoped
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

}
