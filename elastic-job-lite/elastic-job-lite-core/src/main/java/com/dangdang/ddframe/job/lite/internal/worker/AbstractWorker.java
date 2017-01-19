package com.dangdang.ddframe.job.lite.internal.worker;

public abstract class AbstractWorker implements Runnable{

    public abstract void doWork();
    
    @Override
    public void run() {
        doWork();
    }
}
