package idv.clu.gateway.strategy.retry;

import java.util.concurrent.Callable;

/**
 * @author clu
 */
public interface RetryStrategy {

    <T> T executeWithRetries(Callable<T> action) throws Exception;

}

