package idv.clu.gateway.strategy.routing;

import idv.clu.gateway.common.RoutingConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author clu
 */
@ApplicationScoped
public class RoundRobinRoutingStrategy implements RoutingStrategy {

    private final static Logger LOG = LoggerFactory.getLogger(RoundRobinRoutingStrategy.class);

    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    @Inject
    RoutingConfig routingConfig;

    Set<String> availableInstances;

    @PostConstruct
    public void init() {
        LOG.info("Initializing RoundRobinRoutingStrategy.");

        this.availableInstances = routingConfig.getAvailableInstances();

        if (availableInstances.isEmpty()) {
            throw new IllegalStateException("No API instances configured in routingConfig.");
        }

        LOG.info("Available API instances: {}", availableInstances);
    }

    @Override
    public String getNextTargetUrl(String urlPath) {
        if (availableInstances == null || availableInstances.isEmpty()) {
            throw new IllegalArgumentException("No available instances for routing");
        }

        // Convert Set to List for indexed access
        List<String> instancesList = new ArrayList<>(availableInstances);

        // TODO check node's health status here according to the list
        final int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % instancesList.size());
        final String nextUrl = instancesList.get(index).concat(urlPath);
        LOG.debug("Next target URL selected by round robin: {}", nextUrl);
        return nextUrl;
    }

}
