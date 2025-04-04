package idv.clu.api.strategy.retry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.Callable;

/**
 * @author clu
 */
@ApplicationScoped
@RetryStrategyType(RetryStrategyType.Strategy.FIXED_DELAY)
public class FixedRetryStrategy implements RetryStrategy {

    @Inject
    @ConfigProperty(name = "strategy.retry.fixed.max.count", defaultValue = "3")
    int maxRetryCount;

    @SuppressWarnings("unused")
    public FixedRetryStrategy() {
    }

    public FixedRetryStrategy(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public <T> T executeWithRetries(Callable<T> action) throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt < maxRetryCount; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                lastException = e;
            }
        }
        assert lastException != null;
        throw lastException;
    }

}

