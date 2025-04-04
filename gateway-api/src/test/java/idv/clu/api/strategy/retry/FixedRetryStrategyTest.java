package idv.clu.api.strategy.retry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author clu
 */
public class FixedRetryStrategyTest {

    private static final int RETRY_COUNT = 3;

    @Test
    void testExecuteWithRetriesSuccess() throws Exception {
        RetryStrategy retryStrategy = new FixedRetryStrategy(RETRY_COUNT);
        AtomicInteger counter = new AtomicInteger();

        String result = retryStrategy.executeWithRetries(() -> {
            int count = counter.incrementAndGet();
            if (count < RETRY_COUNT) {
                throw new IOException("Retry attempt " + count);
            }
            return "Success";
        });

        assertEquals("Success", result);
        assertEquals(RETRY_COUNT, counter.get());
    }

    @Test
    void testExecuteWithRetriesFailure() {
        RetryStrategy retryStrategy = new FixedRetryStrategy(RETRY_COUNT);
        AtomicInteger counter = new AtomicInteger();

        assertThrows(IOException.class, () -> retryStrategy.executeWithRetries(() -> {
            counter.incrementAndGet();
            throw new IOException("Failure");
        }));
        assertEquals(RETRY_COUNT, counter.get());
    }

}
