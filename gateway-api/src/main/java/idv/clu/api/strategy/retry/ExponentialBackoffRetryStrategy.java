package idv.clu.api.strategy.retry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Exponential Backoff Retry Strategy Implementation
 * <p/>
 * This strategy retries an operation with an exponentially increasing delay
 * between retries. For example, if the base delay is 500ms, the retry delays
 * will be: 500ms, 1s, 2s, 4s, and so on.
 *
 * @author clu
 */
@ApplicationScoped
@RetryStrategyType(RetryStrategyType.Strategy.EXPONENTIAL_BACKOFF)
public class ExponentialBackoffRetryStrategy implements RetryStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ExponentialBackoffRetryStrategy.class);

    @Inject
    @ConfigProperty(name = "strategy.retry.exponentialbackoff.max.count", defaultValue = "2")
    int maxRetryCount;

    @Inject
    @ConfigProperty(name = "strategy.retry.exponentialbackoff.delay.millis", defaultValue = "500")
    long baseDelayMillis;

    @SuppressWarnings("unused")
    public ExponentialBackoffRetryStrategy() {
    }

    public ExponentialBackoffRetryStrategy(int maxRetryCount, long baseDelayMillis) {
        this.maxRetryCount = maxRetryCount;
        this.baseDelayMillis = baseDelayMillis;
    }

    @Override
    public <T> T executeWithRetries(Callable<T> action) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt < maxRetryCount; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                lastException = e;

                long delay = (long) (baseDelayMillis * Math.pow(2, attempt));

                LOG.debug("Retry attempt {} failed. Retrying in {} ms.", attempt + 1, delay);

                Thread.sleep(delay);
            }
        }

        assert lastException != null;
        throw lastException;
    }

}

