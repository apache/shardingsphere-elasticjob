package org.apache.shardingsphere.elasticjob.kernel.infra.util;

/**
 * Tracks elapsed time and interruption for a bounded wait loop, so callers can consistently
 * decide when to give up instead of blocking indefinitely.
 */
public final class BoundedWaitGuard {

    private final long maxWaitMillis;

    private final long startTime;

    private BoundedWaitGuard(final long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Start tracking a new bounded wait.
     *
     * @param maxWaitMillis max milliseconds to wait before giving up
     * @return a new guard, timing starts now
     */
    public static BoundedWaitGuard start(final long maxWaitMillis) {
        return new BoundedWaitGuard(maxWaitMillis);
    }

    /**
     * Judge whether the caller should give up waiting: either the thread was interrupted,
     * or the configured max wait time has elapsed.
     *
     * @return whether to give up
     */
    public boolean shouldGiveUp() {
        return Thread.currentThread().isInterrupted() || System.currentTimeMillis() - startTime > maxWaitMillis;
    }

    /**
     * Judge whether giving up is specifically due to interruption, so callers can log accordingly.
     *
     * @return whether the current thread is interrupted
     */
    public boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }
}