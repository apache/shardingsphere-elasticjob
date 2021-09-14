package org.apache.shardingsphere.elasticjob.lite.internal.annotation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobProp;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfigurationFactory;
import org.apache.shardingsphere.elasticjob.infra.exception.JobAnnotationException;

public class JobAnnotationBuilder {
    
    /**
     * generate JobConfiguration from @ElasticJobConfiguration.
     * @param type The job of @ElasticJobConfiguration annotation class
     * @return JobConfiguration
     */
    public static JobConfiguration generateJobConfiguration(final Class<?> type) {
        ElasticJobConfiguration annotation = type.getAnnotation(ElasticJobConfiguration.class);
        Preconditions.checkArgument(null != annotation, "@ElasticJobConfiguration not found by class '%s'.", type);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(annotation.jobName()), "@ElasticJobConfiguration jobName not be empty by class '%s'.", type);
        JobConfiguration.Builder jobConfigurationBuilder = JobConfiguration.newBuilder(annotation.jobName(), annotation.shardingTotalCount())
                .shardingItemParameters(annotation.shardingItemParameters())
                .cron(Strings.isNullOrEmpty(annotation.cron()) ? null : annotation.cron())
                .timeZone(Strings.isNullOrEmpty(annotation.timeZone()) ? null : annotation.timeZone())
                .jobParameter(annotation.jobParameter())
                .monitorExecution(annotation.monitorExecution())
                .failover(annotation.failover())
                .misfire(annotation.misfire())
                .maxTimeDiffSeconds(annotation.maxTimeDiffSeconds())
                .reconcileIntervalMinutes(annotation.reconcileIntervalMinutes())
                .jobShardingStrategyType(annotation.jobShardingStrategyType())
                .jobExecutorServiceHandlerType(annotation.jobExecutorServiceHandlerType())
                .jobErrorHandlerType(Strings.isNullOrEmpty(annotation.jobErrorHandlerType()) ? null : annotation.jobErrorHandlerType())
                .jobListenerTypes(annotation.jobListenerTypes())
                .description(annotation.description())
                .disabled(annotation.disabled())
                .overwrite(annotation.overwrite());
        for (Class<? extends JobExtraConfigurationFactory> clazz : annotation.extraConfigurations()) {
            try {
                JobExtraConfiguration jobExtraConfiguration = clazz.newInstance().getJobExtraConfiguration();
                jobConfigurationBuilder.addExtraConfigurations(jobExtraConfiguration);
            } catch (IllegalAccessException | InstantiationException exception) {
                throw (JobAnnotationException) new JobAnnotationException("new JobExtraConfigurationFactory instance by class '%s' failure", clazz).initCause(exception);
            }
        }
        for (ElasticJobProp prop :annotation.props()) {
            jobConfigurationBuilder.setProperty(prop.key(), prop.value());
        }
    
        return jobConfigurationBuilder.build();
    }
    
}
