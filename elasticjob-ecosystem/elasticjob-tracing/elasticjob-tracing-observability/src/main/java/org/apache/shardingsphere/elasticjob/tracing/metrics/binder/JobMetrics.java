package org.apache.shardingsphere.elasticjob.tracing.metrics.binder;

import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class JobMetrics {
    /**
     * jobname -> state -> count
     * support job level metrics
     */
    private Map<String, Map<JobStatusTraceEvent.State, AtomicLong>> metrics = new ConcurrentHashMap<>();

    public void increase(String jobName, JobStatusTraceEvent.State state) {
        metrics.computeIfAbsent(jobName, k -> new ConcurrentHashMap<>());
        metrics.get(jobName).computeIfAbsent(state, k -> new AtomicLong(0)).incrementAndGet();
    }


}
