package idv.clu.gateway.strategy.retry;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExponentialBackoffRetryStrategyTest {

    @Test
    void testSuccessfulExecutionWithoutRetries() throws Exception {
        Callable<String> callable = mock(Callable.class);
        when(callable.call()).thenReturn("success");

        ExponentialBackoffRetryStrategy retryStrategy = new ExponentialBackoffRetryStrategy(3, 100);

        String result = retryStrategy.executeWithRetries(callable);

        assertEquals("success", result);
        verify(callable, times(1)).call();
    }

    @Test
    void testRetriesAndEventuallySucceeds() throws Exception {
        Callable<String> callable = mock(Callable.class);
        when(callable.call())
                .thenThrow(new RuntimeException("First attempt failed"))
                .thenThrow(new RuntimeException("Second attempt failed"))
                .thenReturn("success");

        ExponentialBackoffRetryStrategy retryStrategy = new ExponentialBackoffRetryStrategy(3, 100);

        String result = retryStrategy.executeWithRetries(callable);

        assertEquals("success", result);
        verify(callable, times(3)).call();
    }

    @Test
    void testExceedsMaxRetriesAndThrowsLastException() throws Exception {
        Callable<String> callable = mock(Callable.class);
        when(callable.call()).thenThrow(new RuntimeException("Always fails"));

        ExponentialBackoffRetryStrategy retryStrategy = new ExponentialBackoffRetryStrategy(3, 100);

        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> retryStrategy.executeWithRetries(callable));
        assertEquals("Always fails", thrownException.getMessage());
        verify(callable, times(3)).call();
    }

    @Test
    void testNoRetriesWhenMaxRetryCountIsZero() throws Exception {
        Callable<String> callable = mock(Callable.class);
        when(callable.call()).thenThrow(new RuntimeException("No retries allowed"));

        ExponentialBackoffRetryStrategy retryStrategy = new ExponentialBackoffRetryStrategy(0, 100);

        assertThrows(AssertionError.class, () -> retryStrategy.executeWithRetries(callable));
        verify(callable, times(0)).call();
    }

    @Test
    void testDelayBetweenRetries() throws Exception {
        Callable<String> callable = mock(Callable.class);
        when(callable.call())
                .thenThrow(new RuntimeException("First attempt failed"))
                .thenThrow(new RuntimeException("Second attempt failed"))
                .thenReturn("success");

        ExponentialBackoffRetryStrategy retryStrategy = spy(new ExponentialBackoffRetryStrategy(3, 100));

        String result = retryStrategy.executeWithRetries(callable);

        assertEquals("success", result);
        verify(retryStrategy, times(1)).executeWithRetries(callable);
    }

}