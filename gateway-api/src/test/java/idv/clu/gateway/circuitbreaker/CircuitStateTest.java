package idv.clu.gateway.circuitbreaker;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author clu
 */
public class CircuitStateTest {

    @Test
    public void testRecordFailureFromClosed() {
        CircuitContext context = Mockito.mock(CircuitContext.class);
        long failedTime = System.currentTimeMillis();

        CircuitState resultState = CircuitState.CLOSED.recordFailure(failedTime, context);

        Mockito.verify(context).setLastFailedTime(failedTime);
        assertEquals(CircuitState.OPEN, resultState);
        assertTrue(resultState.isOpen());
    }

    @Test
    public void testRecordFailureFromHalfOpen() {
        CircuitContext context = Mockito.mock(CircuitContext.class);
        long failedTime = System.currentTimeMillis();

        CircuitState resultState = CircuitState.HALF_OPEN.recordFailure(failedTime, context);

        Mockito.verify(context).setLastFailedTime(failedTime);
        assertEquals(CircuitState.OPEN, resultState);
        assertTrue(resultState.isOpen());
    }

    @Test
    public void testRecordFailureFromOpen() {
        CircuitContext context = Mockito.mock(CircuitContext.class);
        long failedTime = System.currentTimeMillis();

        CircuitState resultState = CircuitState.OPEN.recordFailure(failedTime, context);

        Mockito.verify(context, Mockito.never()).setLastFailedTime(Mockito.anyLong());
        assertEquals(CircuitState.OPEN, resultState);
        assertTrue(resultState.isOpen());
    }

    @Test
    public void testRecordSuccessFromClosed() {
        CircuitState resultState = CircuitState.HALF_OPEN.recordSuccess();

        assertEquals(CircuitState.CLOSED, resultState);
        assertFalse(resultState.isOpen());
    }

    @Test
    public void testCheckStateFromOpen() {
        CircuitContext context = new CircuitContext();

        long failedTime = System.currentTimeMillis();
        CircuitState initialState = CircuitState.CLOSED.recordFailure(failedTime, context);
        assertEquals(CircuitState.OPEN, initialState);
        assertTrue(initialState.isOpen());

        CircuitState middleState = initialState.checkState((failedTime + 1L), 10L, context);
        assertEquals(CircuitState.OPEN, middleState);
        assertTrue(middleState.isOpen());

        CircuitState resultState = initialState.checkState((failedTime + 100L), 10L, context);
        assertEquals(CircuitState.HALF_OPEN, resultState);
        assertFalse(resultState.isOpen());
    }

    @Test
    public void testBehaviorWhenNothingChanged() {
        CircuitContext context = new CircuitContext();
        CircuitState initialState = CircuitState.CLOSED;

        assertEquals(CircuitState.CLOSED, initialState.recordSuccess());
        assertEquals(CircuitState.CLOSED, initialState.checkState(10L, 10L, context));
    }

}