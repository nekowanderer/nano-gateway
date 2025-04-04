package idv.clu.api.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import okhttp3.OkHttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.TimeUnit;

/**
 * @author clu
 */
@ApplicationScoped
public class ApplicationContext {

    @Inject
    @ConfigProperty(name = "client.timeout.connect")
    int connectTimeout;

    @Inject
    @ConfigProperty(name = "client.timeout.read")
    int readTimeout;

    @Inject
    @ConfigProperty(name = "client.timeout.write")
    int writeTimeout;

    @Produces
    @ApplicationScoped
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();
    }

}
