package com.dangdang.ddframe.job.lite.internal.worker.reconcile;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

import com.dangdang.ddframe.job.lite.internal.config.ConfigurationNode;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

public class ReconcileWorkerListenerManager extends AbstractListenerManager {

    private final ConfigurationNode configNode;

    private long reconcileCycleTime = ReconcileWorker.DEFAULT_SLEEP_TIME / 1000;

    public ReconcileWorkerListenerManager(CoordinatorRegistryCenter regCenter, String jobName) {
        super(regCenter, jobName);
        configNode = new ConfigurationNode(jobName);
    }

    @Override
    public void start() {
        addDataListener(new ReconcileCycleTimeChangedJobListener());
    }
    
    class ReconcileCycleTimeChangedJobListener extends AbstractJobListener {

        @Override
        protected void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) {
            if(configNode.isConfigPath(path) && Type.NODE_UPDATED == event.getType()) {
                long newReconcileCycleTime = LiteJobConfigurationGsonFactory.fromJson(new String(event.getData().getData())).getReconcileCycleTime();
                if(Long.compare(newReconcileCycleTime, reconcileCycleTime) != 0) {
                    reconcileCycleTime = newReconcileCycleTime;
                    ReconcileWorker.setSleepTime(newReconcileCycleTime * 1000);
                }
            }
        }
    }
}
