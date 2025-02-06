package idv.clu.api;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author clu
 */
public class TestPropertyResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> properties = new HashMap<>();
        properties.put("SIMPLE_API_INSTANCES",
                "http://simple_api_1:8080,http://simple_api_2:8080,http://simple_api_3:8080");
        return properties;
    }

    @Override
    public void stop() {
    }

}

