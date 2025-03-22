package idv.clu.api.cache;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author clu
 */
@Path("/cache")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CacheResource {

    private final static Logger LOG = LoggerFactory.getLogger(CacheResource.class);

    @Inject
    CacheService cacheService;

    public CacheResource() {}

    @GET
    @Path("/item/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getCacheItem(@PathParam("key") String key) {
        LOG.info("Fetching cache item with key: {}", key);
        return cacheService
                .getItem(key)
                .onItem()
                .transform(value -> Response.ok(value).build()
                );
    }

    @PUT
    @Path("/item/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> putCacheItem(@PathParam("key") String key, String requestPayload) {
        LOG.info("Storing cache item with key: {}", key);
        return cacheService
                .putItem(key, requestPayload)
                .onItem()
                .transform(ignored -> Response.ok().build()
                );
    }

}
