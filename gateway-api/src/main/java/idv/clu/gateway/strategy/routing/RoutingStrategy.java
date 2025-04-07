package idv.clu.gateway.strategy.routing;

/**
 * @author clu
 */
public interface RoutingStrategy {

    String getNextTargetUrl(String urlPath);

}

