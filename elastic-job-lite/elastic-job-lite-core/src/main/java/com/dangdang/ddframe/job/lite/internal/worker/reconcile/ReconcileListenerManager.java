package com.dangdang.ddframe.job.lite.internal.worker.reconcile;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

import com.dangdang.ddframe.job.lite.internal.config.ConfigurationNode;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

public class ReconcileListenerManager extends AbstractListenerManager {
    
    private final ConfigurationNode configNode;
    
    private int reconcileIntervalSeconds = ReconcileWorker.DEFAULT_SLEEP_TIME / 1000;
    
    public ReconcileListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        super(regCenter, jobName);
        configNode = new ConfigurationNode(jobName);
    }
    
    @Override
    public void start() {
        addDataListener(new ReconcileIntervalSecondsChangedJobListener());
    }
    
    class ReconcileIntervalSecondsChangedJobListener extends AbstractJobListener {
    
        @Override
        protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
            if (configNode.isConfigPath(path) && Type.NODE_UPDATED == event.getType()) {
                int newReconcileIntervalSeconds = LiteJobConfigurationGsonFactory.fromJson(new String(event.getData().getData())).getReconcileIntervalSeconds();
                if (Long.compare(newReconcileIntervalSeconds, reconcileIntervalSeconds) != 0) {
                    reconcileIntervalSeconds = newReconcileIntervalSeconds;
                    ReconcileWorker.setSleepTime(newReconcileIntervalSeconds * 1000);
                }
            }
        }
    }
}
