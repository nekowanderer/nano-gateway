package idv.clu.gateway.cache;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.RedisAPI;
import io.vertx.mutiny.redis.client.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisCacheServiceTest {

    @Mock
    RedisAPI redisAPI;

    @Mock
    Response mockResponse;

    @InjectMocks
    RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        redisCacheService.environment = "dev";
        redisCacheService.duration = Duration.ofMillis(500);
        redisCacheService.expiry = 500L;
    }

    @Test
    void testGetItemSuccessfullyForDevEnvironment() {
        when(mockResponse.toString()).thenReturn("mockValue");
        when(redisAPI.get("dev_testKey")).thenReturn(Uni.createFrom().item(mockResponse));

        String result = redisCacheService.getItem("testKey").subscribeAsCompletionStage().join();

        assertEquals("mockValue", result);
        verify(redisAPI, times(1)).get("dev_testKey");
    }

    @Test
    void testGetItemSuccessfullyForContainerEnvironment() {
        redisCacheService.environment = "container";

        when(mockResponse.toString()).thenReturn("mockValue");
        when(redisAPI.get("container_testKey")).thenReturn(Uni.createFrom().item(mockResponse));

        String result = redisCacheService.getItem("testKey").subscribeAsCompletionStage().join();

        assertEquals("mockValue", result);
        verify(redisAPI, times(1)).get("container_testKey");
    }

    @Test
    void testGetItemSuccessfullyForProductionEnvironment() {
        redisCacheService.environment = "production";

        when(mockResponse.toString()).thenReturn("mockValue");
        when(redisAPI.get("testKey")).thenReturn(Uni.createFrom().item(mockResponse));

        String result = redisCacheService.getItem("testKey").subscribeAsCompletionStage().join();

        assertEquals("mockValue", result);
        verify(redisAPI, times(1)).get("testKey");
    }

    @Test
    void testGetItemReturnsNullWhenNoResponse() {
        when(redisAPI.get("dev_testKey")).thenReturn(Uni.createFrom().nullItem());

        String result = redisCacheService.getItem("testKey").subscribeAsCompletionStage().join();

        assertNull(result);
        verify(redisAPI, times(1)).get("dev_testKey");
    }

    @Test
    void testGetItemHandlesFailureGracefully() {
        when(redisAPI.get("dev_testKey")).thenReturn(
                Uni.createFrom().failure(new RuntimeException("Connection error"))
        );

        String result = redisCacheService.getItem("testKey").subscribeAsCompletionStage().join();

        assertNull(result);
        verify(redisAPI, times(1)).get("dev_testKey");
    }

    @Test
    void testGetItemHandlesTimeoutGracefully() {
        when(redisAPI.get("dev_testKey"))
                .thenReturn(
                        Uni.createFrom().item(mockResponse).onItem().delayIt().by(Duration.ofSeconds(1))
                );

        String result = redisCacheService.getItem("testKey").subscribeAsCompletionStage().join();

        assertNull(result);
        verify(redisAPI, times(1)).get("dev_testKey");
    }

    @Test
    void testPutItemSuccessfullyForDevEnvironment() {
        when(redisAPI.set(List.of("dev_testKey", "testValue", "PX", "500")))
                .thenReturn(
                        Uni.createFrom().item(mockResponse)
                );

        redisCacheService.putItem("testKey", "testValue").subscribeAsCompletionStage().join();

        verify(redisAPI, times(1)).set(List.of("dev_testKey", "testValue", "PX", "500"));
    }

    @Test
    void testPutItemSuccessfullyForContainerEnvironment() {
        redisCacheService.environment = "container";
        when(redisAPI.set(List.of("container_testKey", "testValue", "PX", "500")))
                .thenReturn(
                        Uni.createFrom().item(mockResponse)
                );

        redisCacheService.putItem("testKey", "testValue").subscribeAsCompletionStage().join();

        verify(redisAPI, times(1)).set(List.of("container_testKey", "testValue", "PX", "500"));
    }

    @Test
    void testPutItemSuccessfullyForProductionEnvironment() {
        redisCacheService.environment = "production";
        when(redisAPI.set(List.of("testKey", "testValue", "PX", "500")))
                .thenReturn(
                        Uni.createFrom().item(mockResponse)
                );

        redisCacheService.putItem("testKey", "testValue").subscribeAsCompletionStage().join();

        verify(redisAPI, times(1)).set(List.of("testKey", "testValue", "PX", "500"));
    }

    @Test
    void testPutItemHandlesNullResponseGracefully() {
        when(redisAPI.set(List.of("dev_testKey", "testValue", "PX", "500")))
                .thenReturn(
                        Uni.createFrom().nullItem()
                );

        redisCacheService.putItem("testKey", "testValue").subscribeAsCompletionStage().join();

        verify(redisAPI, times(1)).set(List.of("dev_testKey", "testValue", "PX", "500"));
    }

    @Test
    void testPutItemHandlesFailureGracefully() {
        when(redisAPI.set(List.of("dev_testKey", "testValue", "PX", "500")))
                .thenReturn(
                        Uni.createFrom().failure(new RuntimeException("Connection error"))
                );

        redisCacheService.putItem("testKey", "testValue").subscribeAsCompletionStage().join();

        verify(redisAPI, times(1)).set(List.of("dev_testKey", "testValue", "PX", "500"));
    }

    @Test
    void testPutItemHandlesTimeoutGracefully() {
        when(redisAPI.set(List.of("dev_testKey", "testValue", "PX", "500")))
                .thenReturn(
                        Uni.createFrom().item(mockResponse).onItem().delayIt().by(Duration.ofSeconds(1))
                );

        redisCacheService.putItem("testKey", "testValue").subscribeAsCompletionStage().join();

        verify(redisAPI, times(1)).set(List.of("dev_testKey", "testValue", "PX", "500"));
    }

}