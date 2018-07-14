package io.elasticjob.lite.spring.config.dataflow;

import io.elasticjob.lite.api.dataflow.DataflowJob;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import io.elasticjob.lite.spring.job.util.AopTargetUtils;
import lombok.Getter;

/**
 * Spring configuration support cglib proxy
 * @author chennina
 *
 */
public class SpringFlowJobConfigurationAdapter {
  
  @Getter
  private DataflowJobConfiguration dataflowJobConfiguration;
  
  public SpringFlowJobConfigurationAdapter(DataflowJob<?> dataflowJob,final String cron, final int shardingTotalCount, final String shardingItemParameters,final boolean  streamingProcess) {
   
    JobCoreConfiguration jobCoreConfiguration= JobCoreConfiguration.newBuilder(
        AopTargetUtils.getTargetCglibClassName(dataflowJob), cron, shardingTotalCount).shardingItemParameters(shardingItemParameters).build();  
    this.dataflowJobConfiguration= new DataflowJobConfiguration(jobCoreConfiguration,  AopTargetUtils.getTargetCglibClassName(dataflowJob), streamingProcess);
  }
  
  

}