package idv.clu.gateway.strategy.retry;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author clu
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface RetryStrategyType {

    Strategy value();

    enum Strategy {
        FIXED_DELAY, EXPONENTIAL_BACKOFF
    }

}
