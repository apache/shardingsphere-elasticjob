package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.election.LeaderService;
import io.elasticjob.lite.internal.instance.InstanceService;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.plugins.management.ShutdownHookPlugin;
import org.quartz.spi.ClassLoadHelper;

/**
 * 作业关闭钩子.
 *
 * @author zhangliang
 */
public final class JobShutdownHookPlugin extends ShutdownHookPlugin {
    
    private String jobName;
    
    private Thread t;
    
    @Override
    public void initialize(final String name, final Scheduler scheduler, final ClassLoadHelper classLoadHelper) throws SchedulerException {
        getLog().info("Registering Quartz shutdown hook.");
    
        t = new Thread("Quartz Shutdown-Hook "
                + scheduler.getSchedulerName()) {
            @Override
            public void run() {
                getLog().info("Shutting down Quartz...");
                try {
                    scheduler.shutdown(isCleanShutdown());
                } catch (SchedulerException e) {
                    getLog().info(
                            "Error shutting down Quartz: " + e.getMessage(), e);
                }
            }
        };
    
        Runtime.getRuntime().addShutdownHook(t);
        jobName = scheduler.getSchedulerName();
    }
    
    @Override
    public void shutdown() {
        CoordinatorRegistryCenter regCenter = JobRegistry.getInstance().getRegCenter(jobName);
        if (null == regCenter) {
            return;
        }
        LeaderService leaderService = new LeaderService(regCenter, jobName);
        if (leaderService.isLeader()) {
            leaderService.removeLeader();
        }
        new InstanceService(regCenter, jobName).removeInstance();
        if (!Thread.currentThread().getName().equals(t.getName())) {
            Runtime.getRuntime().removeShutdownHook(t);
        }
    }
}
