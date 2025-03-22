package idv.clu.api.cache;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheResourceTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CacheResource cacheResource;

    public CacheResourceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCacheItemWithValidKey() {
        String key = "testKey";
        String cachedItem = "cachedValue";
        when(cacheService.getItem(key)).thenReturn(Uni.createFrom().item(cachedItem));

        Response response = cacheResource.getCacheItem(key).await().indefinitely();

        assertEquals(200, response.getStatus());
        assertEquals(cachedItem, response.getEntity());
        verify(cacheService).getItem(key);
    }

    @Test
    void testGetCacheItemWithNonExistentKey() {
        String key = "nonExistentKey";
        when(cacheService.getItem(key)).thenReturn(Uni.createFrom().nullItem());

        Response response = cacheResource.getCacheItem(key).await().indefinitely();

        assertEquals(200, response.getStatus());
        assertNull(response.getEntity());
        verify(cacheService).getItem(key);
    }

    @Test
    void testPutCacheItemWithValidKeyAndPayload() {
        String key = "testKey";
        String payload = "testPayload";
        when(cacheService.putItem(key, payload)).thenReturn(Uni.createFrom().nullItem());

        Response response = cacheResource.putCacheItem(key, payload).await().indefinitely();

        assertEquals(200, response.getStatus());
        verify(cacheService).putItem(key, payload);
    }

    @Test
    void testPutCacheItemWithNullKeyOrPayload() {
        assertThrows(NullPointerException.class, () -> {
            String key = null;
            String payload = null;
            when(cacheService.putItem(key, payload)).thenReturn(Uni.createFrom().failure(new NullPointerException("Key or payload is null")));

            cacheResource.putCacheItem(key, payload).await().indefinitely();

            verify(cacheService).putItem(key, payload);
        });
    }

}