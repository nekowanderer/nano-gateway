package idv.clu.api.client.provider;

import idv.clu.api.client.SimpleApiClient;
import idv.clu.api.common.RoutingConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author clu
 */
@ApplicationScoped
public class RestClientProvider {

    private final static Logger LOG = LoggerFactory.getLogger(RestClientProvider.class);

    @Inject
    RoutingConfig routingConfig;

    List<SimpleApiClient> clients;
    private AtomicInteger index;

    @PostConstruct
    void init() {
        index = new AtomicInteger(0);
        List<String> instances = routingConfig.getAvailableInstances();
        if (instances.isEmpty()) {
            throw new IllegalStateException("No simple api instances configured.");
        }
        this.clients = instances
                .stream()
                .map(this::createClient)
                .collect(Collectors.toList());

        LOG.info("Successfully initialized SimpleApiClients.");

    }

    SimpleApiClient createClient(String instanceUrl) {
        try {
            LOG.info("Create client for instance: {}", instanceUrl);
            return RestClientBuilder
                    .newBuilder()
                    .baseUri(new URI(instanceUrl))
                    .build(SimpleApiClient.class);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public SimpleApiClient getNextClient() {
        return clients.get(index.getAndUpdate(i -> (i + 1) % clients.size()));
    }

}
