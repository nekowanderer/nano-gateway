package idv.clu.api.circuitbreaker;

/**
 * @author clu
 */
public enum CircuitState {

    CLOSED {
        @Override
        public CircuitState recordFailure(long failedTime, CircuitContext context) {
            context.setLastFailedTime(failedTime);
            return OPEN;
        }

        @Override
        public boolean isOpen() {
            return false;
        }
    },
    OPEN {
        @Override
        public CircuitState checkState(long currentTime, long delayTimeMs, CircuitContext context) {
            if (currentTime - context.getLastFailedTime() >= delayTimeMs) {
                return HALF_OPEN;
            }
            return this;
        }

        @Override
        public boolean isOpen() {
            return true;
        }
    },
    HALF_OPEN {
        @Override
        public CircuitState recordFailure(long failedTime, CircuitContext context) {
            context.setLastFailedTime(failedTime);
            return OPEN;
        }

        @Override
        public CircuitState recordSuccess() {
            return CLOSED;
        }

        @Override
        public boolean isOpen() {
            return false;
        }
    };

    public CircuitState recordFailure(long failedTime, CircuitContext context) {
        return this;
    }

    public CircuitState recordSuccess() {
        return this;
    }

    public CircuitState checkState(long currentTime, long delayTimeMs, CircuitContext context) {
        return this;
    }

    public abstract boolean isOpen();

}
