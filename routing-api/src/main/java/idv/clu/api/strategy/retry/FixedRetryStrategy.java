package idv.clu.api.strategy.retry;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.Callable;

/**
 * @author clu
 */
@ApplicationScoped
public class FixedRetryStrategy implements RetryStrategy {

    private static final int DEFAULT_MAX_RETRY_COUNT = 3;

    private final int maxRetryCount;

    @SuppressWarnings("unused")
    public FixedRetryStrategy() {
        this(DEFAULT_MAX_RETRY_COUNT);
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

