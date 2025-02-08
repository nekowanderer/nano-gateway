package idv.clu.api.strategy.routing;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author clu
 */
@ApplicationScoped
public class RoundRobinRoutingStrategy implements RoutingStrategy {

    private final static Logger LOG = LoggerFactory.getLogger(RoundRobinRoutingStrategy.class);

    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    @Override
    public String getNextTargetUrl(List<String> availableInstances) {
        if (availableInstances == null || availableInstances.isEmpty()) {
            throw new IllegalArgumentException("No available instances for routing");
        }

        final int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % availableInstances.size());
        final String nextUrl = availableInstances.get(index);
        LOG.debug("Next target URL selected by round robin: {}", nextUrl);
        return nextUrl;
    }

}

