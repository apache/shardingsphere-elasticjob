package com.dangdang.example.elasticjob.spring.job.listener;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

public class SimpleListener implements ElasticJobListener {
    
    @Override
    public void beforeJobExecuted(final ShardingContext shardingContext) {
        System.out.println("beforeJobExecuted:" + shardingContext.getJobName());
    }
    
    @Override
    public void afterJobExecuted(final ShardingContext shardingContext) {
        System.out.println("afterJobExecuted:" + shardingContext.getJobName());
    }
}
