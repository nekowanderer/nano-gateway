package idv.clu.api.common;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.List;

/**
 * @author clu
 */
@ApplicationScoped
public class RoutingConfig {

    @ConfigProperty(name = "SIMPLE_API_INSTANCES")
    String apiInstances;

    public List<String> getSimpleApiInstances() {
        return Arrays.asList(apiInstances.split(","));
    }

}
