package org.apache.shardingsphere.elasticjob.annotation.job.impl;

import org.apache.shardingsphere.elasticjob.annotation.EnableElasticJob;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobProp;
import org.apache.shardingsphere.elasticjob.annotation.job.CustomJob;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

@EnableElasticJob(
        cron = "0/5 * * * * ?",
        jobName = "SimpleTestJob",
        shardingTotalCount = 3,
        shardingItemParameters = "0=Beijing,1=Shanghai,2=Guangzhou",
        jobListenerTypes = {"NOOP", "LOG"},
        props = {@ElasticJobProp(key = "print.title", value = "test title")}
)
public class SimpleTestJob implements CustomJob {
    
    @Override
    public void execute(final ShardingContext shardingContext) {
    }
    
}
