package idv.clu.api.common;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationContextTest {

    @Test
    void okHttpClientShouldBuildOkHttpClientWithConfiguration() {
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.connectTimeout = 5000;
        applicationContext.readTimeout = 10000;
        applicationContext.writeTimeout = 15000;

        OkHttpClient okHttpClient = applicationContext.okHttpClient();

        assertNotNull(okHttpClient);
        assertEquals(5000, okHttpClient.connectTimeoutMillis());
        assertEquals(10000, okHttpClient.readTimeoutMillis());
        assertEquals(15000, okHttpClient.writeTimeoutMillis());
    }

}
