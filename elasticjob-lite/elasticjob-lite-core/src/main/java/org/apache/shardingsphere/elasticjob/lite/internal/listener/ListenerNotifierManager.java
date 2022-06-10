package org.apache.shardingsphere.elasticjob.lite.internal.listener;

import org.apache.curator.utils.ThreadUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Manage listener's notify executor,
 * each job has its own listener notify executor.
 */
public final class ListenerNotifierManager {

    private static volatile ListenerNotifierManager instance;

    private final Map<String, Executor> listenerNotifyExecutors = new ConcurrentHashMap<>();

    private ListenerNotifierManager() { }

    /**
     * Get singleton instance of ListenerNotifierManager.
     * @return singleton instance of ListenerNotifierManager.
     */
    public static ListenerNotifierManager getInstance() {
        if (null == instance) {
            synchronized (ListenerNotifierManager.class) {
                if (null == instance) {
                    instance = new ListenerNotifierManager();
                }
            }
        }
        return instance;
    }

    /**
     * Register a listener notify executor for the job specified.
     * @param jobName The job's name.
     */
    public void registerJobNotifyExecutor(final String jobName) {
        if (!listenerNotifyExecutors.containsKey(jobName)) {
            synchronized (this) {
                if (!listenerNotifyExecutors.containsKey(jobName)) {
                    ThreadFactory threadFactory = ThreadUtils.newGenericThreadFactory("ListenerNotify-" + jobName);
                    Executor notifyExecutor = Executors.newSingleThreadExecutor(threadFactory);
                    listenerNotifyExecutors.put(jobName, notifyExecutor);
                }
            }
        }
    }

    /**
     * Get the listener notify executor for the specified job.
     * @param jobName The job's name.
     * @return The job listener's notify executor.
     */
    public Executor getJobNotifyExecutor(final String jobName) {
        return listenerNotifyExecutors.get(jobName);
    }
}
