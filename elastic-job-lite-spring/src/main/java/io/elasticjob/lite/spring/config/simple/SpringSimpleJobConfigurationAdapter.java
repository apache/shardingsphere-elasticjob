package io.elasticjob.lite.spring.config.simple;

import io.elasticjob.lite.api.simple.SimpleJob;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.spring.job.util.AopTargetUtils;
import lombok.Getter;
/**
 * spring simple job configuration support cglib proxy
 * @author chennina
 *
 */
public class SpringSimpleJobConfigurationAdapter {
  
  @Getter
  private SimpleJobConfiguration simpleJobConfiguration;

  public SpringSimpleJobConfigurationAdapter(SimpleJob simpleJob,final String cron, final int shardingTotalCount, final String shardingItemParameters) {
   
    JobCoreConfiguration jobCoreConfiguration= JobCoreConfiguration.newBuilder(
         AopTargetUtils.getTargetCglibClassName(simpleJob), cron, shardingTotalCount).shardingItemParameters(shardingItemParameters).build();   
     this.simpleJobConfiguration = new SimpleJobConfiguration(jobCoreConfiguration,  AopTargetUtils.getTargetCglibClassName(simpleJob));
     
     
  }
}
  