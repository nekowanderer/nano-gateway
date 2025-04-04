package idv.clu.api.cache;

import io.smallrye.mutiny.Uni;

/**
 * @author clu
 */
public interface CacheService {

    Uni<String> getItem(String key);

    Uni<Void> putItem(String key, Object value);

}
