package idv.clu.api.strategy.routing;

/**
 * @author clu
 */
public interface RoutingStrategy {

    String getNextTargetUrl(String urlPath);

}

