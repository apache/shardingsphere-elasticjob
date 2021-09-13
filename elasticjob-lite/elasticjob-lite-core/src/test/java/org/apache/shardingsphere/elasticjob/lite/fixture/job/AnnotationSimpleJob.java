package org.apache.shardingsphere.elasticjob.lite.fixture.job;

import lombok.Getter;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobProp;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

@Getter
@ElasticJobConfiguration(
    jobName = "AnnotationSimpleJob",
    description = "desc",
    shardingTotalCount = 3,
    shardingItemParameters = "0=a,1=b,2=c",
    cron = "*/10 * * * * ?",
    props = {
        @ElasticJobProp(key = "print.title", value = "test title"),
        @ElasticJobProp(key = "print.content", value = "test content")
    }
)
public class AnnotationSimpleJob implements SimpleJob {
    
    private volatile boolean completed;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        completed = true;
    }
}
