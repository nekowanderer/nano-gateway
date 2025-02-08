package idv.clu.api.strategy.routing;

import java.util.List;

/**
 * @author clu
 */
public interface RoutingStrategy {

    String getNextTargetUrl(List<String> availableInstances);

}

