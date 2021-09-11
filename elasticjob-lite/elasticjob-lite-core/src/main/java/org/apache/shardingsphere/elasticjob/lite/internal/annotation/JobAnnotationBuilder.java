package org.apache.shardingsphere.elasticjob.lite.internal.annotation;

import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobProp;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfigurationFactory;
import org.apache.shardingsphere.elasticjob.infra.exception.JobAnnotationException;

public class JobAnnotationBuilder {
    
    private final Class<?> type;
    
    public JobAnnotationBuilder(Class<?> type) {
        this.type = type;
    }
    
    public JobConfiguration generateJobConfiguration() {
        ElasticJobConfiguration annotation = type.getAnnotation(ElasticJobConfiguration.class);
        String jobName = annotation.jobName();
        JobConfiguration.Builder jobConfigurationBuilder = JobConfiguration.newBuilder(jobName, annotation.shardingTotalCount())
                .cron(annotation.cron())
                .shardingItemParameters(annotation.shardingItemParameters())
                .jobParameter(annotation.jobParameter())
                .monitorExecution(annotation.monitorExecution())
                .failover(annotation.failover())
                .misfire(annotation.misfire())
                .maxTimeDiffSeconds(annotation.maxTimeDiffSeconds())
                .reconcileIntervalMinutes(annotation.reconcileIntervalMinutes())
                .jobShardingStrategyType(annotation.jobShardingStrategyType())
                .jobExecutorServiceHandlerType(annotation.jobExecutorServiceHandlerType())
                .jobErrorHandlerType(annotation.jobErrorHandlerType())
                .jobListenerTypes(annotation.jobListenerTypes())
                .addExtraConfigurations(null)
                .description(annotation.description())
                .setProperty(null, null)
                .disabled(annotation.disabled())
                .overwrite(annotation.overwrite())
                .label(annotation.label())
                .staticSharding(annotation.staticSharding());
        for (Class<? extends JobExtraConfigurationFactory> clazz : annotation.extraConfigurations()) {
            try {
                JobExtraConfiguration jobExtraConfiguration = clazz.newInstance().getJobExtraConfiguration();
                jobConfigurationBuilder.addExtraConfigurations(jobExtraConfiguration);
            } catch (IllegalAccessException | InstantiationException  exception) {
                throw (JobAnnotationException) new JobAnnotationException("new JobExtraConfigurationFactory instance by class '%s' failure", clazz).initCause(exception);
            }
        }
        for (ElasticJobProp prop :annotation.props()) {
            jobConfigurationBuilder.setProperty(prop.key(), prop.value());
        }
    
        return jobConfigurationBuilder.build();
    }
    
}
