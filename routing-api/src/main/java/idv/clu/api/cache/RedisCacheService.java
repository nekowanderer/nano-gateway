package idv.clu.api.cache;

import idv.clu.api.common.enums.Environment;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.RedisAPI;
import io.vertx.mutiny.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * @author clu
 *
 * TODO: Add null check for input parameter and error handling, then modify the test.
 */
@ApplicationScoped
public class RedisCacheService implements CacheService{

    private static final Logger LOG = LoggerFactory.getLogger(RedisCacheService.class);
    private static final String PEXPIRE_IN_MILLISECONDS = "PX";

    @Inject
    RedisAPI redisAPI;

    @ConfigProperty(name = "environment")
    String environment;

    @ConfigProperty(name = "api.redis.reactive-timeout")
    Duration duration;

    @ConfigProperty(name = "api.redis.default-expiry-ms")
    Long expiry;

    public RedisCacheService() {
    }

    @Override
    public Uni<String> getItem(String key) {
        final String cacheKey = appendEnvironmentPrefix(key);
        return redisAPI
                .get(cacheKey)
                .ifNoItem()
                .after(duration)
                .fail()
                .map(response -> {
                    LOG.debug("Obtained key {} with value: {}", cacheKey, response);
                    return Optional.ofNullable(response)
                            .map(Response::toString)
                            .orElse(null);
                }).onFailure().recoverWithItem(error -> {
                    LOG.error("Failed to get key: {}, error: {}", cacheKey, error.getMessage());
                    return null;
                });
    }

    @Override
    public Uni<Void> putItem(String key, Object value) {
        final String cacheKey = appendEnvironmentPrefix(key);
        return redisAPI
                .set(List.of(
                        cacheKey, value.toString(), PEXPIRE_IN_MILLISECONDS, String.valueOf(expiry)))
                .ifNoItem()
                .after(duration)
                .fail()
                .map(response -> {
                    LOG.debug("Set key {} with result: {}", cacheKey, response);
                    return null;
                }).onFailure().recoverWithItem(error -> {
                    LOG.error("Failed to cache key: {}, expiry: {}, error: {}", cacheKey, expiry, error.getMessage());
                    return null;
                }).replaceWithVoid();
    }

    private String appendEnvironmentPrefix(String key) {
        Environment environment = Environment.valueOf(this.environment.toUpperCase());
        String prefix;
        switch (environment) {
            case DEV -> prefix = "dev_";
            case CONTAINER -> prefix = "container_";
            default -> prefix = "";
        }

        return prefix + key;
    }

}
