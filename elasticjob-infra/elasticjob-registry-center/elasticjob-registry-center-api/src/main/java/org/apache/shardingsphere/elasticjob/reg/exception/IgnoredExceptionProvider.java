package org.apache.shardingsphere.elasticjob.reg.exception;

import java.util.Collection;

/**
 * Ignored exception provider.
 */
public interface IgnoredExceptionProvider {
    
    /**
     * Get ignored exceptions.
     *
     * @return ignored exceptions
     */
    Collection<Class<? extends Throwable>> getIgnoredExceptions();
}
