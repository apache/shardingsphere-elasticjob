package com.dangdang.ddframe.job.lite.internal.worker;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dangdang.ddframe.job.lite.internal.worker.reconcile.ReconcileWorker;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

public class WorkersManager {

    private static final int WORKER_NUM = 10;
    
    private final ExecutorService executor = Executors.newFixedThreadPool(WORKER_NUM);
    
    private final List<AbstractWorker> workers = new LinkedList<>();
    
    public WorkersManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        workers.add(new ReconcileWorker(regCenter, jobName));
    }
    
    public void start() {
        for(AbstractWorker worker : workers) {
            executor.submit(worker);
        }
    }
}
