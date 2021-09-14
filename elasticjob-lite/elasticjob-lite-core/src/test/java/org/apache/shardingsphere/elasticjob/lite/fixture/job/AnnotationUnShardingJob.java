package org.apache.shardingsphere.elasticjob.lite.fixture.job;

import lombok.Getter;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

@Getter
@ElasticJobConfiguration(
    jobName = "AnnotationUnShardingJob",
    description = "desc",
    shardingTotalCount = 1
)
public class AnnotationUnShardingJob implements SimpleJob {
    
    private volatile boolean completed;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        completed = true;
    }
}
