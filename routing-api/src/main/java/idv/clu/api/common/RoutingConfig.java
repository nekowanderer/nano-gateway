package idv.clu.api.common;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author clu
 */
@ApplicationScoped
public class RoutingConfig {

    @ConfigProperty(name = "AVAILABLE_API_INSTANCES")
    String apiInstances;

    public Set<String> getAvailableInstances() {
        return new HashSet<>(Arrays.asList(apiInstances.split(",")));
    }

}
